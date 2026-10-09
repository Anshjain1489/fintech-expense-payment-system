package com.ansh.fintech.controller;

import com.ansh.fintech.dto.CreateOrderRequest;
import com.ansh.fintech.dto.PaymentOrderResponse;
import com.ansh.fintech.dto.PaymentVerifyRequest;
import com.ansh.fintech.dto.PaymentVerifyResponse;
import com.ansh.fintech.model.TransactionRecord;
import com.ansh.fintech.security.UserPrincipal;
import com.ansh.fintech.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints for Razorpay order generation, signature verification, and webhook handling")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay payment order", description = "Creates a Razorpay order idempotently using the mandatory Idempotency-Key header.")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {
        PaymentOrderResponse response = paymentService.createOrder(principal.getUid(), idempotencyKey, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature", description = "Verifies server-side signature and validates order ownership.")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PaymentVerifyRequest request) {
        PaymentVerifyResponse response = paymentService.verifyPayment(principal.getUid(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay Webhook Handler", description = "Public signature-protected webhook endpoint processing payment.captured and payment.failed events.")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload) {
        paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get user transactions", description = "Lists payment transaction history for the authenticated user.")
    public ResponseEntity<List<TransactionRecord>> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<TransactionRecord> transactions = paymentService.getTransactions(principal.getUid());
        return ResponseEntity.ok(transactions);
    }
}
