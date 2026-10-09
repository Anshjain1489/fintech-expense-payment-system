package com.ansh.fintech.controller;

import com.ansh.fintech.model.AuditLog;
import com.ansh.fintech.model.TransactionRecord;
import com.ansh.fintech.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Privileged administrator endpoints requiring ROLE_ADMIN authority")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all system transactions", description = "Lists payment transactions for all users in the system.")
    public ResponseEntity<List<TransactionRecord>> getAllTransactions() {
        List<TransactionRecord> transactions = adminService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/users")
    @Operation(summary = "Get all system users", description = "Lists registered user accounts in Firestore.")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system audit logs", description = "Lists system audit logs for financial actions.")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> auditLogs = adminService.getAllAuditLogs();
        return ResponseEntity.ok(auditLogs);
    }
}
