import React, { useEffect, useState } from 'react';
import { AdminAPI } from '../services/api';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import { ShieldCheck, Users, CreditCard, History, Lock } from 'lucide-react';

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState('transactions'); // transactions, users, audit
  const [transactions, setTransactions] = useState([]);
  const [users, setUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchAdminData = async () => {
    try {
      setLoading(true);
      setError('');
      if (activeTab === 'transactions') {
        const res = await AdminAPI.getTransactions();
        setTransactions(res.data || []);
      } else if (activeTab === 'users') {
        const res = await AdminAPI.getUsers();
        setUsers(res.data || []);
      } else if (activeTab === 'audit') {
        const res = await AdminAPI.getAuditLogs();
        setAuditLogs(res.data || []);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Access Denied: Requires ROLE_ADMIN authority');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminData();
  }, [activeTab]);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6 animate-fadeIn">
      {/* Header */}
      <div className="bg-slate-900/90 border border-slate-800 p-6 rounded-3xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center space-x-2">
            <ShieldCheck className="w-6 h-6 text-teal-400" />
            <span>Admin Control Panel</span>
          </h1>
          <p className="text-slate-400 text-sm mt-1">System-wide transaction audits, user management, and event trails</p>
        </div>
        <div className="flex items-center space-x-2 px-3 py-1.5 bg-rose-950/60 border border-rose-800/60 rounded-xl text-rose-300 text-xs font-semibold">
          <Lock className="w-4 h-4 text-rose-400" />
          <span>ROLE_ADMIN Enforced</span>
        </div>
      </div>

      {error && <ErrorMessage title="Permission Error" message={error} onRetry={fetchAdminData} />}

      {/* Tabs */}
      <div className="flex border-b border-slate-800 space-x-4">
        <button
          onClick={() => setActiveTab('transactions')}
          className={`flex items-center space-x-2 pb-3 text-sm font-semibold border-b-2 transition-colors ${
            activeTab === 'transactions'
              ? 'border-teal-400 text-teal-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <CreditCard className="w-4 h-4" />
          <span>All Transactions ({transactions.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('users')}
          className={`flex items-center space-x-2 pb-3 text-sm font-semibold border-b-2 transition-colors ${
            activeTab === 'users'
              ? 'border-teal-400 text-teal-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Users className="w-4 h-4" />
          <span>System Users ({users.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('audit')}
          className={`flex items-center space-x-2 pb-3 text-sm font-semibold border-b-2 transition-colors ${
            activeTab === 'audit'
              ? 'border-teal-400 text-teal-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <History className="w-4 h-4" />
          <span>System Audit Logs ({auditLogs.length})</span>
        </button>
      </div>

      {/* Tab Content */}
      {loading ? (
        <LoadingSpinner message="Fetching admin system records..." />
      ) : activeTab === 'transactions' ? (
        transactions.length === 0 ? (
          <EmptyState title="No system transactions" description="No user transactions logged yet." />
        ) : (
          <div className="bg-slate-900/90 border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm text-slate-200">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/60 text-xs font-semibold text-slate-400 uppercase">
                    <th className="py-4 px-6">User ID</th>
                    <th className="py-4 px-6">Razorpay Order ID</th>
                    <th className="py-4 px-6">Payment ID</th>
                    <th className="py-4 px-6 text-right">Amount (₹)</th>
                    <th className="py-4 px-6 text-center">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="hover:bg-slate-800/40">
                      <td className="py-4 px-6 font-mono text-xs text-slate-300">{tx.userId}</td>
                      <td className="py-4 px-6 font-mono text-xs text-teal-400">{tx.razorpayOrderId}</td>
                      <td className="py-4 px-6 font-mono text-xs text-slate-400">{tx.razorpayPaymentId || '-'}</td>
                      <td className="py-4 px-6 text-right font-bold text-slate-100">
                        ₹{(tx.amountPaise / 100).toFixed(2)}
                      </td>
                      <td className="py-4 px-6 text-center uppercase font-bold text-xs">
                        <span className={`px-2.5 py-1 rounded-full border ${
                          tx.status === 'paid' ? 'bg-teal-950 text-teal-400 border-teal-800/60' : 'bg-slate-800 text-slate-300 border-slate-700'
                        }`}>
                          {tx.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      ) : activeTab === 'users' ? (
        users.length === 0 ? (
          <EmptyState title="No registered users" description="No user documents found in Firestore." />
        ) : (
          <div className="bg-slate-900/90 border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm text-slate-200">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/60 text-xs font-semibold text-slate-400 uppercase">
                    <th className="py-4 px-6">UID</th>
                    <th className="py-4 px-6">Email</th>
                    <th className="py-4 px-6">Role</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {users.map((u) => (
                    <tr key={u.uid} className="hover:bg-slate-800/40">
                      <td className="py-4 px-6 font-mono text-xs text-slate-400">{u.uid}</td>
                      <td className="py-4 px-6 font-semibold text-slate-200">{u.email || 'N/A'}</td>
                      <td className="py-4 px-6">
                        <span className="uppercase text-xs font-bold px-2 py-0.5 rounded bg-teal-950 text-teal-400 border border-teal-800/40">
                          {u.role || 'USER'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      ) : (
        auditLogs.length === 0 ? (
          <EmptyState title="No audit logs" description="No financial audit logs recorded yet." />
        ) : (
          <div className="bg-slate-900/90 border border-slate-800 rounded-3xl overflow-hidden shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm text-slate-200">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/60 text-xs font-semibold text-slate-400 uppercase">
                    <th className="py-4 px-6">Timestamp</th>
                    <th className="py-4 px-6">User ID</th>
                    <th className="py-4 px-6">Action</th>
                    <th className="py-4 px-6">Entity ID</th>
                    <th className="py-4 px-6">Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {auditLogs.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-800/40">
                      <td className="py-4 px-6 font-mono text-xs text-slate-400">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td className="py-4 px-6 font-mono text-xs text-slate-300">{log.userId}</td>
                      <td className="py-4 px-6 font-bold text-teal-400 text-xs">{log.action}</td>
                      <td className="py-4 px-6 font-mono text-xs text-slate-400">{log.entityId}</td>
                      <td className="py-4 px-6 text-slate-300 text-xs">{log.details}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      )}
    </div>
  );
}
