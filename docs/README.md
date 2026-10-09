# FinTech Expense & Payment Management System

A monorepo production-style application for managing expenses, budgets, approval workflows, and Razorpay payment integration using Java 17 Spring Boot and React (Vite).

## Structure
- `backend/`: Spring Boot 3.x backend REST API service.
- `frontend/`: React + Vite + Tailwind CSS frontend application.
- `firebase/`: Firebase configuration, firestore security rules, and emulator settings.
- `docs/`: System documentation and API references.

## Environment Setup
Set the following environment variables when running locally or in production:
- `FIREBASE_KEY_PATH`: Path to Firebase Service Account JSON file (or use `FIRESTORE_EMULATOR_HOST=localhost:8080` for emulator mode).
- `RAZORPAY_KEY_ID`: Razorpay API Key ID (Test Mode).
- `RAZORPAY_KEY_SECRET`: Razorpay API Secret.
- `RAZORPAY_WEBHOOK_SECRET`: Razorpay Webhook Signing Secret.
- `CORS_ALLOWED_ORIGINS`: Allowed CORS origin (default `http://localhost:5173`).
