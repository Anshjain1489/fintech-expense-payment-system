package com.ansh.fintech.service;

import com.ansh.fintech.model.AuditLog;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final Firestore firestore;

    public AuditService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Writes an audit log entry inside an active Firestore transaction.
     */
    public String logActionInTransaction(Transaction transaction, String userId, String action, String entityId, String details) {
        String logId = UUID.randomUUID().toString();
        DocumentReference auditRef = firestore.collection("auditLogs").document(logId);

        AuditLog auditLog = new AuditLog(
                logId,
                userId,
                action,
                entityId,
                details,
                System.currentTimeMillis()
        );

        transaction.set(auditRef, auditLog.toMap());
        log.info("Audit log queued in transaction: userId={}, action={}, entityId={}", userId, action, entityId);
        return logId;
    }
}
