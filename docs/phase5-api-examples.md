# Phase 5 Approval Workflow, Receipt Storage, Reports & CSV Export

## Base URL
`http://localhost:8080/api/v1`

---

## 1. Submit Expense Claim for Approval
- **Endpoint**: `POST /approvals`
- **Headers**: `Authorization: Bearer <FIREBASE_ID_TOKEN>`
- **Request Body**:
```json
{
  "expenseId": "exp-100",
  "comment": "Travel reimbursement claim for client visit"
}
```
- **Response (201 Created)**:
```json
{
  "id": "app-a1b2c3d4",
  "expenseId": "exp-100",
  "requestedBy": "user-emp-1",
  "approverId": null,
  "status": "pending",
  "comment": "Travel reimbursement claim for client visit",
  "createdAt": 1792080000000
}
```

---

## 2. Approve or Reject Claim (Manager / Accountant)
- **Endpoint**: `PUT /approvals/app-a1b2c3d4`
- **Headers**: `Authorization: Bearer <FIREBASE_ID_TOKEN>`
- **Request Body**:
```json
{
  "status": "approved",
  "comment": "Verified travel receipt. Approved."
}
```
- **Response (200 OK)**:
```json
{
  "id": "app-a1b2c3d4",
  "expenseId": "exp-100",
  "requestedBy": "user-emp-1",
  "approverId": "manager-99",
  "status": "approved",
  "comment": "Verified travel receipt. Approved.",
  "createdAt": 1792080000000
}
```

---

## 3. Upload Receipt to Cloud Storage
- **Endpoint**: `POST /expenses/exp-100/receipt`
- **Headers**:
  - `Authorization: Bearer <FIREBASE_ID_TOKEN>`
  - `Content-Type: multipart/form-data`
- **Form Data**:
  - `file`: `<invoice.pdf / receipt.png>` (Strictly max 5 MB, Image or PDF only)
- **Response (200 OK)**:
```json
{
  "expenseId": "exp-100",
  "receiptUrl": "https://storage.googleapis.com/demo-fintech-project.appspot.com/receipts/user-emp-1/exp-100_uuid.pdf"
}
```

---

## 4. Get Monthly Report
- **Endpoint**: `GET /reports/monthly?month=2026-10`
- **Headers**: `Authorization: Bearer <FIREBASE_ID_TOKEN>`
- **Response (200 OK)**:
```json
{
  "month": "2026-10",
  "totalAmountPaise": 250000,
  "expenseCount": 3,
  "categoryBreakdownPaise": {
    "groceries": 100000,
    "transport": 150000
  }
}
```

---

## 5. Export Expenses to CSV
- **Endpoint**: `GET /reports/export?month=2026-10`
- **Headers**: `Authorization: Bearer <FIREBASE_ID_TOKEN>`
- **Response Header**: `Content-Type: text/csv`, `Content-Disposition: attachment; filename="expenses-export-2026-10.csv"`
- **Response Body**:
```csv
ID,Date,Category,Account,Amount(Paise),Amount(INR),Status,Note
"exp-100","2026-10-10","groceries","bank_1",100000,1000.00,"approved","Weekly groceries"
"exp-101","2026-10-15","transport","bank_1",150000,1500.00,"approved","Flight ticket"
```
