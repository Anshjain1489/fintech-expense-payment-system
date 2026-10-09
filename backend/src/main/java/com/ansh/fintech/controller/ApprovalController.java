package com.ansh.fintech.controller;

import com.ansh.fintech.dto.ApprovalDecisionRequest;
import com.ansh.fintech.dto.ApprovalRequest;
import com.ansh.fintech.model.ApprovalRecord;
import com.ansh.fintech.security.UserPrincipal;
import com.ansh.fintech.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approvals")
@Tag(name = "Approvals", description = "Endpoints for employee claim submission and manager approval/rejection workflow")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping
    @Operation(summary = "Submit expense claim for approval", description = "Employee submits an expense claim creating a pending approval request and notification.")
    public ResponseEntity<ApprovalRecord> submitApproval(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApprovalRequest request) {
        ApprovalRecord record = approvalService.submitApproval(principal.getUid(), request);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Approve or reject expense claim", description = "Manager or Accountant approves/rejects an expense claim with optional comments.")
    public ResponseEntity<ApprovalRecord> decideApproval(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ApprovalDecisionRequest request) {
        ApprovalRecord updatedRecord = approvalService.decideApproval(principal.getUid(), id, request);
        return ResponseEntity.ok(updatedRecord);
    }

    @GetMapping
    @Operation(summary = "List approval requests", description = "Lists pending and processed expense approval requests.")
    public ResponseEntity<List<ApprovalRecord>> getApprovals() {
        List<ApprovalRecord> approvals = approvalService.getApprovals();
        return ResponseEntity.ok(approvals);
    }
}
