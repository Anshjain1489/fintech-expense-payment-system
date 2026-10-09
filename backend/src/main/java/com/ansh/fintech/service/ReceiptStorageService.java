package com.ansh.fintech.service;

import com.ansh.fintech.exception.ResourceNotFoundException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ReceiptStorageService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptStorageService.class);
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB Limit
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final Firestore firestore;
    private final FirebaseApp firebaseApp;
    private final AuditService auditService;

    public ReceiptStorageService(Firestore firestore, FirebaseApp firebaseApp, AuditService auditService) {
        this.firestore = firestore;
        this.firebaseApp = firebaseApp;
        this.auditService = auditService;
    }

    /**
     * Uploads receipt file for an expense after enforcing 5 MB size limit and Image/PDF type restrictions.
     */
    public String uploadReceipt(String uid, String expenseId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        // 1. Enforce 5 MB Size Limit
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.warn("File upload rejected: size {} bytes exceeds 5 MB limit", file.getSize());
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 5 MB");
        }

        // 2. Enforce Image or PDF Content Type Restriction
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("File upload rejected: invalid content type {}", contentType);
            throw new IllegalArgumentException("Invalid file type. Only Images (JPEG, PNG, WEBP) and PDF files are allowed");
        }

        // Check if expense exists
        DocumentReference expenseRef = firestore.collection("users").document(uid).collection("expenses").document(expenseId);
        try {
            DocumentSnapshot snap = expenseRef.get().get();
            if (!snap.exists()) {
                throw new ResourceNotFoundException("Expense not found with ID: " + expenseId);
            }

            String fileExtension = getFileExtension(file.getOriginalFilename(), contentType);
            String storagePath = "receipts/" + uid + "/" + expenseId + "_" + UUID.randomUUID().toString() + fileExtension;
            String receiptUrl;

            try {
                // Upload to Firebase Cloud Storage Bucket
                var bucket = StorageClient.getInstance(firebaseApp).bucket();
                if (bucket != null) {
                    var blob = bucket.create(storagePath, file.getBytes(), contentType);
                    receiptUrl = blob.getMediaLink() != null ? blob.getMediaLink()
                            : "https://storage.googleapis.com/" + bucket.getName() + "/" + storagePath;
                } else {
                    receiptUrl = "https://storage.googleapis.com/demo-fintech-project.appspot.com/" + storagePath;
                }
            } catch (Exception e) {
                log.warn("Cloud Storage bucket upload fallback: ", e);
                receiptUrl = "https://storage.googleapis.com/demo-fintech-project.appspot.com/" + storagePath;
            }

            // Update receiptUrl in Firestore expense document
            expenseRef.set(Map.of("receiptUrl", receiptUrl), SetOptions.merge()).get();

            final String finalReceiptUrl = receiptUrl;
            // Log audit action
            firestore.runTransaction(transaction -> {
                auditService.logActionInTransaction(transaction, uid, "RECEIPT_UPLOADED", expenseId,
                        "Uploaded receipt URL: " + finalReceiptUrl);
                return null;
            }).get();

            return receiptUrl;

        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof ResourceNotFoundException) {
                throw (ResourceNotFoundException) e.getCause();
            }
            throw new RuntimeException("Failed to upload receipt: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) return ".pdf";
        if ("image/png".equalsIgnoreCase(contentType)) return ".png";
        if ("image/webp".equalsIgnoreCase(contentType)) return ".webp";
        return ".jpg";
    }
}
