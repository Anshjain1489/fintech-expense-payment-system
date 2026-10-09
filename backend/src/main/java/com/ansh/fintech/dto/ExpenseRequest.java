package com.ansh.fintech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ExpenseRequest {

    @NotNull(message = "Amount in paise is required")
    @Min(value = 1, message = "Amount in paise must be at least 1")
    private Long amountPaise;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String accountId;

    private String note;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in yyyy-MM-dd format")
    private String date;

    private String receiptUrl;

    private Boolean recurring;

    public ExpenseRequest() {}

    public ExpenseRequest(Long amountPaise, String categoryId, String accountId, String note, String date, String receiptUrl, Boolean recurring) {
        this.amountPaise = amountPaise;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.note = note;
        this.date = date;
        this.receiptUrl = receiptUrl;
        this.recurring = recurring;
    }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public Boolean getRecurring() { return recurring; }
    public void setRecurring(Boolean recurring) { this.recurring = recurring; }
}
