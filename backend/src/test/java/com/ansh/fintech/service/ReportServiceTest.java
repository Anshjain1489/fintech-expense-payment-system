package com.ansh.fintech.service;

import com.ansh.fintech.dto.MonthlyReportResponse;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private Firestore firestore;
    private ReportService reportService;

    private CollectionReference usersRef;
    private DocumentReference userDocRef;
    private CollectionReference expensesRef;
    private Query query;

    @BeforeEach
    void setUp() {
        firestore = mock(Firestore.class);
        usersRef = mock(CollectionReference.class);
        userDocRef = mock(DocumentReference.class);
        expensesRef = mock(CollectionReference.class);
        query = mock(Query.class);

        when(firestore.collection("users")).thenReturn(usersRef);
        when(usersRef.document(anyString())).thenReturn(userDocRef);
        when(userDocRef.collection("expenses")).thenReturn(expensesRef);
        when(expensesRef.whereEqualTo(anyString(), any())).thenReturn(query);

        reportService = new ReportService(firestore);
    }

    @Test
    void testExportExpensesCsvGeneratesValidHeadersAndData() throws Exception {
        String uid = "user-100";
        String month = "2026-10";

        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("id", "exp-001");
        data1.put("amountPaise", 150000L); // 1500.00 INR
        data1.put("categoryId", "groceries");
        data1.put("accountId", "bank_1");
        data1.put("date", "2026-10-10");
        data1.put("status", "approved");
        data1.put("note", "Supermarket items");
        data1.put("createdAt", 1792000000000L);

        when(doc1.getId()).thenReturn("exp-001");
        when(doc1.getData()).thenReturn(data1);

        when(snapshot.getDocuments()).thenReturn(List.of(doc1));
        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<QuerySnapshot> futureSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureSnap.get()).thenReturn(snapshot);
        when(query.get()).thenReturn(futureSnap);

        String csv = reportService.exportExpensesCsv(uid, month);

        assertNotNull(csv);
        assertTrue(csv.contains("ID,Date,Category,Account,Amount(Paise),Amount(INR),Status,Note"));
        assertTrue(csv.contains("\"exp-001\""));
        assertTrue(csv.contains("\"groceries\""));
        assertTrue(csv.contains("150000"));
        assertTrue(csv.contains("1500.00"));
    }

    @Test
    void testGetMonthlyReportAggregatesTotals() throws Exception {
        String uid = "user-100";
        String month = "2026-10";

        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot doc1 = mock(QueryDocumentSnapshot.class);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("id", "exp-001");
        data1.put("amountPaise", 50000L);
        data1.put("categoryId", "dining");

        when(doc1.getId()).thenReturn("exp-001");
        when(doc1.getData()).thenReturn(data1);

        when(snapshot.getDocuments()).thenReturn(List.of(doc1));
        @SuppressWarnings("unchecked")
        com.google.api.core.ApiFuture<QuerySnapshot> futureSnap = mock(com.google.api.core.ApiFuture.class);
        when(futureSnap.get()).thenReturn(snapshot);
        when(query.get()).thenReturn(futureSnap);

        MonthlyReportResponse report = reportService.getMonthlyReport(uid, month);

        assertNotNull(report);
        assertEquals("2026-10", report.getMonth());
        assertEquals(50000L, report.getTotalAmountPaise());
        assertEquals(1, report.getExpenseCount());
        assertEquals(50000L, report.getCategoryBreakdownPaise().get("dining"));
    }
}
