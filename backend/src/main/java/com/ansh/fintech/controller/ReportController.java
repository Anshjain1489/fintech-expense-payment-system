package com.ansh.fintech.controller;

import com.ansh.fintech.dto.MonthlyReportResponse;
import com.ansh.fintech.security.UserPrincipal;
import com.ansh.fintech.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Endpoints for financial spending analytics, category reports, and CSV export")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly spending report", description = "Returns aggregated monthly spending metrics and category totals.")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month) {
        MonthlyReportResponse report = reportService.getMonthlyReport(principal.getUid(), month);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category")
    @Operation(summary = "Get category breakdown report", description = "Returns total spending per category in paise.")
    public ResponseEntity<Map<String, Long>> getCategoryReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month) {
        Map<String, Long> report = reportService.getCategoryReport(principal.getUid(), month);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    @Operation(summary = "Export expenses to CSV", description = "Generates and downloads a clean CSV file export of user expense records.")
    public ResponseEntity<String> exportCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String month) {
        String csvData = reportService.exportExpensesCsv(principal.getUid(), month);
        String filename = "expenses-export" + (month != null ? "-" + month : "") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
