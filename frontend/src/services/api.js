import axios from 'axios';
import { auth } from '../firebase';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to dynamically attach Firebase Bearer ID Token
api.interceptors.request.use(async (config) => {
  try {
    const user = auth.currentUser;
    if (user) {
      const token = await user.getIdToken();
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      // Fallback for local dev testing token if present
      const devToken = localStorage.getItem('dev_token');
      if (devToken) {
        config.headers.Authorization = `Bearer ${devToken}`;
      }
    }
  } catch (error) {
    console.error('Error fetching Firebase ID token:', error);
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// API Services
export const ExpenseAPI = {
  getExpenses: (month) => api.get('/expenses', { params: { month } }),
  getExpense: (id) => api.get(`/expenses/${id}`),
  createExpense: (data) => api.post('/expenses', data),
  updateExpense: (id, data) => api.put(`/expenses/${id}`, data),
  deleteExpense: (id) => api.delete(`/expenses/${id}`),
};

export const BudgetAPI = {
  getBudgets: (month) => api.get('/budgets', { params: { month } }),
  setBudget: (data) => api.post('/budgets', data),
};

export const PaymentAPI = {
  // Generates a fresh Idempotency-Key UUID header per payment attempt!
  createOrder: (data, customIdempotencyKey) => {
    const idempotencyKey = customIdempotencyKey || crypto.randomUUID();
    return api.post('/payments/create-order', data, {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    });
  },
  verifyPayment: (data) => api.post('/payments/verify', data),
  getTransactions: () => api.get('/payments/transactions'),
};

export const AdminAPI = {
  getTransactions: () => api.get('/admin/transactions'),
  getUsers: () => api.get('/admin/users'),
  getAuditLogs: () => api.get('/admin/audit-logs'),
};

export default api;
