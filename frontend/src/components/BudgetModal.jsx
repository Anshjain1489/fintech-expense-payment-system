import React, { useEffect, useState } from 'react';
import { X, PieChart } from 'lucide-react';

export default function BudgetModal({ isOpen, onClose, onSubmit }) {
  const [categoryId, setCategoryId] = useState('groceries');
  const [month, setMonth] = useState(new Date().toISOString().substring(0, 7)); // yyyy-MM
  const [limitInr, setLimitInr] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setCategoryId('groceries');
    setMonth(new Date().toISOString().substring(0, 7));
    setLimitInr('');
    setError('');
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const parsedInr = parseFloat(limitInr);
    if (isNaN(parsedInr) || parsedInr <= 0) {
      setError('Please enter a valid monthly limit in ₹');
      return;
    }

    const limitPaise = Math.round(parsedInr * 100);

    try {
      setSubmitting(true);
      await onSubmit({
        categoryId,
        month,
        limitPaise,
      });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to set budget');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl relative">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-teal-500/10 text-teal-400 rounded-xl">
              <PieChart className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-bold text-slate-100">Set Category Budget</h3>
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
              Category *
            </label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none"
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
              Month (yyyy-MM) *
            </label>
            <input
              type="month"
              required
              value={month}
              onChange={(e) => setMonth(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Monthly Limit (₹ INR) *
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-400 text-sm font-semibold">
                ₹
              </span>
              <input
                type="number"
                step="1"
                min="1"
                required
                value={limitInr}
                onChange={(e) => setLimitInr(e.target.value)}
                placeholder="10000"
                className="w-full pl-9 pr-4 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none focus:ring-1 focus:ring-teal-500 font-medium"
              />
            </div>
            <span className="text-[11px] text-slate-500 mt-1 block">
              Budget ID will automatically be set as <code className="text-teal-400">{categoryId}_{month}</code>
            </span>
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
              {submitting ? 'Setting...' : 'Set Budget'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
