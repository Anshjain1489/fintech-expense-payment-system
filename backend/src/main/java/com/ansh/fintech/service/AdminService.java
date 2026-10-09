package com.ansh.fintech.service;

import com.ansh.fintech.model.AuditLog;
import com.ansh.fintech.model.TransactionRecord;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final Firestore firestore;

    public AdminService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Lists all system transactions across all users.
     */
    public List<TransactionRecord> getAllTransactions() {
        CollectionReference txRef = firestore.collection("transactions");
        try {
            var docs = txRef.get().get().getDocuments();
            List<TransactionRecord> list = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                list.add(TransactionRecord.fromMap(doc.getId(), doc.getData()));
            }
            list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            return list;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching admin transactions: ", e);
            throw new RuntimeException("Failed to fetch all transactions: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all registered user accounts from users collection.
     */
    public List<Map<String, Object>> getAllUsers() {
        CollectionReference usersRef = firestore.collection("users");
        try {
            var docs = usersRef.get().get().getDocuments();
            List<Map<String, Object>> users = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                Map<String, Object> map = new HashMap<>(doc.getData());
                map.put("uid", doc.getId());
                users.add(map);
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching admin users: ", e);
            throw new RuntimeException("Failed to fetch all users: " + e.getMessage(), e);
        }
    }

    /**
     * Lists system audit logs from auditLogs collection.
     */
    public List<AuditLog> getAllAuditLogs() {
        CollectionReference auditRef = firestore.collection("auditLogs");
        try {
            var docs = auditRef.get().get().getDocuments();
            List<AuditLog> list = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                list.add(AuditLog.fromMap(doc.getId(), doc.getData()));
            }
            list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            return list;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching audit logs: ", e);
            throw new RuntimeException("Failed to fetch audit logs: " + e.getMessage(), e);
        }
    }
}
