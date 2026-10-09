package com.ansh.fintech.service;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReceiptStorageServiceTest {

    private Firestore firestore;
    private FirebaseApp firebaseApp;
    private AuditService auditService;
    private ReceiptStorageService receiptStorageService;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        firebaseApp = mock(FirebaseApp.class);
        auditService = mock(AuditService.class);
        receiptStorageService = new ReceiptStorageService(firestore, firebaseApp, auditService);
    }

    @Test
    void testUploadReceiptRejectsFileExceeding5MB() {
        // 6 MB dummy byte array
        byte[] oversizedContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", oversizedContent);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                receiptStorageService.uploadReceipt("user-100", "exp-1", file)
        );

        assertTrue(ex.getMessage().contains("exceeds maximum allowed limit of 5 MB"));
    }

    @Test
    void testUploadReceiptRejectsInvalidFileType() {
        byte[] content = "some text content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "document.txt", "text/plain", content);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                receiptStorageService.uploadReceipt("user-100", "exp-1", file)
        );

        assertTrue(ex.getMessage().contains("Invalid file type"));
    }

    @Test
    void testUploadReceiptAcceptsPdfUnder5MB() throws Exception {
        byte[] validPdf = "%PDF-1.4 header test".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf", validPdf);

        CollectionReference usersRef = mock(CollectionReference.class);
        DocumentReference userDocRef = mock(DocumentReference.class);
        CollectionReference expensesRef = mock(CollectionReference.class);
        DocumentReference expenseDocRef = mock(DocumentReference.class);
        DocumentSnapshot snap = mock(DocumentSnapshot.class);

        when(firestore.collection("users")).thenReturn(usersRef);
        when(usersRef.document(anyString())).thenReturn(userDocRef);
        when(userDocRef.collection("expenses")).thenReturn(expensesRef);
        when(expensesRef.document(anyString())).thenReturn(expenseDocRef);

        when(snap.exists()).thenReturn(true);
        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureSnap.get()).thenReturn(snap);
        when(expenseDocRef.get()).thenReturn(futureSnap);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<com.google.cloud.firestore.WriteResult> futureWrite = mock(com.google.api.core.ApiFuture.class);
        when(expenseDocRef.set(anyMap(), any())).thenReturn(futureWrite);

        when(firestore.runTransaction(any())).thenAnswer(invocation -> {
            Object function = invocation.getArgument(0);
            try {
                java.lang.reflect.Method method = function.getClass().getMethods()[0];
                Object result = method.invoke(function, mock(com.google.cloud.firestore.Transaction.class));
                return com.google.api.core.ApiFutures.immediateFuture(result);
            } catch (Exception e) {
                return com.google.api.core.ApiFutures.immediateFailedFuture(e);
            }
        });

        String receiptUrl = receiptStorageService.uploadReceipt("user-100", "exp-1", file);
        assertNotNull(receiptUrl);
        assertTrue(receiptUrl.contains("invoice.pdf") || receiptUrl.contains("storage.googleapis.com"));
    }
}
