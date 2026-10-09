package com.ansh.fintech.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "Amount in paise is required")
    @Min(value = 1, message = "Amount in paise must be at least 1")
    private Long amountPaise;

    private String currency = "INR";

    public CreateOrderRequest() {}

    public CreateOrderRequest(Long amountPaise, String currency) {
        this.amountPaise = amountPaise;
        if (currency != null && !currency.trim().isEmpty()) {
            this.currency = currency;
        }
    }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
