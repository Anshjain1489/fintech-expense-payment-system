package com.ansh.fintech.model;

import java.util.HashMap;
import java.util.Map;

public class ApprovalRecord {
    private String id;
    private String expenseId;
    private String requestedBy;
    private String approverId;
    private String status; // pending, approved, rejected
    private String comment;
    private long createdAt;

    public ApprovalRecord() {}

    public ApprovalRecord(String id, String expenseId, String requestedBy, String approverId,
                          String status, String comment, long createdAt) {
        this.id = id;
        this.expenseId = expenseId;
        this.requestedBy = requestedBy;
        this.approverId = approverId;
        this.status = status;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("expenseId", expenseId);
        map.put("requestedBy", requestedBy);
        map.put("approverId", approverId);
        map.put("status", status);
        map.put("comment", comment);
        map.put("createdAt", createdAt);
        return map;
    }

    public static ApprovalRecord fromMap(String id, Map<String, Object> map) {
        ApprovalRecord record = new ApprovalRecord();
        record.setId(id);
        record.setExpenseId((String) map.get("expenseId"));
        record.setRequestedBy((String) map.get("requestedBy"));
        record.setApproverId((String) map.get("approverId"));
        record.setStatus((String) map.get("status"));
        record.setComment((String) map.get("comment"));
        if (map.get("createdAt") != null) {
            record.setCreatedAt(((Number) map.get("createdAt")).longValue());
        }
        return record;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
