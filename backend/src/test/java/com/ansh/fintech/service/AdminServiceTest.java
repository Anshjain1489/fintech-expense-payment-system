package com.ansh.fintech.service;

import com.ansh.fintech.model.AuditLog;
import com.ansh.fintech.model.TransactionRecord;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private Firestore firestore;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        adminService = new AdminService(firestore);
    }

    @Test
    void testGetAllTransactionsReturnsMappedRecords() throws Exception {
        CollectionReference txRef = mock(CollectionReference.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);

        when(firestore.collection("transactions")).thenReturn(txRef);
        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<QuerySnapshot> futureSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureSnap.get()).thenReturn(snapshot);
        when(txRef.get()).thenReturn(futureSnap);

        Map<String, Object> data = new HashMap<>();
        data.put("id", "user-100_key-1");
        data.put("userId", "user-100");
        data.put("amountPaise", 50000L);
        data.put("status", "paid");
        data.put("createdAt", 1792000000000L);

        when(doc1.getId()).thenReturn("user-100_key-1");
        when(doc1.getData()).thenReturn(data);
        when(snapshot.getDocuments()).thenReturn(List.of(doc1));

        List<TransactionRecord> list = adminService.getAllTransactions();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("user-100_key-1", list.get(0).getId());
        assertEquals("paid", list.get(0).getStatus());
    }

    @Test
    void testGetAllAuditLogsReturnsMappedLogs() throws Exception {
        CollectionReference auditRef = mock(CollectionReference.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);

        when(firestore.collection("auditLogs")).thenReturn(auditRef);
        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<QuerySnapshot> futureSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureSnap.get()).thenReturn(snapshot);
        when(auditRef.get()).thenReturn(futureSnap);

        Map<String, Object> data = new HashMap<>();
        data.put("id", "log-1");
        data.put("userId", "user-100");
        data.put("action", "EXPENSE_CREATED");
        data.put("timestamp", 1792000000000L);

        when(doc1.getId()).thenReturn("log-1");
        when(doc1.getData()).thenReturn(data);
        when(snapshot.getDocuments()).thenReturn(List.of(doc1));

        List<AuditLog> list = adminService.getAllAuditLogs();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("EXPENSE_CREATED", list.get(0).getAction());
    }
}
