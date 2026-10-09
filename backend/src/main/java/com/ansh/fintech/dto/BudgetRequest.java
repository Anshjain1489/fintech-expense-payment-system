package com.ansh.fintech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BudgetRequest {

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotBlank(message = "Month is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Month must be in yyyy-MM format")
    private String month;

    @NotNull(message = "Limit in paise is required")
    @Min(value = 1, message = "Limit in paise must be at least 1")
    private Long limitPaise;

    public BudgetRequest() {}

    public BudgetRequest(String categoryId, String month, Long limitPaise) {
        this.categoryId = categoryId;
        this.month = month;
        this.limitPaise = limitPaise;
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Long getLimitPaise() { return limitPaise; }
    public void setLimitPaise(Long limitPaise) { this.limitPaise = limitPaise; }
}
