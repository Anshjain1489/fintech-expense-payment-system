import React, { useEffect, useState } from 'react';
import { X, Receipt, IndianRupee } from 'lucide-react';

export default function ExpenseModal({ isOpen, onClose, onSubmit, initialData = null }) {
  const [amountInr, setAmountInr] = useState('');
  const [categoryId, setCategoryId] = useState('groceries');
  const [accountId, setAccountId] = useState('bank_main');
  const [note, setNote] = useState('');
  const [date, setDate] = useState(new Date().toISOString().substring(0, 10));
  const [receiptUrl, setReceiptUrl] = useState('');
  const [recurring, setRecurring] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (initialData) {
      setAmountInr((initialData.amountPaise / 100).toString());
      setCategoryId(initialData.categoryId || 'groceries');
      setAccountId(initialData.accountId || 'bank_main');
      setNote(initialData.note || '');
      setDate(initialData.date || new Date().toISOString().substring(0, 10));
      setReceiptUrl(initialData.receiptUrl || '');
      setRecurring(initialData.recurring || false);
    } else {
      setAmountInr('');
      setCategoryId('groceries');
      setAccountId('bank_main');
      setNote('');
      setDate(new Date().toISOString().substring(0, 10));
      setReceiptUrl('');
      setRecurring(false);
    }
    setError('');
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const parsedInr = parseFloat(amountInr);
    if (isNaN(parsedInr) || parsedInr <= 0) {
      setError('Please enter a valid positive amount in ₹');
      return;
    }

    const amountPaise = Math.round(parsedInr * 100);

    try {
      setSubmitting(true);
      await onSubmit({
        amountPaise,
        categoryId,
        accountId,
        note,
        date,
        receiptUrl,
        recurring,
      });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to save expense');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-lg w-full p-6 shadow-2xl relative">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-teal-500/10 text-teal-400 rounded-xl">
              <Receipt className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-slate-100">
              {initialData ? 'Edit Expense' : 'Add New Expense'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="mt-4 p-3 bg-rose-950/50 border border-rose-800/60 rounded-xl text-xs text-rose-300">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Amount (₹ INR) *
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-400 text-sm font-semibold">
                ₹
              </span>
              <input
                type="number"
                step="0.01"
                min="0.01"
                required
                value={amountInr}
                onChange={(e) => setAmountInr(e.target.value)}
                placeholder="0.00"
                className="w-full pl-9 pr-4 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none focus:ring-1 focus:ring-teal-500 font-medium"
              />
            </div>
            <span className="text-[11px] text-slate-500 mt-1 block">
              Will be saved internally in Paise (1 ₹ = 100 paise)
            </span>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Category *
              </label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none"
              >
                <option value="groceries">Groceries</option>
                <option value="dining">Dining & Food</option>
                <option value="shopping">Shopping</option>
                <option value="utilities">Bills & Utilities</option>
                <option value="transport">Transport</option>
                <option value="entertainment">Entertainment</option>
                <option value="healthcare">Healthcare</option>
                <option value="other">Other</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Date (yyyy-MM-dd) *
              </label>
              <input
                type="date"
                required
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Note / Description
            </label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. Weekly supermarket shopping"
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Receipt Image URL (Optional)
            </label>
            <input
              type="url"
              value={receiptUrl}
              onChange={(e) => setReceiptUrl(e.target.value)}
              placeholder="https://storage.googleapis.com/..."
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none text-xs"
            />
          </div>

          <div className="flex items-center space-x-2 pt-2">
            <input
              type="checkbox"
              id="recurringCheck"
              checked={recurring}
              onChange={(e) => setRecurring(e.target.checked)}
              className="w-4 h-4 rounded border-slate-800 bg-slate-950 text-teal-500 focus:ring-teal-500"
            />
            <label htmlFor="recurringCheck" className="text-sm font-medium text-slate-300">
              Recurring Monthly Expense
            </label>
          </div>

          <div className="pt-4 flex items-center justify-end space-x-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-sm font-medium transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-400 hover:to-cyan-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-lg shadow-teal-500/20 disabled:opacity-50"
            >
              {submitting ? 'Saving...' : initialData ? 'Update Expense' : 'Create Expense'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
