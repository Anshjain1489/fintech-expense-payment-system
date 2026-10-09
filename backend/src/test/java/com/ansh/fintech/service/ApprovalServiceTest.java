package com.ansh.fintech.service;

import com.ansh.fintech.dto.ApprovalDecisionRequest;
import com.ansh.fintech.dto.ApprovalRequest;
import com.ansh.fintech.model.ApprovalRecord;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApprovalServiceTest {

    private Firestore firestore;
    private NotificationService notificationService;
    private AuditService auditService;
    private Transaction transaction;
    private ApprovalService approvalService;

    private CollectionReference approvalsRef;
    private CollectionReference usersRef;
    private DocumentReference approvalDocRef;
    private DocumentReference expenseDocRef;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        notificationService = mock(NotificationService.class);
        auditService = mock(AuditService.class);
        transaction = mock(Transaction.class);

        approvalsRef = mock(CollectionReference.class);
        usersRef = mock(CollectionReference.class);
        approvalDocRef = mock(DocumentReference.class);
        expenseDocRef = mock(DocumentReference.class);

        when(firestore.collection("approvals")).thenReturn(approvalsRef);
        when(firestore.collection("users")).thenReturn(usersRef);
        when(approvalsRef.document(anyString())).thenReturn(approvalDocRef);

        DocumentReference userDocRef = mock(DocumentReference.class);
        CollectionReference expensesRef = mock(CollectionReference.class);
        when(usersRef.document(anyString())).thenReturn(userDocRef);
        when(userDocRef.collection("expenses")).thenReturn(expensesRef);
        when(expensesRef.document(anyString())).thenReturn(expenseDocRef);

        when(firestore.runTransaction(any())).thenAnswer(invocation -> {
            Object function = invocation.getArgument(0);
            try {
                java.lang.reflect.Method method = function.getClass().getMethods()[0];
                Object result = method.invoke(function, transaction);
                return ApiFutures.immediateFuture(result);
            } catch (Exception e) {
                Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException) ? e.getCause() : e;
                return ApiFutures.immediateFailedFuture(cause);
            }
        });

        approvalService = new ApprovalService(firestore, notificationService, auditService);
    }

    @Test
    void testSubmitApprovalCreatesPendingClaimAndNotification() throws Exception {
        String callingUid = "user-emp-1";
        ApprovalRequest request = new ApprovalRequest("exp-100", "Travel reimbursement claim");

        DocumentSnapshot expenseSnap = mock(DocumentSnapshot.class);
        when(expenseSnap.exists()).thenReturn(true);
        when(expenseSnap.getLong("amountPaise")).thenReturn(250000L);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureExpenseSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureExpenseSnap.get()).thenReturn(expenseSnap);
        when(transaction.get(expenseDocRef)).thenReturn(futureExpenseSnap);

        ApprovalRecord record = approvalService.submitApproval(callingUid, request);

        assertNotNull(record);
        assertEquals("pending", record.getStatus());
        assertEquals("exp-100", record.getExpenseId());
        assertEquals(callingUid, record.getRequestedBy());

        verify(transaction).set(eq(approvalDocRef), anyMap());
        verify(transaction).set(eq(expenseDocRef), eq(Map.of("status", "pending")), eq(SetOptions.merge()));
        verify(notificationService).sendNotificationInTransaction(eq(transaction), eq(callingUid), contains("submitted for approval"));
    }

    @Test
    void testDecideApprovalApprovedUpdatesStatusAndNotifiesEmployee() throws Exception {
        String managerUid = "manager-99";
        String approvalId = "app-55";
        ApprovalDecisionRequest decision = new ApprovalDecisionRequest("approved", "Verified receipt. Approved.");

        DocumentSnapshot approvalSnap = mock(DocumentSnapshot.class);
        when(approvalSnap.exists()).thenReturn(true);
        Map<String, Object> appData = new HashMap<>();
        appData.put("id", approvalId);
        appData.put("expenseId", "exp-100");
        appData.put("requestedBy", "user-emp-1");
        appData.put("status", "pending");
        when(approvalSnap.getData()).thenReturn(appData);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureAppSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureAppSnap.get()).thenReturn(approvalSnap);
        when(transaction.get(approvalDocRef)).thenReturn(futureAppSnap);

        DocumentSnapshot expenseSnap = mock(DocumentSnapshot.class);
        when(expenseSnap.exists()).thenReturn(true);
        when(expenseSnap.getLong("amountPaise")).thenReturn(250000L);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureExpenseSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureExpenseSnap.get()).thenReturn(expenseSnap);
        when(transaction.get(expenseDocRef)).thenReturn(futureExpenseSnap);

        ApprovalRecord result = approvalService.decideApproval(managerUid, approvalId, decision);

        assertNotNull(result);
        assertEquals("approved", result.getStatus());
        assertEquals(managerUid, result.getApproverId());

        verify(transaction).set(eq(expenseDocRef), eq(Map.of("status", "approved")), eq(SetOptions.merge()));
        verify(notificationService).sendNotificationInTransaction(eq(transaction), eq("user-emp-1"), contains("APPROVED"));
    }
}
