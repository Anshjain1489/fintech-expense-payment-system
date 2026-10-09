package com.ansh.fintech.dto;

import java.util.Map;

public class MonthlyReportResponse {
    private String month;
    private long totalAmountPaise;
    private int expenseCount;
    private Map<String, Long> categoryBreakdownPaise;

    public MonthlyReportResponse() {}

    public MonthlyReportResponse(String month, long totalAmountPaise, int expenseCount, Map<String, Long> categoryBreakdownPaise) {
        this.month = month;
        this.totalAmountPaise = totalAmountPaise;
        this.expenseCount = expenseCount;
        this.categoryBreakdownPaise = categoryBreakdownPaise;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public long getTotalAmountPaise() { return totalAmountPaise; }
    public void setTotalAmountPaise(long totalAmountPaise) { this.totalAmountPaise = totalAmountPaise; }

    public int getExpenseCount() { return expenseCount; }
    public void setExpenseCount(int expenseCount) { this.expenseCount = expenseCount; }

    public Map<String, Long> getCategoryBreakdownPaise() { return categoryBreakdownPaise; }
    public void setCategoryBreakdownPaise(Map<String, Long> categoryBreakdownPaise) { this.categoryBreakdownPaise = categoryBreakdownPaise; }
}
