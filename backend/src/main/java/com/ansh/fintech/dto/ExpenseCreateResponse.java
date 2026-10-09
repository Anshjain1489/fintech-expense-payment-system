package com.ansh.fintech.dto;

import com.ansh.fintech.model.Expense;

public class ExpenseCreateResponse {
    private String id;
    private boolean overBudget;
    private Expense expense;

    public ExpenseCreateResponse() {}

    public ExpenseCreateResponse(String id, boolean overBudget, Expense expense) {
        this.id = id;
        this.overBudget = overBudget;
        this.expense = expense;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isOverBudget() { return overBudget; }
    public void setOverBudget(boolean overBudget) { this.overBudget = overBudget; }

    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }
}
