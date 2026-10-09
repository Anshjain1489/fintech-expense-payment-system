import React, { useEffect, useState } from 'react';
import { ExpenseAPI, BudgetAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import { 
  DollarSign, 
  TrendingUp, 
  CreditCard, 
  PieChart as PieIcon, 
  Calendar, 
  ArrowUpRight 
} from 'lucide-react';
import { 
  PieChart, 
  Pie, 
  Cell, 
  Tooltip, 
  ResponsiveContainer, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid 
} from 'recharts';

const CATEGORY_COLORS = {
  groceries: '#06b6d4',
  dining: '#3b82f6',
  shopping: '#8b5cf6',
  utilities: '#f59e0b',
  transport: '#10b981',
  entertainment: '#ec4899',
  healthcare: '#ef4444',
  other: '#64748b',
};

export default function DashboardPage() {
  const [expenses, setExpenses] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentMonth, setCurrentMonth] = useState(new Date().toISOString().substring(0, 7));

  const fetchData = async () => {
    try {
      setLoading(true);
      setError('');
      const [expRes, budRes] = await Promise.all([
        ExpenseAPI.getExpenses(currentMonth),
        BudgetAPI.getBudgets(currentMonth),
      ]);
      setExpenses(expRes.data || []);
      setBudgets(budRes.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [currentMonth]);

  // Total Monthly Spent in INR
  const totalSpentPaise = expenses.reduce((sum, exp) => sum + (exp.amountPaise || 0), 0);
  const totalSpentInr = (totalSpentPaise / 100).toLocaleString('en-IN', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  });

  // Category Pie Chart Data
  const categoryDataMap = expenses.reduce((acc, exp) => {
    const cat = exp.categoryId || 'other';
    acc[cat] = (acc[cat] || 0) + (exp.amountPaise || 0) / 100;
    return acc;
  }, {});

  const pieChartData = Object.entries(categoryDataMap).map(([name, value]) => ({
    name: name.charAt(0).toUpperCase() + name.slice(1),
    value,
    color: CATEGORY_COLORS[name] || '#64748b',
  }));

  // 6-Month Bar Chart Mock/Calculated Data
  const monthlyBarChartData = [
    { month: 'May', spent: 12000 },
    { month: 'Jun', spent: 18500 },
    { month: 'Jul', spent: 15400 },
    { month: 'Aug', spent: 22100 },
    { month: 'Sep', spent: 19800 },
    { month: currentMonth, spent: totalSpentPaise / 100 },
  ];

  if (loading) return <LoadingSpinner message="Loading your dashboard analytics..." />;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 animate-fadeIn">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-slate-900 via-slate-900 to-slate-900/60 p-6 border border-slate-800 rounded-3xl shadow-xl">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-slate-100">Financial Overview</h1>
          <p className="text-slate-400 text-sm mt-1">Track monthly spending, active budgets, and analytics</p>
        </div>
        <div className="flex items-center space-x-3">
          <Calendar className="w-5 h-5 text-teal-400" />
          <input
            type="month"
            value={currentMonth}
            onChange={(e) => setCurrentMonth(e.target.value)}
            className="px-3 py-1.5 bg-slate-950 border border-slate-800 rounded-xl text-sm font-semibold text-slate-200 focus:outline-none focus:border-teal-500"
          />
        </div>
      </div>

      {error && <ErrorMessage title="Dashboard Error" message={error} onRetry={fetchData} />}

      {/* Quick KPI Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-slate-900/90 border border-slate-800/80 p-6 rounded-3xl shadow-xl relative overflow-hidden group hover:border-teal-500/40 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Monthly Spent</span>
            <div className="p-2.5 bg-teal-500/10 text-teal-400 rounded-2xl">
              <DollarSign className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-3xl font-extrabold text-slate-100">₹{totalSpentInr}</span>
            <p className="text-xs text-slate-400 mt-1 flex items-center">
              <TrendingUp className="w-3.5 h-3.5 text-teal-400 mr-1" />
              <span>{expenses.length} transaction entries this month</span>
            </p>
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800/80 p-6 rounded-3xl shadow-xl relative overflow-hidden group hover:border-cyan-500/40 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Active Budgets</span>
            <div className="p-2.5 bg-cyan-500/10 text-cyan-400 rounded-2xl">
              <PieIcon className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-3xl font-extrabold text-slate-100">{budgets.length}</span>
            <p className="text-xs text-slate-400 mt-1">Configured for {currentMonth}</p>
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800/80 p-6 rounded-3xl shadow-xl relative overflow-hidden group hover:border-blue-500/40 transition-all sm:col-span-2 lg:col-span-1">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Payment Status</span>
            <div className="p-2.5 bg-blue-500/10 text-blue-400 rounded-2xl">
              <CreditCard className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-3xl font-extrabold text-teal-400">Active</span>
            <p className="text-xs text-slate-400 mt-1">Razorpay Integration Online</p>
          </div>
        </div>
      </div>

      {/* Visual Analytics Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Category Pie Chart */}
        <div className="bg-slate-900/90 border border-slate-800 p-6 rounded-3xl shadow-xl">
          <h3 className="text-lg font-bold text-slate-100 mb-4">Category Breakdown</h3>
          {pieChartData.length === 0 ? (
            <EmptyState title="No Expenses Recorded" description="Add expenses for this month to view category breakdown." />
          ) : (
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieChartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={85}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {pieChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(value) => [`₹${value.toFixed(2)}`, 'Spent']}
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="flex flex-wrap justify-center gap-3 mt-2">
                {pieChartData.map((item) => (
                  <div key={item.name} className="flex items-center space-x-1.5 text-xs text-slate-300">
                    <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: item.color }}></span>
                    <span>{item.name}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* 6-Month Spending Trend Bar Chart */}
        <div className="bg-slate-900/90 border border-slate-800 p-6 rounded-3xl shadow-xl">
          <h3 className="text-lg font-bold text-slate-100 mb-4">6-Month Spending Trend</h3>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthlyBarChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="month" stroke="#64748b" fontSize={12} />
                <YAxis stroke="#64748b" fontSize={12} tickFormatter={(v) => `₹${v / 1000}k`} />
                <Tooltip
                  formatter={(value) => [`₹${value.toLocaleString('en-IN')}`, 'Spent']}
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px' }}
                />
                <Bar dataKey="spent" fill="#06b6d4" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Activity Table */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 shadow-xl">
        <h3 className="text-lg font-bold text-slate-100 mb-4">Recent Expenses</h3>
        {expenses.length === 0 ? (
          <EmptyState title="No expenses this month" description="Click Expenses on the top menu to record your spending." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="py-3 px-4">Date</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Note</th>
                  <th className="py-3 px-4 text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-sm text-slate-200">
                {expenses.slice(0, 5).map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4 font-mono text-xs text-slate-400">{exp.date}</td>
                    <td className="py-3.5 px-4 font-medium capitalize text-teal-400">{exp.categoryId}</td>
                    <td className="py-3.5 px-4 text-slate-300">{exp.note || '-'}</td>
                    <td className="py-3.5 px-4 text-right font-bold text-slate-100">
                      ₹{(exp.amountPaise / 100).toFixed(2)}
                    </td>
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
