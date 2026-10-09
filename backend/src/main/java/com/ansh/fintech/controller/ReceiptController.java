package com.ansh.fintech.controller;

import com.ansh.fintech.security.UserPrincipal;
import com.ansh.fintech.service.ReceiptStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Receipts", description = "Endpoints for uploading expense receipts to Cloud Storage")
public class ReceiptController {

    private final ReceiptStorageService receiptStorageService;

    public ReceiptController(ReceiptStorageService receiptStorageService) {
        this.receiptStorageService = receiptStorageService;
    }

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload receipt image or PDF", description = "Uploads receipt to Cloud Storage enforcing 5 MB limit and Image/PDF media restrictions.")
    public ResponseEntity<Map<String, String>> uploadReceipt(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        String receiptUrl = receiptStorageService.uploadReceipt(principal.getUid(), id, file);
        return ResponseEntity.ok(Map.of("expenseId", id, "receiptUrl", receiptUrl));
    }
}
