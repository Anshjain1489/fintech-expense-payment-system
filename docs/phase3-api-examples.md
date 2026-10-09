# Phase 3 Razorpay Payment Integration API Examples

## Base URL
`http://localhost:8080/api/v1`

---

## 1. Create Razorpay Payment Order
- **Endpoint**: `POST /payments/create-order`
- **Headers**:
  - `Authorization: Bearer <FIREBASE_ID_TOKEN>`
  - `Idempotency-Key: ik_8f92a10b-56cd-4e3f-91ab-234567890def` (Mandatory!)
- **Request Body**:
```json
{
  "amountPaise": 50000,
  "currency": "INR"
}
```
- **Response (201 Created)**:
```json
{
  "orderId": "order_mock_a1b2c3d4",
  "amountPaise": 50000,
  "currency": "INR",
  "idempotencyKey": "ik_8f92a10b-56cd-4e3f-91ab-234567890def",
  "status": "created"
}
```
*Note: Sending the exact same `Idempotency-Key` header with identical or retried request returns the existing order instantly without creating a duplicate order.*

---

## 2. Verify Payment Signature
- **Endpoint**: `POST /payments/verify`
- **Headers**:
  - `Authorization: Bearer <FIREBASE_ID_TOKEN>`
- **Request Body**:
```json
{
  "razorpayOrderId": "order_mock_a1b2c3d4",
  "razorpayPaymentId": "pay_xyz987654321",
  "razorpaySignature": "a3b4c5d6e7f890123456789abcdef0123456789abcdef0123456789abcdef012"
}
```
- **Response (200 OK)**:
```json
{
  "verified": true,
  "message": "Payment verified successfully",
  "status": "paid"
}
```

---

## 3. Razorpay Webhook Handler (Public, Signature-Protected)
- **Endpoint**: `POST /payments/webhook`
- **Headers**:
  - `X-Razorpay-Signature: <HMAC_SHA256_WEBHOOK_SIGNATURE>`
- **Request Body (`payment.captured`)**:
```json
{
  "event": "payment.captured",
  "payload": {
    "payment": {
      "entity": {
        "id": "pay_xyz987654321",
        "order_id": "order_mock_a1b2c3d4",
        "amount": 50000,
        "currency": "INR",
        "status": "captured"
      }
    }
  }
}
```
- **Response (200 OK)**

---

## 4. Get User Payment Transactions
- **Endpoint**: `GET /payments/transactions`
- **Headers**:
  - `Authorization: Bearer <FIREBASE_ID_TOKEN>`
- **Response (200 OK)**:
```json
[
  {
    "id": "user-100_ik_8f92a10b-56cd-4e3f-91ab-234567890def",
    "userId": "user-100",
    "amountPaise": 50000,
    "currency": "INR",
    "status": "paid",
    "razorpayOrderId": "order_mock_a1b2c3d4",
    "razorpayPaymentId": "pay_xyz987654321",
    "idempotencyKey": "ik_8f92a10b-56cd-4e3f-91ab-234567890def",
    "createdAt": 1792080000000,
    "paidAt": 1792080005000
  }
]
```
