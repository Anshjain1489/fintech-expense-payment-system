# FinTech Expense & Payment Management System

A monorepo production-style application for managing expenses, budgets, approval workflows, and Razorpay payment integration using Java 17 Spring Boot, Firebase Cloud Firestore Admin SDK, and React (Vite) with Tailwind CSS.

---

## Architecture Overview

```
 [ React Frontend ] (Vite + Tailwind + Recharts + Axios)
        |
        |  Authorization: Bearer <Firebase ID Token>
        v
 [ Spring Boot Backend ] (Java 17, Spring Security)
   - FirebaseAuthenticationFilter (Token Verification & Custom Claim Role extraction)
   - GlobalExceptionHandler (@RestControllerAdvice)
   - Business Logic & Atomicity (runTransaction)
        |
        +----------------------------+
        |                            |
        v                            v
[ Firestore Database ]       [ Razorpay Payment Gateway ]
 (Firebase Admin SDK)          (Server-side Signature Verification & Webhooks)
```

---

## Non-Negotiable Financial & Security Rules Enforced

1. **Money Precision**: Stored strictly as `Long` integers in **paise** (1 INR = 100 paise). Floating point types are forbidden for monetary storage.
2. **Backend-Only Database Access**: Firestore rules (`allow read, write: if false;`) block direct client access. All writes execute through Spring Boot inside `runTransaction` (all reads completed before writes).
3. **Razorpay Server-Side Verification**: HMAC-SHA256 signature verification for checkout & webhooks. Idempotent handlers (`payment.captured`, `payment.failed`) with amount-mismatch checks.
4. **Idempotency**: `create-order` requires mandatory `Idempotency-Key` header; retried requests return the identical order without duplicate creation.
5. **Secret Management**: Zero hardcoded credentials. All secrets read from environment variables (`FIREBASE_KEY_PATH`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`). `.gitignore` excludes keys and `.env` files.
6. **Audit Trail**: Every financial action queues an entry in `auditLogs/{id}`.
7. **Sanitized Input & Error Reporting**: Input validated with Jakarta Bean Validation. `GlobalExceptionHandler` returns standard JSON errors without exposing stack traces.
8. **Multi-Tenancy & RBAC**: Data partitioned by `uid` extracted from verified Firebase ID Tokens. `/admin/**` endpoints restricted to `ROLE_ADMIN`.

---

## Repository Structure

```text
fintech-app/
├── backend/                  # Java 17, Spring Boot 3.2, Maven
│   ├── src/main/java/com/ansh/fintech/
│   │   ├── config/           # Firebase, OpenAPI, CORS
│   │   ├── controller/       # Expense, Budget, Payment, Approval, Receipt, Report, Admin
│   │   ├── dto/              # Request & Response DTOs with validation
│   │   ├── exception/        # GlobalExceptionHandler & custom exceptions
│   │   ├── model/            # Expense, Budget, TransactionRecord, ApprovalRecord, AuditLog
│   │   ├── security/         # FirebaseAuthenticationFilter & SecurityConfig
│   │   └── service/          # Expense, Payment, Approval, ReceiptStorage, Report, Admin, Audit
│   ├── src/test/java/        # JUnit 5 & Mockito service unit tests
│   └── Dockerfile            # Multi-stage production container build
├── frontend/                 # React 18, Vite, Tailwind CSS, Recharts
│   ├── src/
│   │   ├── components/       # Navbar, ExpenseModal, BudgetModal, LoadingSpinner, EmptyState
│   │   ├── context/          # AuthContext (Firebase Auth)
│   │   ├── pages/            # Login, Signup, Dashboard, Expenses, Budgets, Payments, Admin
│   │   └── services/         # Axios client with dynamic Bearer Token & Idempotency-Key
│   ├── index.html            # Loads Razorpay JS SDK & Inter font
│   └── vite.config.js        # Proxy to backend on port 8080
├── firebase/                 # Firebase Emulator & Security Rules
│   ├── firebase.json         # Auth (9099), Firestore (8080), Storage (9199) ports
│   └── firestore.rules       # Strict allow read, write: if false; rule
└── docs/                     # Architecture & API documentation
```

---

## Environment Variables

Copy or export the following variables in your environment:

```env
# Firebase Setup
FIREBASE_KEY_PATH=./firebase-key.json
FIRESTORE_EMULATOR_HOST=localhost:8080   # Optional: set when running local emulator

# Razorpay Test Mode Credentials
RAZORPAY_KEY_ID=rzp_test_YourKeyId
RAZORPAY_KEY_SECRET=YourKeySecret
RAZORPAY_WEBHOOK_SECRET=YourWebhookSecret

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

---

## Local Development & Setup Guide

### 1. Prerequisites
- Java 17 JDK
- Node.js (v18+)
- Maven (or IntelliJ bundled Maven)
- Firebase CLI (`npm install -g firebase-tools`)

### 2. Running Firebase Emulator Suite
Navigate to `firebase/` directory and launch emulators:
```bash
cd firebase
firebase emulators:start
```
Emulators UI will open at `http://localhost:4000` (Firestore at `8080`, Auth at `9099`).

### 3. Running Backend Service
```bash
cd backend
mvn clean spring-boot:run
```
- Swagger UI will be accessible at: `http://localhost:8080/api/v1/swagger-ui.html`
- Health check: `http://localhost:8080/api/v1/health`

### 4. Running React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## Testing Razorpay Webhooks Locally with Ngrok

1. Install ngrok (`npm install -g ngrok` or download binary).
2. Expose the Spring Boot backend port:
   ```bash
   ngrok http 8080
   ```
3. Copy the forwarding HTTPS URL generated by ngrok (e.g. `https://a1b2c3d4.ngrok-free.app`).
4. In your [Razorpay Dashboard](https://dashboard.razorpay.com/) (Test Mode):
   - Navigate to **Settings > Webhooks > Add New Webhook**.
   - Set Webhook URL to: `https://a1b2c3d4.ngrok-free.app/api/v1/payments/webhook`.
   - Set Webhook Secret to match `RAZORPAY_WEBHOOK_SECRET`.
   - Select events: `payment.captured` and `payment.failed`.
5. Initiate a payment from the React frontend (`/payments`). Razorpay webhooks will trigger and hit your local backend via ngrok, updating transaction statuses atomically.

---

## Executing Backend Unit Tests

Run full test suite:
```bash
cd backend
mvn test
```
All 21 unit tests across services, controllers, and security filters will compile and execute.

---

## Deployment Steps

### 1. Backend Deployment (Docker)
Build and run the production container:
```bash
cd backend
docker build -t fintech-backend:latest .
docker run -d -p 8080:8080 \
  -e FIREBASE_KEY_PATH=/app/firebase-key.json \
  -e RAZORPAY_KEY_ID=${RAZORPAY_KEY_ID} \
  -e RAZORPAY_KEY_SECRET=${RAZORPAY_KEY_SECRET} \
  -e RAZORPAY_WEBHOOK_SECRET=${RAZORPAY_WEBHOOK_SECRET} \
  -e CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app \
  fintech-backend:latest
```

### 2. Frontend Deployment (Vercel)
1. Push repository to GitHub/GitLab.
2. Import `frontend/` directory into Vercel Dashboard.
3. Configure Environment Variables in Vercel settings:
   - `VITE_FIREBASE_API_KEY`
   - `VITE_FIREBASE_AUTH_DOMAIN`
   - `VITE_FIREBASE_PROJECT_ID`
   - `VITE_RAZORPAY_KEY_ID`
4. Deploy! Vercel will run `npm run build` and output static SPA bundle.
