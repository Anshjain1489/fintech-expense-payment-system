package com.ansh.fintech.dto;

import java.time.LocalDateTime;

public class HealthResponse {
    private final String status;
    private final String service;
    private final LocalDateTime timestamp;

    public HealthResponse(String status, String service) {
        this.status = status;
        this.service = service;
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public String getService() {
        return service;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
