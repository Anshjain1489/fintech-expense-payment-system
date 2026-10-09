package com.ansh.fintech.controller;

import com.ansh.fintech.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Endpoints for API availability and system health status")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check backend health", description = "Public health check endpoint returning service status.")
    public ResponseEntity<HealthResponse> checkHealth() {
        return ResponseEntity.ok(new HealthResponse("UP", "FinTech Expense & Payment API Service"));
    }
}
