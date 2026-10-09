package com.ansh.fintech.controller;

import com.ansh.fintech.dto.ExpenseCreateResponse;
import com.ansh.fintech.dto.ExpenseRequest;
import com.ansh.fintech.model.Expense;
import com.ansh.fintech.security.UserPrincipal;
import com.ansh.fintech.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Expenses", description = "Endpoints for managing user expenses and atomic budget updates")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @Operation(summary = "Create an expense", description = "Creates a new expense for the authenticated user and atomically updates budget spentPaise.")
    public ResponseEntity<ExpenseCreateResponse> createExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseCreateResponse response = expenseService.createExpense(principal.getUid(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List expenses", description = "Lists expenses for the authenticated user, optionally filtered by month (yyyy-MM).")
    public ResponseEntity<List<Expense>> getExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month) {
        List<Expense> expenses = expenseService.getExpenses(principal.getUid(), month);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieves a single expense by ID for the authenticated user.")
    public ResponseEntity<Expense> getExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        Expense expense = expenseService.getExpense(principal.getUid(), id);
        return ResponseEntity.ok(expense);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense", description = "Updates an existing expense and adjusts old and new category budgets atomically.")
    public ResponseEntity<Expense> updateExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ExpenseRequest request) {
        Expense updatedExpense = expenseService.updateExpense(principal.getUid(), id, request);
        return ResponseEntity.ok(updatedExpense);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense", description = "Deletes an expense and subtracts its amount from the budget spentPaise atomically.")
    public ResponseEntity<Void> deleteExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        expenseService.deleteExpense(principal.getUid(), id);
        return ResponseEntity.noContent().build();
    }
}
