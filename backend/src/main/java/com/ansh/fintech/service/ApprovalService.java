package com.ansh.fintech.service;

import com.ansh.fintech.dto.ApprovalDecisionRequest;
import com.ansh.fintech.dto.ApprovalRequest;
import com.ansh.fintech.exception.ResourceNotFoundException;
import com.ansh.fintech.model.ApprovalRecord;
import com.ansh.fintech.model.Expense;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    private final Firestore firestore;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ApprovalService(Firestore firestore, NotificationService notificationService, AuditService auditService) {
        this.firestore = firestore;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Employee submits an expense claim for manager approval.
     */
    public ApprovalRecord submitApproval(String callingUid, ApprovalRequest request) {
        String approvalId = UUID.randomUUID().toString();
        DocumentReference approvalRef = firestore.collection("approvals").document(approvalId);
        DocumentReference expenseRef = firestore.collection("users").document(callingUid)
                .collection("expenses").document(request.getExpenseId());

        ApiFuture<ApprovalRecord> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot expenseSnap = transaction.get(expenseRef).get();
            if (!expenseSnap.exists()) {
                throw new ResourceNotFoundException("Expense not found: " + request.getExpenseId());
            }

            Expense expense = Expense.fromMap(request.getExpenseId(), expenseSnap.getData());

            ApprovalRecord record = new ApprovalRecord(
                    approvalId,
                    request.getExpenseId(),
                    callingUid,
                    null,
                    "pending",
                    request.getComment(),
                    System.currentTimeMillis()
            );

            // --- ALL WRITES AFTER READS ---
            transaction.set(approvalRef, record.toMap());
            transaction.set(expenseRef, Map.of("status", "pending"), SetOptions.merge());

            notificationService.sendNotificationInTransaction(
                    transaction,
                    callingUid,
                    "Expense claim for ₹" + (expense.getAmountPaise() / 100.0) + " submitted for approval."
            );

            auditService.logActionInTransaction(
                    transaction,
                    callingUid,
                    "APPROVAL_REQUESTED",
                    approvalId,
                    "Submitted expense claim expenseId=" + request.getExpenseId()
            );

            return record;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e.getCause();
            }
            throw new RuntimeException("Failed to submit approval request: " + e.getMessage(), e);
        }
    }

    /**
     * Manager or Accountant approves or rejects an expense claim.
     */
    public ApprovalRecord decideApproval(String managerUid, String approvalId, ApprovalDecisionRequest request) {
        DocumentReference approvalRef = firestore.collection("approvals").document(approvalId);

        ApiFuture<ApprovalRecord> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot approvalSnap = transaction.get(approvalRef).get();
            if (!approvalSnap.exists()) {
                throw new ResourceNotFoundException("Approval record not found: " + approvalId);
            }

            ApprovalRecord approval = ApprovalRecord.fromMap(approvalId, approvalSnap.getData());
            DocumentReference expenseRef = firestore.collection("users").document(approval.getRequestedBy())
                    .collection("expenses").document(approval.getExpenseId());

            DocumentSnapshot expenseSnap = transaction.get(expenseRef).get();
            long amountPaise = 0L;
            if (expenseSnap.exists() && expenseSnap.getLong("amountPaise") != null) {
                amountPaise = expenseSnap.getLong("amountPaise");
            }

            // --- ALL WRITES AFTER READS ---
            Map<String, Object> approvalUpdates = new HashMap<>();
            approvalUpdates.put("status", request.getStatus());
            approvalUpdates.put("approverId", managerUid);
            approvalUpdates.put("comment", request.getComment());
            transaction.set(approvalRef, approvalUpdates, SetOptions.merge());

            if (expenseSnap.exists()) {
                transaction.set(expenseRef, Map.of("status", request.getStatus()), SetOptions.merge());
            }

            notificationService.sendNotificationInTransaction(
                    transaction,
                    approval.getRequestedBy(),
                    "Your expense claim for ₹" + (amountPaise / 100.0) + " has been " + request.getStatus().toUpperCase()
                            + (request.getComment() != null ? ": " + request.getComment() : ".")
            );

            auditService.logActionInTransaction(
                    transaction,
                    managerUid,
                    "APPROVAL_" + request.getStatus().toUpperCase(),
                    approvalId,
                    "Decision: " + request.getStatus() + " by managerId=" + managerUid
            );

            approval.setStatus(request.getStatus());
            approval.setApproverId(managerUid);
            approval.setComment(request.getComment());
            return approval;
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e.getCause();
            }
            throw new RuntimeException("Failed to decide approval: " + e.getMessage(), e);
        }
    }

    public List<ApprovalRecord> getApprovals() {
        CollectionReference approvalsRef = firestore.collection("approvals");
        try {
            QuerySnapshot snapshot = approvalsRef.get().get();
            List<ApprovalRecord> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                list.add(ApprovalRecord.fromMap(doc.getId(), doc.getData()));
            }
            list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to list approvals: " + e.getMessage(), e);
        }
    }
}
