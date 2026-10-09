package com.ansh.fintech.dto;

public class PaymentOrderResponse {
    private String orderId;
    private Long amountPaise;
    private String currency;
    private String idempotencyKey;
    private String status;

    public PaymentOrderResponse() {}

    public PaymentOrderResponse(String orderId, Long amountPaise, String currency, String idempotencyKey, String status) {
        this.orderId = orderId;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
