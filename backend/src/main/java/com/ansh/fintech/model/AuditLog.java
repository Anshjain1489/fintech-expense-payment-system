package com.ansh.fintech.model;

import java.util.HashMap;
import java.util.Map;

public class AuditLog {
    private String id;
    private String userId;
    private String action;
    private String entityId;
    private String details;
    private long timestamp;

    public AuditLog() {}

    public AuditLog(String id, String userId, String action, String entityId, String details, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("action", action);
        map.put("entityId", entityId);
        map.put("details", details);
        map.put("timestamp", timestamp);
        return map;
    }

    public static AuditLog fromMap(String id, Map<String, Object> map) {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(id);
        auditLog.setUserId((String) map.get("userId"));
        auditLog.setAction((String) map.get("action"));
        auditLog.setEntityId((String) map.get("entityId"));
        auditLog.setDetails((String) map.get("details"));
        if (map.get("timestamp") != null) {
            auditLog.setTimestamp(((Number) map.get("timestamp")).longValue());
        }
        return auditLog;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
