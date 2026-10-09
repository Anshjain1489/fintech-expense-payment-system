import React, { useEffect, useState } from 'react';
import { ExpenseAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import ExpenseModal from '../components/ExpenseModal';
import { 
  Plus, 
  Trash2, 
  Edit3, 
  Calendar, 
  Receipt, 
  Search, 
  ExternalLink 
} from 'lucide-react';

export default function ExpensesPage() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [monthFilter, setMonthFilter] = useState(new Date().toISOString().substring(0, 7));
  const [searchQuery, setSearchQuery] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedExpense, setSelectedExpense] = useState(null);

  const fetchExpenses = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await ExpenseAPI.getExpenses(monthFilter);
      setExpenses(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch expenses');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, [monthFilter]);

  const handleCreate = () => {
    setSelectedExpense(null);
    setIsModalOpen(true);
  };

  const handleEdit = (expense) => {
    setSelectedExpense(expense);
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this expense? Budget totals will be updated.')) {
      return;
    }
    try {
      await ExpenseAPI.deleteExpense(id);
      fetchExpenses();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete expense');
    }
  };

  const handleModalSubmit = async (data) => {
    if (selectedExpense) {
      await ExpenseAPI.updateExpense(selectedExpense.id, data);
    } else {
      await ExpenseAPI.createExpense(data);
    }
    fetchExpenses();
  };

  const filteredExpenses = expenses.filter((exp) => {
    const q = searchQuery.toLowerCase();
    return (
      (exp.note && exp.note.toLowerCase().includes(q)) ||
      (exp.categoryId && exp.categoryId.toLowerCase().includes(q))
    );
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6 animate-fadeIn">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-slate-900/90 p-6 border border-slate-800 rounded-3xl shadow-xl">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center space-x-2">
            <Receipt className="w-6 h-6 text-teal-400" />
            <span>Expense Tracker</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">Manage and audit your monthly expenditure</p>
        </div>
        <button
          onClick={handleCreate}
          className="flex items-center space-x-2 px-4 py-2.5 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-400 hover:to-cyan-400 text-slate-950 font-bold rounded-xl text-sm transition-all shadow-lg shadow-teal-500/20 active:scale-95"
        >
          <Plus className="w-5 h-5" />
          <span>Add Expense</span>
        </button>
      </div>

      {error && <ErrorMessage title="Error" message={error} onRetry={fetchExpenses} />}

      {/* Filter Toolbar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-slate-900/50 p-4 border border-slate-800 rounded-2xl">
        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
          <input
            type="text"
            placeholder="Search notes or categories..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-teal-500"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto">
          <Calendar className="w-4 h-4 text-teal-400" />
          <span className="text-xs font-semibold text-slate-400 uppercase">Month:</span>
          <input
            type="month"
            value={monthFilter}
            onChange={(e) => setMonthFilter(e.target.value)}
            className="px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-xl text-sm font-semibold text-slate-200 focus:outline-none focus:border-teal-500"
          />
        </div>
      </div>

      {/* Expense Data Table */}
      {loading ? (
        <LoadingSpinner message="Fetching expenses list..." />
      ) : filteredExpenses.length === 0 ? (
        <EmptyState
          title="No expenses found"
          description={`No recorded expenses for month ${monthFilter}.`}
          actionLabel="Add Expense"
          onAction={handleCreate}
        />
      ) : (
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl shadow-xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/60 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-4 px-6">Date</th>
                  <th className="py-4 px-6">Category</th>
                  <th className="py-4 px-6">Note</th>
                  <th className="py-4 px-6">Receipt</th>
                  <th className="py-4 px-6 text-right">Amount (₹)</th>
                  <th className="py-4 px-6 text-center">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-sm text-slate-200">
                {filteredExpenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-4 px-6 font-mono text-xs text-slate-400">{exp.date}</td>
                    <td className="py-4 px-6 font-semibold capitalize text-teal-400">{exp.categoryId}</td>
                    <td className="py-4 px-6 text-slate-300">{exp.note || '-'}</td>
                    <td className="py-4 px-6">
                      {exp.receiptUrl ? (
                        <a
                          href={exp.receiptUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex items-center space-x-1 text-xs text-cyan-400 hover:underline"
                        >
                          <span>View</span>
                          <ExternalLink className="w-3 h-3" />
                        </a>
                      ) : (
                        <span className="text-xs text-slate-500">-</span>
                      )}
                    </td>
                    <td className="py-4 px-6 text-right font-bold text-slate-100 text-base">
                      ₹{(exp.amountPaise / 100).toFixed(2)}
                    </td>
                    <td className="py-4 px-6 text-center">
                      <div className="flex items-center justify-center space-x-2">
                        <button
                          onClick={() => handleEdit(exp)}
                          className="p-1.5 text-slate-400 hover:text-teal-400 hover:bg-slate-800 rounded-lg transition-colors"
                          title="Edit Expense"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(exp.id)}
                          className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-lg transition-colors"
                          title="Delete Expense"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add / Edit Expense Modal */}
      <ExpenseModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleModalSubmit}
        initialData={selectedExpense}
      />
    </div>
  );
}
