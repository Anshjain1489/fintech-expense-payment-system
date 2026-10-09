import React, { useEffect, useState } from 'react';
import { PaymentAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import { CreditCard, ShieldCheck, RefreshCw, CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';

export default function PaymentsPage() {
  const [amountInr, setAmountInr] = useState('500');
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await PaymentAPI.getTransactions();
      setTransactions(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load transaction history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleRazorpayCheckout = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    const parsedInr = parseFloat(amountInr);
    if (isNaN(parsedInr) || parsedInr <= 0) {
      setError('Please enter a valid amount in ₹');
      return;
    }

    const amountPaise = Math.round(parsedInr * 100);

    // Rule: FRESH Idempotency-Key generated per payment attempt
    const freshIdempotencyKey = crypto.randomUUID();

    try {
      setProcessing(true);

      // 1. Call Backend create-order with Idempotency-Key
      const orderRes = await PaymentAPI.createOrder(
        { amountPaise, currency: 'INR' },
        freshIdempotencyKey
      );

      const orderData = orderRes.data;

      // 2. Options for Razorpay SDK Modal
      const options = {
        key: import.meta.env.VITE_RAZORPAY_KEY_ID || 'rzp_test_dummyKeyId',
        amount: orderData.amountPaise,
        currency: orderData.currency || 'INR',
        name: 'FinTech Expense App',
        description: `Payment for Order ${orderData.orderId}`,
        order_id: orderData.orderId,
        handler: async function (response) {
          try {
            // 3. Verify Payment Signature server-side
            const verifyRes = await PaymentAPI.verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });

            setSuccessMsg('Payment verified and completed successfully!');
            fetchTransactions();
          } catch (err) {
            setError(err.response?.data?.message || 'Payment verification failed');
          } finally {
            setProcessing(false);
          }
        },
        modal: {
          ondismiss: function () {
            setProcessing(false);
          },
        },
        theme: {
          color: '#06b6d4',
        },
      };

      if (window.Razorpay) {
        const rzp = new window.Razorpay(options);
        rzp.open();
      } else {
        // Fallback for dev mode if Razorpay JS SDK isn't active
        const simulate = window.confirm(
          `Razorpay SDK mock mode active for Order ${orderData.orderId}.\nClick OK to simulate successful server payment verification.`
        );
        if (simulate) {
          await PaymentAPI.verifyPayment({
            razorpayOrderId: orderData.orderId,
            razorpayPaymentId: 'pay_mock_' + Date.now(),
            razorpaySignature: 'mock_signature_valid',
          });
          setSuccessMsg('Mock payment verified successfully!');
          fetchTransactions();
        }
        setProcessing(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Payment initiation failed');
      setProcessing(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'paid':
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-teal-950 text-teal-400 border border-teal-800/60">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>PAID</span>
          </span>
        );
      case 'failed':
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-950 text-rose-400 border border-rose-800/60">
            <XCircle className="w-3.5 h-3.5" />
            <span>FAILED</span>
          </span>
        );
      case 'amount_mismatch':
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-950 text-amber-400 border border-amber-800/60">
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>MISMATCH</span>
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold bg-slate-800 text-slate-400 border border-slate-700">
            <RefreshCw className="w-3.5 h-3.5 animate-spin" />
            <span>CREATED</span>
          </span>
        );
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 animate-fadeIn">
      {/* Page Header */}
      <div className="bg-slate-900/90 p-6 border border-slate-800 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center space-x-2">
            <CreditCard className="w-6 h-6 text-teal-400" />
            <span>Razorpay Payments</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">Make test mode payments and view payment audit history</p>
        </div>
        <div className="flex items-center space-x-2 text-xs font-medium text-slate-400 bg-slate-950 px-3 py-1.5 rounded-xl border border-slate-800">
          <ShieldCheck className="w-4 h-4 text-teal-400" />
          <span>Server-Side Signature Protected</span>
        </div>
      </div>

      {error && <ErrorMessage title="Payment Error" message={error} />}
      {successMsg && (
        <div className="p-4 bg-teal-950/40 border border-teal-800/60 rounded-xl text-teal-300 text-sm font-semibold flex items-center space-x-2">
          <CheckCircle2 className="w-5 h-5 text-teal-400" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Payment Checkout Form Card */}
      <div className="bg-slate-900/90 border border-slate-800 p-6 rounded-3xl shadow-xl max-w-xl">
        <h3 className="text-lg font-bold text-slate-100 mb-4">Initiate Payment</h3>
        <form onSubmit={handleRazorpayCheckout} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Payment Amount (₹ INR) *
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-400 text-base font-bold">
                ₹
              </span>
              <input
                type="number"
                step="1"
                min="1"
                required
                value={amountInr}
                onChange={(e) => setAmountInr(e.target.value)}
                placeholder="500"
                className="w-full pl-9 pr-4 py-3 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-lg font-bold focus:outline-none"
              />
            </div>
            <span className="text-xs text-slate-400 mt-1 block">
              Generates fresh <code className="text-teal-400 font-mono">Idempotency-Key</code> header per request attempt
            </span>
          </div>

          <button
            type="submit"
            disabled={processing}
            className="w-full py-3 px-6 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-400 hover:to-cyan-400 text-slate-950 font-bold rounded-xl text-sm transition-all shadow-lg shadow-teal-500/20 active:scale-[0.99] flex items-center justify-center space-x-2 disabled:opacity-50"
          >
            <span>{processing ? 'Launching Checkout...' : 'Pay with Razorpay'}</span>
            <CreditCard className="w-4 h-4" />
          </button>
        </form>
      </div>

      {/* Transaction History Table */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <h3 className="text-lg font-bold text-slate-100 mb-4">Payment History</h3>
        {loading ? (
          <LoadingSpinner message="Loading transaction records..." />
        ) : transactions.length === 0 ? (
          <EmptyState title="No transactions recorded" description="Initiate a payment above to see transaction history." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/60 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-4 px-6">Order ID</th>
                  <th className="py-4 px-6">Payment ID</th>
                  <th className="py-4 px-6 text-right">Amount (₹)</th>
                  <th className="py-4 px-6 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-sm text-slate-200">
                {transactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-4 px-6 font-mono text-xs text-teal-400">{tx.razorpayOrderId}</td>
                    <td className="py-4 px-6 font-mono text-xs text-slate-400">{tx.razorpayPaymentId || '-'}</td>
                    <td className="py-4 px-6 text-right font-bold text-slate-100">
                      ₹{(tx.amountPaise / 100).toFixed(2)}
                    </td>
                    <td className="py-4 px-6 text-center">{getStatusBadge(tx.status)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
