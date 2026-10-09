package com.ansh.fintech.service;

import com.ansh.fintech.dto.CreateOrderRequest;
import com.ansh.fintech.dto.PaymentOrderResponse;
import com.ansh.fintech.dto.PaymentVerifyRequest;
import com.ansh.fintech.dto.PaymentVerifyResponse;
import com.ansh.fintech.exception.ResourceNotFoundException;
import com.ansh.fintech.exception.UnauthorizedAccessException;
import com.ansh.fintech.model.TransactionRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Firestore firestore;
    private final AuditService auditService;
    private final RazorpaySignatureService signatureService;

    @Value("${razorpay.key-id:rzp_test_dummyKeyId}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:dummySecret}")
    private String razorpayKeySecret;

    public PaymentService(Firestore firestore, AuditService auditService, RazorpaySignatureService signatureService) {
        this.firestore = firestore;
        this.auditService = auditService;
        this.signatureService = signatureService;
    }

    /**
     * Create a Razorpay payment order idempotently using transaction doc ID = uid + "_" + idempotencyKey.
     * Uses docRef.create(...) so duplicate writes fail atomically. Retried request returns the existing order.
     */
    public PaymentOrderResponse createOrder(String uid, String idempotencyKey, CreateOrderRequest request) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency-Key header is required for payment order creation");
        }

        String docId = TransactionRecord.buildId(uid, idempotencyKey);
        DocumentReference txRef = firestore.collection("transactions").document(docId);

        try {
            // Check if transaction already exists (Idempotent check)
            DocumentSnapshot existingSnap = txRef.get().get();
            if (existingSnap.exists()) {
                log.info("Idempotent createOrder request detected for key {}. Returning existing order.", idempotencyKey);
                TransactionRecord existingTx = TransactionRecord.fromMap(docId, existingSnap.getData());
                return new PaymentOrderResponse(
                        existingTx.getRazorpayOrderId(),
                        existingTx.getAmountPaise(),
                        existingTx.getCurrency(),
                        idempotencyKey,
                        existingTx.getStatus()
                );
            }

            // Generate Razorpay Order
            String razorpayOrderId = createRazorpayOrder(request.getAmountPaise(), request.getCurrency(), docId);

            TransactionRecord record = new TransactionRecord(
                    docId,
                    uid,
                    request.getAmountPaise(),
                    request.getCurrency(),
                    "created",
                    razorpayOrderId,
                    null,
                    idempotencyKey,
                    System.currentTimeMillis(),
                    null
            );

            // Use txRef.create(...) so duplicates fail atomically at Firestore level
            txRef.create(record.toMap()).get();

            // Log audit trail
            firestore.runTransaction(transaction -> {
                auditService.logActionInTransaction(transaction, uid, "PAYMENT_ORDER_CREATED", docId,
                        "Created payment order id=" + razorpayOrderId + " amountPaise=" + request.getAmountPaise());
                return null;
            }).get();

            return new PaymentOrderResponse(
                    razorpayOrderId,
                    request.getAmountPaise(),
                    request.getCurrency(),
                    idempotencyKey,
                    "created"
            );

        } catch (ExecutionException e) {
            if (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("ALREADY_EXISTS")) {
                log.warn("Atomic duplicate createOrder race condition caught for docId {}. Returning existing order.", docId);
                return createOrder(uid, idempotencyKey, request);
            }
            throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment order creation interrupted", e);
        }
    }

    /**
     * Server-side signature verification & order ownership validation.
     */
    public PaymentVerifyResponse verifyPayment(String uid, PaymentVerifyRequest request) {
        boolean isValidSig = signatureService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValidSig) {
            log.error("Payment signature verification failed for orderId={}", request.getRazorpayOrderId());
            throw new IllegalArgumentException("Invalid Razorpay payment signature");
        }

        // Find transaction doc by razorpayOrderId
        TransactionRecord txRecord = findTransactionByOrderId(request.getRazorpayOrderId());
        if (txRecord == null) {
            throw new ResourceNotFoundException("Transaction record not found for Razorpay order ID: " + request.getRazorpayOrderId());
        }

        // Verify order belongs to the calling user
        if (!txRecord.getUserId().equals(uid)) {
            log.error("Unauthorized payment verification attempt: order userId={} does not match calling uid={}", txRecord.getUserId(), uid);
            throw new UnauthorizedAccessException("Payment order does not belong to the calling user");
        }

        // Mark paid inside transaction
        markPaidInTransaction(txRecord.getId(), request.getRazorpayPaymentId(), txRecord.getAmountPaise());

        return new PaymentVerifyResponse(true, "Payment verified successfully", "paid");
    }

    /**
     * Webhook processing for payment.captured and payment.failed events.
     * Validates X-Razorpay-Signature and executes idempotent status transitions inside a transaction.
     */
    public void processWebhook(String payload, String signature) {
        boolean isValidSig = signatureService.verifyWebhookSignature(payload, signature);
        if (!isValidSig) {
            log.error("Webhook signature verification failed");
            throw new IllegalArgumentException("Invalid Razorpay webhook signature");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.optString("event");

        JSONObject payloadObj = json.optJSONObject("payload");
        if (payloadObj == null) return;

        JSONObject paymentObj = payloadObj.optJSONObject("payment");
        if (paymentObj == null) return;

        JSONObject entityObj = paymentObj.optJSONObject("entity");
        if (entityObj == null) entityObj = paymentObj;

        String razorpayOrderId = entityObj.optString("order_id");
        String razorpayPaymentId = entityObj.optString("id");
        long receivedAmountPaise = entityObj.optLong("amount", 0L);

        TransactionRecord record = findTransactionByOrderId(razorpayOrderId);
        if (record == null) {
            log.warn("Webhook received for unknown Razorpay order ID: {}", razorpayOrderId);
            return;
        }

        if ("payment.captured".equals(event)) {
            markPaidInTransaction(record.getId(), razorpayPaymentId, receivedAmountPaise);
        } else if ("payment.failed".equals(event)) {
            markFailedInTransaction(record.getId(), razorpayPaymentId);
        }
    }

    /**
     * Atomically marks a transaction as paid inside a Firestore transaction.
     * Idempotent: Same webhook twice = paid once. Checks for amount mismatch.
     */
    public void markPaidInTransaction(String docId, String razorpayPaymentId, long receivedAmountPaise) {
        DocumentReference txRef = firestore.collection("transactions").document(docId);

        ApiFuture<Void> future = firestore.runTransaction(transaction -> {
            // --- ALL READS FIRST ---
            DocumentSnapshot snap = transaction.get(txRef).get();
            if (!snap.exists()) {
                throw new ResourceNotFoundException("Transaction record not found: " + docId);
            }

            TransactionRecord record = TransactionRecord.fromMap(docId, snap.getData());

            // Idempotent check: same webhook twice = paid once!
            if ("paid".equalsIgnoreCase(record.getStatus())) {
                log.info("Transaction {} is already marked paid. Skipping duplicate webhook execution.", docId);
                return null;
            }

            // Amount mismatch check
            if (record.getAmountPaise() != receivedAmountPaise) {
                log.error("Amount mismatch for tx {}: expected {} paise, received {} paise",
                        docId, record.getAmountPaise(), receivedAmountPaise);

                transaction.set(txRef, Map.of(
                        "status", "amount_mismatch",
                        "razorpayPaymentId", razorpayPaymentId
                ), SetOptions.merge());

                auditService.logActionInTransaction(transaction, record.getUserId(), "PAYMENT_AMOUNT_MISMATCH", docId,
                        "Expected " + record.getAmountPaise() + " paise, received " + receivedAmountPaise + " paise");
                return null;
            }

            // --- ALL WRITES AFTER READS ---
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "paid");
            updates.put("razorpayPaymentId", razorpayPaymentId);
            updates.put("paidAt", System.currentTimeMillis());
            transaction.set(txRef, updates, SetOptions.merge());

            auditService.logActionInTransaction(transaction, record.getUserId(), "PAYMENT_CAPTURED", docId,
                    "Payment captured amountPaise=" + receivedAmountPaise + " paymentId=" + razorpayPaymentId);

            return null;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating transaction paid status: ", e);
            throw new RuntimeException("Failed to mark transaction as paid: " + e.getMessage(), e);
        }
    }

    private void markFailedInTransaction(String docId, String razorpayPaymentId) {
        DocumentReference txRef = firestore.collection("transactions").document(docId);

        ApiFuture<Void> future = firestore.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(txRef).get();
            if (!snap.exists()) return null;

            TransactionRecord record = TransactionRecord.fromMap(docId, snap.getData());

            transaction.set(txRef, Map.of(
                    "status", "failed",
                    "razorpayPaymentId", razorpayPaymentId != null ? razorpayPaymentId : ""
            ), SetOptions.merge());

            auditService.logActionInTransaction(transaction, record.getUserId(), "PAYMENT_FAILED", docId,
                    "Payment failed for orderId=" + record.getRazorpayOrderId());
            return null;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error marking transaction failed: ", e);
        }
    }

    public List<TransactionRecord> getTransactions(String uid) {
        CollectionReference txRef = firestore.collection("transactions");
        Query query = txRef.whereEqualTo("userId", uid);

        try {
            QuerySnapshot snapshot = query.get().get();
            List<TransactionRecord> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                list.add(TransactionRecord.fromMap(doc.getId(), doc.getData()));
            }
            list.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to list user transactions: " + e.getMessage(), e);
        }
    }

    private TransactionRecord findTransactionByOrderId(String orderId) {
        CollectionReference txRef = firestore.collection("transactions");
        try {
            QuerySnapshot snapshot = txRef.whereEqualTo("razorpayOrderId", orderId).get().get();
            if (snapshot.isEmpty()) return null;
            DocumentSnapshot doc = snapshot.getDocuments().get(0);
            return TransactionRecord.fromMap(doc.getId(), doc.getData());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error searching transaction by order ID {}: ", orderId, e);
            return null;
        }
    }

    private String createRazorpayOrder(long amountPaise, String currency, String docId) {
        // If dummy key is used in development/test environment
        if (razorpayKeyId == null || razorpayKeyId.startsWith("rzp_test_dummy")) {
            return "order_mock_" + UUID.randomUUID().toString().substring(0, 8);
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountPaise); // Amount in paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", docId);
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            log.error("Error generating Razorpay Order: ", e);
            throw new RuntimeException("Razorpay client failed to create order: " + e.getMessage(), e);
        }
    }
}
