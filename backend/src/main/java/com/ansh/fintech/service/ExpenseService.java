package com.ansh.fintech.service;

import com.ansh.fintech.dto.BudgetRequest;
import com.ansh.fintech.dto.ExpenseCreateResponse;
import com.ansh.fintech.dto.ExpenseRequest;
import com.ansh.fintech.exception.ResourceNotFoundException;
import com.ansh.fintech.model.Budget;
import com.ansh.fintech.model.Expense;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ExpenseService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);
    private final Firestore firestore;
    private final AuditService auditService;

    public ExpenseService(Firestore firestore, AuditService auditService) {
        this.firestore = firestore;
        this.auditService = auditService;
    }

    /**
     * Create an expense and atomically update category budget inside a Firestore transaction.
     */
    public ExpenseCreateResponse createExpense(String uid, ExpenseRequest request) {
        String expenseId = UUID.randomUUID().toString();
        String date = request.getDate();
        String month = date.substring(0, 7); // yyyy-MM
        String budgetId = Budget.buildId(request.getCategoryId(), month);

        DocumentReference expenseRef = firestore.collection("users").document(uid).collection("expenses").document(expenseId);
        DocumentReference budgetRef = firestore.collection("users").document(uid).collection("budgets").document(budgetId);

        ApiFuture<ExpenseCreateResponse> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot budgetSnap = transaction.get(budgetRef).get();

            long currentLimit = 0L;
            long currentSpent = 0L;
            if (budgetSnap.exists()) {
                Long limitObj = budgetSnap.getLong("limitPaise");
                Long spentObj = budgetSnap.getLong("spentPaise");
                currentLimit = (limitObj != null) ? limitObj : 0L;
                currentSpent = (spentObj != null) ? spentObj : 0L;
            }

            long newSpentPaise = currentSpent + request.getAmountPaise();
            boolean overBudget = (currentLimit > 0 && newSpentPaise > currentLimit);

            Expense expense = new Expense(
                    expenseId,
                    request.getAmountPaise(),
                    request.getCategoryId(),
                    request.getAccountId() != null ? request.getAccountId() : "default",
                    request.getNote(),
                    date,
                    month,
                    request.getReceiptUrl(),
                    "approved", // Default status
                    Boolean.TRUE.equals(request.getRecurring()),
                    System.currentTimeMillis()
            );

            // --- ALL WRITES AFTER READS ---
            transaction.set(expenseRef, expense.toMap());

            Map<String, Object> budgetMap = new HashMap<>();
            budgetMap.put("id", budgetId);
            budgetMap.put("categoryId", request.getCategoryId());
            budgetMap.put("month", month);
            budgetMap.put("limitPaise", currentLimit);
            budgetMap.put("spentPaise", newSpentPaise);
            transaction.set(budgetRef, budgetMap, SetOptions.merge());

            auditService.logActionInTransaction(transaction, uid, "EXPENSE_CREATED", expenseId,
                    "Created expense amountPaise=" + request.getAmountPaise() + " category=" + request.getCategoryId());

            return new ExpenseCreateResponse(expenseId, overBudget, expense);
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error executing createExpense transaction: ", e);
            throw new RuntimeException("Failed to create expense: " + e.getMessage(), e);
        }
    }

    /**
     * Get an expense by ID.
     */
    public Expense getExpense(String uid, String expenseId) {
        DocumentReference expenseRef = firestore.collection("users").document(uid).collection("expenses").document(expenseId);
        try {
            DocumentSnapshot snapshot = expenseRef.get().get();
            if (!snapshot.exists()) {
                throw new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            }
            return Expense.fromMap(expenseId, snapshot.getData());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to retrieve expense: " + e.getMessage(), e);
        }
    }

    /**
     * List expenses, optionally filtered by month (yyyy-MM).
     */
    public List<Expense> getExpenses(String uid, String month) {
        CollectionReference expensesRef = firestore.collection("users").document(uid).collection("expenses");
        Query query = expensesRef;
        if (month != null && !month.trim().isEmpty()) {
            query = query.whereEqualTo("month", month);
        }

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<Expense> expenses = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                expenses.add(Expense.fromMap(doc.getId(), doc.getData()));
            }
            expenses.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            return expenses;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to list expenses: " + e.getMessage(), e);
        }
    }

    /**
     * Update an expense and adjust affected budget spentPaise atomically inside a transaction.
     */
    public Expense updateExpense(String uid, String expenseId, ExpenseRequest request) {
        DocumentReference expenseRef = firestore.collection("users").document(uid).collection("expenses").document(expenseId);

        String newDate = request.getDate();
        String newMonth = newDate.substring(0, 7);
        String newBudgetId = Budget.buildId(request.getCategoryId(), newMonth);
        DocumentReference newBudgetRef = firestore.collection("users").document(uid).collection("budgets").document(newBudgetId);

        ApiFuture<Expense> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot expenseSnap = transaction.get(expenseRef).get();
            if (!expenseSnap.exists()) {
                throw new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            }

            Expense existingExpense = Expense.fromMap(expenseId, expenseSnap.getData());
            String oldBudgetId = Budget.buildId(existingExpense.getCategoryId(), existingExpense.getMonth());
            DocumentReference oldBudgetRef = firestore.collection("users").document(uid).collection("budgets").document(oldBudgetId);

            DocumentSnapshot oldBudgetSnap = transaction.get(oldBudgetRef).get();
            DocumentSnapshot newBudgetSnap = oldBudgetId.equals(newBudgetId) ? oldBudgetSnap : transaction.get(newBudgetRef).get();

            // --- ALL WRITES AFTER READS ---
            long oldAmount = existingExpense.getAmountPaise();
            long newAmount = request.getAmountPaise();

            if (oldBudgetId.equals(newBudgetId)) {
                long currentSpent = oldBudgetSnap.exists() && oldBudgetSnap.getLong("spentPaise") != null
                        ? oldBudgetSnap.getLong("spentPaise") : 0L;
                long updatedSpent = Math.max(0L, currentSpent - oldAmount + newAmount);

                Map<String, Object> budgetUpdates = new HashMap<>();
                budgetUpdates.put("spentPaise", updatedSpent);
                transaction.set(oldBudgetRef, budgetUpdates, SetOptions.merge());
            } else {
                if (oldBudgetSnap.exists()) {
                    long oldSpent = oldBudgetSnap.getLong("spentPaise") != null ? oldBudgetSnap.getLong("spentPaise") : 0L;
                    transaction.set(oldBudgetRef, Map.of("spentPaise", Math.max(0L, oldSpent - oldAmount)), SetOptions.merge());
                }

                long newSpent = newBudgetSnap.exists() && newBudgetSnap.getLong("spentPaise") != null
                        ? newBudgetSnap.getLong("spentPaise") : 0L;
                transaction.set(newBudgetRef, Map.of(
                        "id", newBudgetId,
                        "categoryId", request.getCategoryId(),
                        "month", newMonth,
                        "spentPaise", newSpent + newAmount
                ), SetOptions.merge());
            }

            Expense updatedExpense = new Expense(
                    expenseId,
                    newAmount,
                    request.getCategoryId(),
                    request.getAccountId() != null ? request.getAccountId() : existingExpense.getAccountId(),
                    request.getNote(),
                    newDate,
                    newMonth,
                    request.getReceiptUrl() != null ? request.getReceiptUrl() : existingExpense.getReceiptUrl(),
                    existingExpense.getStatus(),
                    Boolean.TRUE.equals(request.getRecurring()),
                    existingExpense.getCreatedAt()
            );

            transaction.set(expenseRef, updatedExpense.toMap());
            auditService.logActionInTransaction(transaction, uid, "EXPENSE_UPDATED", expenseId,
                    "Updated expense from amountPaise=" + oldAmount + " to " + newAmount);

            return updatedExpense;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e.getCause();
            }
            throw new RuntimeException("Failed to update expense: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an expense and adjust budget spentPaise inside a transaction.
     */
    public void deleteExpense(String uid, String expenseId) {
        DocumentReference expenseRef = firestore.collection("users").document(uid).collection("expenses").document(expenseId);

        ApiFuture<Void> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot expenseSnap = transaction.get(expenseRef).get();
            if (!expenseSnap.exists()) {
                throw new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            }

            Expense expense = Expense.fromMap(expenseId, expenseSnap.getData());
            String budgetId = Budget.buildId(expense.getCategoryId(), expense.getMonth());
            DocumentReference budgetRef = firestore.collection("users").document(uid).collection("budgets").document(budgetId);

            DocumentSnapshot budgetSnap = transaction.get(budgetRef).get();

            // --- ALL WRITES AFTER READS ---
            if (budgetSnap.exists()) {
                long currentSpent = budgetSnap.getLong("spentPaise") != null ? budgetSnap.getLong("spentPaise") : 0L;
                long updatedSpent = Math.max(0L, currentSpent - expense.getAmountPaise());
                transaction.set(budgetRef, Map.of("spentPaise", updatedSpent), SetOptions.merge());
            }

            transaction.delete(expenseRef);
            auditService.logActionInTransaction(transaction, uid, "EXPENSE_DELETED", expenseId,
                    "Deleted expense amountPaise=" + expense.getAmountPaise());

            return null;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e.getCause();
            }
            throw new RuntimeException("Failed to delete expense: " + e.getMessage(), e);
        }
    }

    /**
     * Set or update a category budget.
     */
    public Budget setBudget(String uid, BudgetRequest request) {
        String budgetId = Budget.buildId(request.getCategoryId(), request.getMonth());
        DocumentReference budgetRef = firestore.collection("users").document(uid).collection("budgets").document(budgetId);

        ApiFuture<Budget> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot budgetSnap = transaction.get(budgetRef).get();
            long currentSpent = 0L;
            if (budgetSnap.exists() && budgetSnap.getLong("spentPaise") != null) {
                currentSpent = budgetSnap.getLong("spentPaise");
            }

            Budget budget = new Budget(
                    budgetId,
                    request.getCategoryId(),
                    request.getMonth(),
                    request.getLimitPaise(),
                    currentSpent
            );

            // --- ALL WRITES AFTER READS ---
            transaction.set(budgetRef, budget.toMap());
            auditService.logActionInTransaction(transaction, uid, "BUDGET_SET", budgetId,
                    "Set budget limitPaise=" + request.getLimitPaise() + " for month=" + request.getMonth());

            return budget;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to set budget: " + e.getMessage(), e);
        }
    }

    /**
     * List user budgets.
     */
    public List<Budget> getBudgets(String uid, String month) {
        CollectionReference budgetsRef = firestore.collection("users").document(uid).collection("budgets");
        Query query = budgetsRef;
        if (month != null && !month.trim().isEmpty()) {
            query = query.whereEqualTo("month", month);
        }

        try {
            QuerySnapshot snapshot = query.get().get();
            List<Budget> budgets = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                budgets.add(Budget.fromMap(doc.getId(), doc.getData()));
            }
            return budgets;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to list budgets: " + e.getMessage(), e);
        }
    }
}
