package com.ansh.fintech.dto;

import jakarta.validation.constraints.NotBlank;

public class ApprovalRequest {

    @NotBlank(message = "Expense ID is required")
    private String expenseId;

    private String comment;

    public ApprovalRequest() {}

    public ApprovalRequest(String expenseId, String comment) {
        this.expenseId = expenseId;
        this.comment = comment;
    }

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
