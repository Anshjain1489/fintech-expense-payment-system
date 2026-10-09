package com.ansh.fintech.model;

import java.util.HashMap;
import java.util.Map;

public class Expense {
    private String id;
    private long amountPaise;
    private String categoryId;
    private String accountId;
    private String note;
    private String date; // yyyy-MM-dd
    private String month; // yyyy-MM
    private String receiptUrl;
    private String status; // pending, approved, rejected
    private boolean recurring;
    private long createdAt;

    public Expense() {}

    public Expense(String id, long amountPaise, String categoryId, String accountId, String note,
                   String date, String month, String receiptUrl, String status, boolean recurring, long createdAt) {
        this.id = id;
        this.amountPaise = amountPaise;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.note = note;
        this.date = date;
        this.month = month;
        this.receiptUrl = receiptUrl;
        this.status = status;
        this.recurring = recurring;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("amountPaise", amountPaise);
        map.put("categoryId", categoryId);
        map.put("accountId", accountId);
        map.put("note", note);
        map.put("date", date);
        map.put("month", month);
        map.put("receiptUrl", receiptUrl);
        map.put("status", status);
        map.put("recurring", recurring);
        map.put("createdAt", createdAt);
        return map;
    }

    public static Expense fromMap(String id, Map<String, Object> map) {
        Expense expense = new Expense();
        expense.setId(id);
        if (map.get("amountPaise") != null) {
            expense.setAmountPaise(((Number) map.get("amountPaise")).longValue());
        }
        expense.setCategoryId((String) map.get("categoryId"));
        expense.setAccountId((String) map.get("accountId"));
        expense.setNote((String) map.get("note"));
        expense.setDate((String) map.get("date"));
        expense.setMonth((String) map.get("month"));
        expense.setReceiptUrl((String) map.get("receiptUrl"));
        expense.setStatus((String) map.get("status"));
        if (map.get("recurring") != null) {
            expense.setRecurring((Boolean) map.get("recurring"));
        }
        if (map.get("createdAt") != null) {
            expense.setCreatedAt(((Number) map.get("createdAt")).longValue());
        }
        return expense;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(long amountPaise) { this.amountPaise = amountPaise; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
