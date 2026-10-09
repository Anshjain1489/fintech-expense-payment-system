package com.ansh.fintech.service;

import com.ansh.fintech.dto.BudgetRequest;
import com.ansh.fintech.dto.ExpenseCreateResponse;
import com.ansh.fintech.dto.ExpenseRequest;
import com.ansh.fintech.exception.ResourceNotFoundException;
import com.ansh.fintech.model.Budget;
import com.ansh.fintech.model.Expense;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    private Firestore firestore;
    private AuditService auditService;
    private Transaction transaction;
    private ExpenseService expenseService;

    private CollectionReference usersRef;
    private DocumentReference userDocRef;
    private CollectionReference expensesRef;
    private CollectionReference budgetsRef;
    private DocumentReference expenseDocRef;
    private DocumentReference budgetDocRef;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        auditService = mock(AuditService.class);
        transaction = mock(Transaction.class);

        usersRef = mock(CollectionReference.class);
        userDocRef = mock(DocumentReference.class);
        expensesRef = mock(CollectionReference.class);
        budgetsRef = mock(CollectionReference.class);
        expenseDocRef = mock(DocumentReference.class);
        budgetDocRef = mock(DocumentReference.class);

        when(firestore.collection("users")).thenReturn(usersRef);
        when(usersRef.document(anyString())).thenReturn(userDocRef);
        when(userDocRef.collection("expenses")).thenReturn(expensesRef);
        when(userDocRef.collection("budgets")).thenReturn(budgetsRef);
        when(expensesRef.document(anyString())).thenReturn(expenseDocRef);
        when(budgetsRef.document(anyString())).thenReturn(budgetDocRef);

        // Mock transaction execution to immediately invoke transaction handler
        when(firestore.runTransaction(any())).thenAnswer(invocation -> {
            Object function = invocation.getArgument(0);
            try {
                java.lang.reflect.Method method = function.getClass().getMethods()[0];
                Object result = method.invoke(function, transaction);
                return ApiFutures.immediateFuture(result);
            } catch (Exception e) {
                Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException) ? e.getCause() : e;
                return ApiFutures.immediateFailedFuture(cause);
            }
        });

        expenseService = new ExpenseService(firestore, auditService);
    }

    @Test
    void testCreateExpenseUnderBudget() throws Exception {
        String uid = "user-100";
        ExpenseRequest request = new ExpenseRequest(50000L, "groceries", "acc-1", "Weekly food", "2026-10-15", null, false);

        // Mock Budget Snapshot read
        DocumentSnapshot budgetSnap = mock(DocumentSnapshot.class);
        when(budgetSnap.exists()).thenReturn(true);
        when(budgetSnap.getLong("limitPaise")).thenReturn(100000L); // Limit: 1000 INR
        when(budgetSnap.getLong("spentPaise")).thenReturn(20000L); // Already spent: 200 INR

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureBudgetSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureBudgetSnap.get()).thenReturn(budgetSnap);
        when(transaction.get(budgetDocRef)).thenReturn(futureBudgetSnap);

        ExpenseCreateResponse response = expenseService.createExpense(uid, request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertFalse(response.isOverBudget()); // Total 70000 <= 100000 -> Not over budget

        verify(transaction).set(eq(expenseDocRef), anyMap());
        verify(transaction).set(eq(budgetDocRef), anyMap(), eq(SetOptions.merge()));
        verify(auditService).logActionInTransaction(eq(transaction), eq(uid), eq("EXPENSE_CREATED"), anyString(), anyString());
    }

    @Test
    void testCreateExpenseExceedsBudget() throws Exception {
        String uid = "user-100";
        ExpenseRequest request = new ExpenseRequest(90000L, "shopping", "acc-1", "New clothes", "2026-10-15", null, false);

        DocumentSnapshot budgetSnap = mock(DocumentSnapshot.class);
        when(budgetSnap.exists()).thenReturn(true);
        when(budgetSnap.getLong("limitPaise")).thenReturn(100000L); // 1000 INR limit
        when(budgetSnap.getLong("spentPaise")).thenReturn(20000L); // 200 INR spent

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureBudgetSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureBudgetSnap.get()).thenReturn(budgetSnap);
        when(transaction.get(budgetDocRef)).thenReturn(futureBudgetSnap);

        ExpenseCreateResponse response = expenseService.createExpense(uid, request);

        assertNotNull(response);
        assertTrue(response.isOverBudget()); // 20000 + 90000 = 110000 > 100000 -> over budget
    }

    @Test
    void testGetExpenseNotFoundThrowsException() throws Exception {
        String uid = "user-100";
        String expenseId = "non-existent-exp";

        DocumentSnapshot snap = mock(DocumentSnapshot.class);
        when(snap.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureSnap.get()).thenReturn(snap);
        when(expenseDocRef.get()).thenReturn(futureSnap);

        assertThrows(ResourceNotFoundException.class, () -> expenseService.getExpense(uid, expenseId));
    }

    @Test
    void testDeleteExpenseAdjustsBudget() throws Exception {
        String uid = "user-100";
        String expenseId = "exp-789";

        // Mock Expense snapshot read
        DocumentSnapshot expenseSnap = mock(DocumentSnapshot.class);
        when(expenseSnap.exists()).thenReturn(true);
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("id", expenseId);
        expenseData.put("amountPaise", 30000L);
        expenseData.put("categoryId", "transport");
        expenseData.put("month", "2026-10");
        when(expenseSnap.getData()).thenReturn(expenseData);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureExpenseSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureExpenseSnap.get()).thenReturn(expenseSnap);
        when(transaction.get(expenseDocRef)).thenReturn(futureExpenseSnap);

        // Mock Budget snapshot read
        DocumentSnapshot budgetSnap = mock(DocumentSnapshot.class);
        when(budgetSnap.exists()).thenReturn(true);
        when(budgetSnap.getLong("spentPaise")).thenReturn(50000L);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureBudgetSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureBudgetSnap.get()).thenReturn(budgetSnap);
        when(transaction.get(budgetDocRef)).thenReturn(futureBudgetSnap);

        expenseService.deleteExpense(uid, expenseId);

        verify(transaction).delete(expenseDocRef);
        verify(transaction).set(eq(budgetDocRef), eq(Map.of("spentPaise", 20000L)), eq(SetOptions.merge()));
        verify(auditService).logActionInTransaction(eq(transaction), eq(uid), eq("EXPENSE_DELETED"), eq(expenseId), anyString());
    }

    @Test
    void testSetBudgetUsesCategoryIdAndMonthFormat() throws Exception {
        String uid = "user-100";
        BudgetRequest request = new BudgetRequest("dining", "2026-10", 150000L);

        DocumentSnapshot budgetSnap = mock(DocumentSnapshot.class);
        when(budgetSnap.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureBudgetSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureBudgetSnap.get()).thenReturn(budgetSnap);
        when(transaction.get(budgetDocRef)).thenReturn(futureBudgetSnap);

        Budget budget = expenseService.setBudget(uid, request);

        assertNotNull(budget);
        assertEquals("dining_2026-10", budget.getId());
        assertEquals("dining", budget.getCategoryId());
        assertEquals("2026-10", budget.getMonth());
        assertEquals(150000L, budget.getLimitPaise());
        verify(transaction).set(eq(budgetDocRef), anyMap());
    }
}
