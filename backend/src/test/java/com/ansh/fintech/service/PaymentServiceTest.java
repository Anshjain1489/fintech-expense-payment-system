package com.ansh.fintech.service;

import com.ansh.fintech.dto.CreateOrderRequest;
import com.ansh.fintech.dto.PaymentOrderResponse;
import com.ansh.fintech.model.TransactionRecord;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private Firestore firestore;
    private AuditService auditService;
    private RazorpaySignatureService signatureService;
    private Transaction transaction;
    private PaymentService paymentService;

    private CollectionReference transactionsRef;
    private DocumentReference docRef;
    private DocumentSnapshot docSnap;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        auditService = mock(AuditService.class);
        signatureService = mock(RazorpaySignatureService.class);
        transaction = mock(Transaction.class);

        transactionsRef = mock(CollectionReference.class);
        docRef = mock(DocumentReference.class);
        docSnap = mock(DocumentSnapshot.class);

        when(firestore.collection("transactions")).thenReturn(transactionsRef);
        when(transactionsRef.document(anyString())).thenReturn(docRef);

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

        paymentService = new PaymentService(firestore, auditService, signatureService);
    }

    @Test
    void testDuplicateCreateOrderReturnsSameOrder() throws Exception {
        String uid = "user-100";
        String idempotencyKey = "key-abc-123";
        CreateOrderRequest request = new CreateOrderRequest(50000L, "INR");

        // Mock first call: doc does NOT exist
        DocumentSnapshot notExistSnap = mock(DocumentSnapshot.class);
        when(notExistSnap.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureNotExist = mock(com.google.api.core.ApiFuture.class);
        when(futureNotExist.get()).thenReturn(notExistSnap);
        when(docRef.get()).thenReturn(futureNotExist);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<WriteResult> futureWrite = mock(com.google.api.core.ApiFuture.class);
        when(futureWrite.get()).thenReturn(mock(WriteResult.class));
        when(docRef.create(anyMap())).thenReturn(futureWrite);

        PaymentOrderResponse response1 = paymentService.createOrder(uid, idempotencyKey, request);
        assertNotNull(response1);
        String firstOrderId = response1.getOrderId();
        assertNotNull(firstOrderId);

        // Mock second call: doc EXISTS with first order details
        DocumentSnapshot existSnap = mock(DocumentSnapshot.class);
        when(existSnap.exists()).thenReturn(true);
        Map<String, Object> data = new HashMap<>();
        data.put("id", TransactionRecord.buildId(uid, idempotencyKey));
        data.put("userId", uid);
        data.put("amountPaise", 50000L);
        data.put("currency", "INR");
        data.put("status", "created");
        data.put("razorpayOrderId", firstOrderId);
        data.put("idempotencyKey", idempotencyKey);
        when(existSnap.getData()).thenReturn(data);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureExist = mock(com.google.api.core.ApiFuture.class);
        when(futureExist.get()).thenReturn(existSnap);
        when(docRef.get()).thenReturn(futureExist);

        PaymentOrderResponse response2 = paymentService.createOrder(uid, idempotencyKey, request);

        // Retried request MUST return the exact same order
        assertEquals(firstOrderId, response2.getOrderId());
        assertEquals("created", response2.getStatus());
        assertEquals(50000L, response2.getAmountPaise());
        verify(docRef, times(1)).create(anyMap()); // create() called only ONCE!
    }

    @Test
    void testBadWebhookSignatureThrowsException() {
        String payload = "{\"event\": \"payment.captured\"}";
        String badSignature = "invalid_sig_xyz";

        when(signatureService.verifyWebhookSignature(payload, badSignature)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                paymentService.processWebhook(payload, badSignature)
        );
        assertEquals("Invalid Razorpay webhook signature", ex.getMessage());
    }

    @Test
    void testSameWebhookTwiceMarksPaidOnce() throws Exception {
        String payload = "{\n" +
                "  \"event\": \"payment.captured\",\n" +
                "  \"payload\": {\n" +
                "    \"payment\": {\n" +
                "      \"entity\": {\n" +
                "        \"id\": \"pay_123456\",\n" +
                "        \"order_id\": \"order_test_789\",\n" +
                "        \"amount\": 50000\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String validSignature = "valid_sig_123";
        when(signatureService.verifyWebhookSignature(payload, validSignature)).thenReturn(true);

        // Mock querying transaction by order ID
        Query query = mock(Query.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot txDocSnap = mock(QueryDocumentSnapshot.class);

        when(transactionsRef.whereEqualTo("razorpayOrderId", "order_test_789")).thenReturn(query);
        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<QuerySnapshot> futureQuery = mock(com.google.api.core.ApiFuture.class);
        when(futureQuery.get()).thenReturn(querySnapshot);
        when(query.get()).thenReturn(futureQuery);

        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.getDocuments()).thenReturn(List.of(txDocSnap));
        when(txDocSnap.getId()).thenReturn("user-100_key-1");
        when(txDocSnap.exists()).thenReturn(true);

        // FIRST WEBHOOK EXECUTION: status is "created"
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("id", "user-100_key-1");
        initialData.put("userId", "user-100");
        initialData.put("amountPaise", 50000L);
        initialData.put("status", "created");
        when(txDocSnap.getData()).thenReturn(initialData);

        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<DocumentSnapshot> futureTxSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureTxSnap.get()).thenReturn(txDocSnap);
        when(transaction.get(docRef)).thenReturn(futureTxSnap);

        // Process webhook 1st time
        paymentService.processWebhook(payload, validSignature);

        // Verify status update set in transaction
        verify(transaction, times(1)).set(eq(docRef), anyMap(), eq(SetOptions.merge()));

        // SECOND WEBHOOK EXECUTION: status is ALREADY "paid"
        Map<String, Object> paidData = new HashMap<>(initialData);
        paidData.put("status", "paid");
        paidData.put("razorpayPaymentId", "pay_123456");
        when(txDocSnap.getData()).thenReturn(paidData);

        // Process webhook 2nd time
        paymentService.processWebhook(payload, validSignature);

        // Verify transaction.set was NOT called again on 2nd execution (Idempotent: paid once!)
        verify(transaction, times(1)).set(eq(docRef), anyMap(), eq(SetOptions.merge()));
    }
}
