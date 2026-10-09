# FinTech Expense & Payment Management System Architecture

## Architecture Overview

```
 [ React Frontend ] (Vite + Tailwind + Recharts)
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
 (Firebase Admin SDK)          (Server-side Verification & Webhooks)
```

## Security & Auth Rules
1. Authentication handled via Firebase Auth.
2. Spring Boot verifies Bearer Token on every request via `FirebaseAuthenticationFilter`.
3. Roles: `admin`, `user`, `accountant` mapped to `ROLE_ADMIN`, `ROLE_USER`, `ROLE_ACCOUNTANT`.
4. Firestore direct access is **denied** (`allow read, write: if false;`).
5. All monetary calculations use `Long` in **paise** (1 INR = 100 paise).
