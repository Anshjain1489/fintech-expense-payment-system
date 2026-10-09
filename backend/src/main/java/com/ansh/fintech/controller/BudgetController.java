package com.ansh.fintech.controller;

import com.ansh.fintech.dto.BudgetRequest;
import com.ansh.fintech.model.Budget;
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
@RequestMapping("/budgets")
@Tag(name = "Budgets", description = "Endpoints for managing category monthly spending budgets")
public class BudgetController {

    private final ExpenseService expenseService;

    public BudgetController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @Operation(summary = "Set or update budget", description = "Creates or updates a budget for a category and month using format {categoryId}_{yyyy-MM}.")
    public ResponseEntity<Budget> setBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BudgetRequest request) {
        Budget budget = expenseService.setBudget(principal.getUid(), request);
        return new ResponseEntity<>(budget, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List budgets", description = "Lists budgets for the authenticated user, optionally filtered by month (yyyy-MM).")
    public ResponseEntity<List<Budget>> getBudgets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month) {
        List<Budget> budgets = expenseService.getBudgets(principal.getUid(), month);
        return ResponseEntity.ok(budgets);
    }
}
