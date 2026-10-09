# Phase 2 REST API Examples & Postman Collections

## Base URL
`http://localhost:8080/api/v1`

## Authentication Header
```http
Authorization: Bearer <FIREBASE_ID_TOKEN>
```

---

## 1. Create Expense
- **Endpoint**: `POST /expenses`
- **Description**: Creates a new expense and updates category budget `spentPaise` atomically.
- **Request Body**:
```json
{
  "amountPaise": 50000,
  "categoryId": "groceries",
  "accountId": "acc-101",
  "note": "Weekly supermarket shopping",
  "date": "2026-10-15",
  "receiptUrl": "https://storage.googleapis.com/demo/receipts/rec_001.jpg",
  "recurring": false
}
```
- **Response (201 Created)**:
```json
{
  "id": "e4a90f12-789a-4c2d-90bc-123456789abc",
  "overBudget": false,
  "expense": {
    "id": "e4a90f12-789a-4c2d-90bc-123456789abc",
    "amountPaise": 50000,
    "categoryId": "groceries",
    "accountId": "acc-101",
    "note": "Weekly supermarket shopping",
    "date": "2026-10-15",
    "month": "2026-10",
    "receiptUrl": "https://storage.googleapis.com/demo/receipts/rec_001.jpg",
    "status": "approved",
    "recurring": false,
    "createdAt": 1792080000000
  }
}
```

---

## 2. List Expenses (Filtered by Month)
- **Endpoint**: `GET /expenses?month=2026-10`
- **Response (200 OK)**:
```json
[
  {
    "id": "e4a90f12-789a-4c2d-90bc-123456789abc",
    "amountPaise": 50000,
    "categoryId": "groceries",
    "accountId": "acc-101",
    "note": "Weekly supermarket shopping",
    "date": "2026-10-15",
    "month": "2026-10",
    "receiptUrl": "https://storage.googleapis.com/demo/receipts/rec_001.jpg",
    "status": "approved",
    "recurring": false,
    "createdAt": 1792080000000
  }
]
```

---

## 3. Get Expense by ID
- **Endpoint**: `GET /expenses/e4a90f12-789a-4c2d-90bc-123456789abc`
- **Response (200 OK)**:
```json
{
  "id": "e4a90f12-789a-4c2d-90bc-123456789abc",
  "amountPaise": 50000,
  "categoryId": "groceries",
  "accountId": "acc-101",
  "note": "Weekly supermarket shopping",
  "date": "2026-10-15",
  "month": "2026-10",
  "receiptUrl": "https://storage.googleapis.com/demo/receipts/rec_001.jpg",
  "status": "approved",
  "recurring": false,
  "createdAt": 1792080000000
}
```

---

## 4. Update Expense
- **Endpoint**: `PUT /expenses/e4a90f12-789a-4c2d-90bc-123456789abc`
- **Request Body**:
```json
{
  "amountPaise": 65000,
  "categoryId": "groceries",
  "accountId": "acc-101",
  "note": "Updated supermarket shopping + organic items",
  "date": "2026-10-15",
  "receiptUrl": "https://storage.googleapis.com/demo/receipts/rec_001.jpg",
  "recurring": false
}
```
- **Response (200 OK)**:
```json
{
  "id": "e4a90f12-789a-4c2d-90bc-123456789abc",
  "amountPaise": 65000,
  "categoryId": "groceries",
  "accountId": "acc-101",
  "note": "Updated supermarket shopping + organic items",
  "date": "2026-10-15",
  "month": "2026-10",
  "receiptUrl": "https://storage.googleapis.com/demo/receipts/rec_001.jpg",
  "status": "approved",
  "recurring": false,
  "createdAt": 1792080000000
}
```

---

## 5. Delete Expense
- **Endpoint**: `DELETE /expenses/e4a90f12-789a-4c2d-90bc-123456789abc`
- **Response (24 No Content)**

---

## 6. Set Category Budget
- **Endpoint**: `POST /budgets`
- **Description**: Sets or updates monthly budget using document key format `{categoryId}_{yyyy-MM}`.
- **Request Body**:
```json
{
  "categoryId": "groceries",
  "month": "2026-10",
  "limitPaise": 100000
}
```
- **Response (201 Created)**:
```json
{
  "id": "groceries_2026-10",
  "categoryId": "groceries",
  "month": "2026-10",
  "limitPaise": 100000,
  "spentPaise": 65000
}
```

---

## 7. List Budgets
- **Endpoint**: `GET /budgets?month=2026-10`
- **Response (200 OK)**:
```json
[
  {
    "id": "groceries_2026-10",
    "categoryId": "groceries",
    "month": "2026-10",
    "limitPaise": 100000,
    "spentPaise": 65000
  }
]
```
