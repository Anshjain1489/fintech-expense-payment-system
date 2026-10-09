package com.ansh.fintech.model;

import java.util.HashMap;
import java.util.Map;

public class Budget {
    private String id; // format: {categoryId}_{yyyy-MM}
    private String categoryId;
    private String month; // format: yyyy-MM
    private long limitPaise;
    private long spentPaise;

    public Budget() {}

    public Budget(String id, String categoryId, String month, long limitPaise, long spentPaise) {
        this.id = id;
        this.categoryId = categoryId;
        this.month = month;
        this.limitPaise = limitPaise;
        this.spentPaise = spentPaise;
    }

    public static String buildId(String categoryId, String month) {
        return categoryId + "_" + month;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("categoryId", categoryId);
        map.put("month", month);
        map.put("limitPaise", limitPaise);
        map.put("spentPaise", spentPaise);
        return map;
    }

    public static Budget fromMap(String id, Map<String, Object> map) {
        Budget budget = new Budget();
        budget.setId(id);
        budget.setCategoryId((String) map.get("categoryId"));
        budget.setMonth((String) map.get("month"));
        if (map.get("limitPaise") != null) {
            budget.setLimitPaise(((Number) map.get("limitPaise")).longValue());
        }
        if (map.get("spentPaise") != null) {
            budget.setSpentPaise(((Number) map.get("spentPaise")).longValue());
        }
        return budget;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public long getLimitPaise() { return limitPaise; }
    public void setLimitPaise(long limitPaise) { this.limitPaise = limitPaise; }

    public long getSpentPaise() { return spentPaise; }
    public void setSpentPaise(long spentPaise) { this.spentPaise = spentPaise; }
}
