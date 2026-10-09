package com.ansh.fintech.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class RazorpaySignatureService {

    private static final Logger log = LoggerFactory.getLogger(RazorpaySignatureService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    @Value("${razorpay.key-secret:dummySecret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook-secret:dummyWebhookSecret}")
    private String razorpayWebhookSecret;

    /**
     * Verifies payment signature sent from frontend checkout.
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) {
            return false;
        }
        String payload = orderId + "|" + paymentId;
        String expectedSignature = calculateHmacSha256(payload, razorpayKeySecret);
        return expectedSignature.equalsIgnoreCase(signature);
    }

    /**
     * Verifies webhook signature sent in X-Razorpay-Signature header.
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (payload == null || signature == null) {
            return false;
        }
        String expectedSignature = calculateHmacSha256(payload, razorpayWebhookSecret);
        return expectedSignature.equalsIgnoreCase(signature);
    }

    public String calculateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error calculating HMAC SHA256 signature: ", e);
            throw new RuntimeException("Signature calculation failure", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
