package com.ansh.fintech.service;

import com.ansh.fintech.dto.MonthlyReportResponse;
import com.ansh.fintech.model.Expense;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private final Firestore firestore;

    public ReportService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Aggregates monthly total spending and category breakdown.
     */
    public MonthlyReportResponse getMonthlyReport(String uid, String month) {
        List<Expense> expenses = fetchExpenses(uid, month);

        long totalPaise = 0L;
        Map<String, Long> categoryMap = new HashMap<>();

        for (Expense exp : expenses) {
            totalPaise += exp.getAmountPaise();
            String cat = exp.getCategoryId() != null ? exp.getCategoryId() : "other";
            categoryMap.put(cat, categoryMap.getOrDefault(cat, 0L) + exp.getAmountPaise());
        }

        return new MonthlyReportResponse(
                month != null ? month : "ALL",
                totalPaise,
                expenses.size(),
                categoryMap
        );
    }

    /**
     * Aggregates category spending report across user expenses.
     */
    public Map<String, Long> getCategoryReport(String uid, String month) {
        List<Expense> expenses = fetchExpenses(uid, month);
        Map<String, Long> categoryMap = new HashMap<>();
        for (Expense exp : expenses) {
            String cat = exp.getCategoryId() != null ? exp.getCategoryId() : "other";
            categoryMap.put(cat, categoryMap.getOrDefault(cat, 0L) + exp.getAmountPaise());
        }
        return categoryMap;
    }

    /**
     * Generates clean CSV string formatted for expense export.
     */
    public String exportExpensesCsv(String uid, String month) {
        List<Expense> expenses = fetchExpenses(uid, month);

        StringBuilder csv = new StringBuilder();
        // CSV Header
        csv.append("ID,Date,Category,Account,Amount(Paise),Amount(INR),Status,Note\n");

        for (Expense exp : expenses) {
            double amountInr = exp.getAmountPaise() / 100.0;
            String noteSanitized = exp.getNote() != null ? exp.getNote().replace("\"", "\"\"") : "";

            csv.append("\"").append(exp.getId()).append("\",")
               .append("\"").append(exp.getDate() != null ? exp.getDate() : "").append("\",")
               .append("\"").append(exp.getCategoryId() != null ? exp.getCategoryId() : "").append("\",")
               .append("\"").append(exp.getAccountId() != null ? exp.getAccountId() : "").append("\",")
               .append(exp.getAmountPaise()).append(",")
               .append(String.format(Locale.US, "%.2f", amountInr)).append(",")
               .append("\"").append(exp.getStatus() != null ? exp.getStatus() : "").append("\",")
               .append("\"").append(noteSanitized).append("\"\n");
        }

        return csv.toString();
    }

    private List<Expense> fetchExpenses(String uid, String month) {
        CollectionReference expensesRef = firestore.collection("users").document(uid).collection("expenses");
        Query query = expensesRef;
        if (month != null && !month.trim().isEmpty()) {
            query = query.whereEqualTo("month", month);
        }

        try {
            var docs = query.get().get().getDocuments();
            List<Expense> expenses = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                expenses.add(Expense.fromMap(doc.getId(), doc.getData()));
            }
            expenses.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
            return expenses;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error fetching expenses for report: ", e);
            throw new RuntimeException("Failed to generate report data: " + e.getMessage(), e);
        }
    }
}
