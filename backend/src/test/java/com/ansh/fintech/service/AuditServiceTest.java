package com.ansh.fintech.service;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    private Firestore firestore;
    private Transaction transaction;
    private CollectionReference collectionReference;
    private DocumentReference documentReference;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        transaction = mock(Transaction.class);
        collectionReference = mock(CollectionReference.class);
        documentReference = mock(DocumentReference.class);

        when(firestore.collection("auditLogs")).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);

        auditService = new AuditService(firestore);
    }

    @Test
    void testLogActionInTransactionQueuesAuditLog() {
        String logId = auditService.logActionInTransaction(
                transaction,
                "user-123",
                "EXPENSE_CREATED",
                "exp-456",
                "Created test expense"
        );

        assertNotNull(logId);
        verify(firestore).collection("auditLogs");
        verify(transaction).set(eq(documentReference), anyMap());
    }
}
