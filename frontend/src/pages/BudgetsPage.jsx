import React, { useEffect, useState } from 'react';
import { BudgetAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import BudgetModal from '../components/BudgetModal';
import { PieChart, Plus, Calendar, AlertTriangle, CheckCircle2 } from 'lucide-react';

export default function BudgetsPage() {
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [monthFilter, setMonthFilter] = useState(new Date().toISOString().substring(0, 7));
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchBudgets = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await BudgetAPI.getBudgets(monthFilter);
      setBudgets(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch budgets');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, [monthFilter]);

  const handleModalSubmit = async (data) => {
    await BudgetAPI.setBudget(data);
    fetchBudgets();
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6 animate-fadeIn">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900/90 p-6 border border-slate-800 rounded-3xl shadow-xl">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center space-x-2">
            <PieChart className="w-6 h-6 text-teal-400" />
            <span>Budget Management</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">Set limits and monitor category monthly spending limits</p>
        </div>
        <div className="flex items-center space-x-3">
          <div className="flex items-center space-x-2">
            <Calendar className="w-4 h-4 text-teal-400" />
            <input
              type="month"
              value={monthFilter}
              onChange={(e) => setMonthFilter(e.target.value)}
              className="px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-xl text-sm font-semibold text-slate-200 focus:outline-none focus:border-teal-500"
            />
          </div>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-400 hover:to-cyan-400 text-slate-950 font-bold rounded-xl text-sm transition-all shadow-lg shadow-teal-500/20 active:scale-95"
          >
            <Plus className="w-5 h-5" />
            <span>Set Budget</span>
          </button>
        </div>
      </div>

      {error && <ErrorMessage title="Error" message={error} onRetry={fetchBudgets} />}

      {/* Budget Cards List */}
      {loading ? (
        <LoadingSpinner message="Loading monthly budgets..." />
      ) : budgets.length === 0 ? (
        <EmptyState
          title="No budgets set"
          description={`No active category budgets found for ${monthFilter}.`}
          actionLabel="Set Category Budget"
          onAction={() => setIsModalOpen(true)}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {budgets.map((b) => {
            const limitInr = (b.limitPaise || 0) / 100;
            const spentInr = (b.spentPaise || 0) / 100;
            const pct = limitInr > 0 ? Math.min(100, Math.round((spentInr / limitInr) * 100)) : 0;
            const isOver = b.spentPaise > b.limitPaise;

            return (
              <div
                key={b.id}
                className={`bg-slate-900/90 border p-6 rounded-3xl shadow-xl transition-all ${
                  isOver
                    ? 'border-rose-500/60 bg-rose-950/10 ring-1 ring-rose-500/30'
                    : 'border-slate-800 hover:border-teal-500/40'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className="text-base font-bold capitalize text-slate-100">{b.categoryId}</span>
                  {isOver ? (
                    <span className="inline-flex items-center space-x-1 text-xs font-bold text-rose-400 bg-rose-950/80 border border-rose-800/60 px-2.5 py-1 rounded-full">
                      <AlertTriangle className="w-3.5 h-3.5" />
                      <span>Over Limit</span>
                    </span>
                  ) : (
                    <span className="inline-flex items-center space-x-1 text-xs font-bold text-teal-400 bg-teal-950/80 border border-teal-800/60 px-2.5 py-1 rounded-full">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      <span>On Track</span>
                    </span>
                  )}
                </div>

                <div className="mt-6 flex items-baseline justify-between text-sm">
                  <span className="text-slate-400">Spent: <strong className={isOver ? 'text-rose-400 font-bold' : 'text-slate-200'}>₹{spentInr.toFixed(2)}</strong></span>
                  <span className="text-slate-400">Limit: <strong className="text-slate-200">₹{limitInr.toFixed(2)}</strong></span>
                </div>

                {/* Progress Bar Component */}
                <div className="mt-3 relative w-full h-3 bg-slate-950 rounded-full overflow-hidden border border-slate-800">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isOver
                        ? 'bg-gradient-to-r from-rose-600 to-red-500'
                        : pct > 80
                        ? 'bg-gradient-to-r from-amber-500 to-yellow-400'
                        : 'bg-gradient-to-r from-teal-500 to-cyan-400'
                    }`}
                    style={{ width: `${pct}%` }}
                  ></div>
                </div>

                <div className="mt-2 text-right">
                  <span className={`text-xs font-semibold ${isOver ? 'text-rose-400' : 'text-slate-400'}`}>
                    {pct}% Used
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Set Budget Modal */}
      <BudgetModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleModalSubmit}
      />
    </div>
  );
}
