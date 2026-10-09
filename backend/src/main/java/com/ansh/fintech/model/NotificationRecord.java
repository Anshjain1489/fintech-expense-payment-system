package com.ansh.fintech.model;

import java.util.HashMap;
import java.util.Map;

public class NotificationRecord {
    private String id;
    private String userId;
    private String message;
    private boolean read;
    private long createdAt;

    public NotificationRecord() {}

    public NotificationRecord(String id, String userId, String message, boolean read, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("message", message);
        map.put("read", read);
        map.put("createdAt", createdAt);
        return map;
    }

    public static NotificationRecord fromMap(String id, Map<String, Object> map) {
        NotificationRecord record = new NotificationRecord();
        record.setId(id);
        record.setUserId((String) map.get("userId"));
        record.setMessage((String) map.get("message"));
        if (map.get("read") != null) {
            record.setRead((Boolean) map.get("read"));
        }
        if (map.get("createdAt") != null) {
            record.setCreatedAt(((Number) map.get("createdAt")).longValue());
        }
        return record;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
