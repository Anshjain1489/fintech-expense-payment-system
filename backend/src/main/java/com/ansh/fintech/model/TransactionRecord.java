package com.ansh.fintech.model;

import java.util.HashMap;
import java.util.Map;

public class TransactionRecord {
    private String id; // format: {userId}_{idempotencyKey}
    private String userId;
    private long amountPaise;
    private String currency;
    private String status; // created, paid, failed, amount_mismatch
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String idempotencyKey;
    private long createdAt;
    private Long paidAt;

    public TransactionRecord() {}

    public TransactionRecord(String id, String userId, long amountPaise, String currency,
                             String status, String razorpayOrderId, String razorpayPaymentId,
                             String idempotencyKey, long createdAt, Long paidAt) {
        this.id = id;
        this.userId = userId;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.status = status;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public static String buildId(String userId, String idempotencyKey) {
        return userId + "_" + idempotencyKey;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("amountPaise", amountPaise);
        map.put("currency", currency);
        map.put("status", status);
        map.put("razorpayOrderId", razorpayOrderId);
        map.put("razorpayPaymentId", razorpayPaymentId);
        map.put("idempotencyKey", idempotencyKey);
        map.put("createdAt", createdAt);
        if (paidAt != null) {
            map.put("paidAt", paidAt);
        }
        return map;
    }

    public static TransactionRecord fromMap(String id, Map<String, Object> map) {
        TransactionRecord record = new TransactionRecord();
        record.setId(id);
        record.setUserId((String) map.get("userId"));
        if (map.get("amountPaise") != null) {
            record.setAmountPaise(((Number) map.get("amountPaise")).longValue());
        }
        record.setCurrency((String) map.get("currency"));
        record.setStatus((String) map.get("status"));
        record.setRazorpayOrderId((String) map.get("razorpayOrderId"));
        record.setRazorpayPaymentId((String) map.get("razorpayPaymentId"));
        record.setIdempotencyKey((String) map.get("idempotencyKey"));
        if (map.get("createdAt") != null) {
            record.setCreatedAt(((Number) map.get("createdAt")).longValue());
        }
        if (map.get("paidAt") != null) {
            record.setPaidAt(((Number) map.get("paidAt")).longValue());
        }
        return record;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public Long getPaidAt() { return paidAt; }
    public void setPaidAt(Long paidAt) { this.paidAt = paidAt; }
}
