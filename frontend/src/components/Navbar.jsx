import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, 
  Receipt, 
  PieChart, 
  CreditCard, 
  LogOut, 
  Menu, 
  X, 
  ShieldAlert,
  Wallet
} from 'lucide-react';

export default function Navbar() {
  const { currentUser, userRole, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (e) {
      console.error('Logout error:', e);
    }
  };

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { label: 'Expenses', path: '/expenses', icon: Receipt },
    { label: 'Budgets', path: '/budgets', icon: PieChart },
    { label: 'Payments', path: '/payments', icon: CreditCard },
    ...(userRole === 'ADMIN' ? [{ label: 'Admin Panel', path: '/admin', icon: ShieldAlert }] : []),
  ];

  return (
    <header className="sticky top-0 z-40 bg-slate-900/90 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Brand Logo */}
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-gradient-to-tr from-teal-500 to-cyan-400 rounded-xl shadow-lg shadow-teal-500/20 text-slate-950 font-bold">
              <Wallet className="w-5 h-5" />
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-slate-100 via-slate-200 to-teal-400 bg-clip-text text-transparent">
              FinTech Flow
            </span>
          </div>

          {/* Desktop Nav Items */}
          <nav className="hidden md:flex items-center space-x-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) =>
                    `flex items-center space-x-2 px-3.5 py-2 rounded-xl text-sm font-medium transition-all ${
                      isActive
                        ? 'bg-teal-500/10 text-teal-400 border border-teal-500/20'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>

          {/* Right Profile & Logout */}
          <div className="hidden md:flex items-center space-x-4">
            <div className="text-right">
              <p className="text-xs font-semibold text-slate-200">{currentUser?.email || 'User'}</p>
              <div className="flex items-center justify-end space-x-1">
                <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.5 rounded bg-teal-950 text-teal-400 border border-teal-800/40">
                  {userRole || 'USER'}
                </span>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-950/30 rounded-xl border border-transparent hover:border-rose-900/40 transition-all"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden">
            <button
              onClick={() => setMobileOpen(!mobileOpen)}
              className="p-2 text-slate-400 hover:text-slate-200 rounded-lg"
            >
              {mobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileOpen && (
        <div className="md:hidden border-b border-slate-800 bg-slate-900/95 px-4 pt-2 pb-4 space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3 py-2.5 rounded-xl text-base font-medium ${
                    isActive
                      ? 'bg-teal-500/10 text-teal-400'
                      : 'text-slate-400 hover:bg-slate-800'
                  }`
                }
              >
                <Icon className="w-5 h-5" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
          <div className="pt-4 border-t border-slate-800 flex items-center justify-between">
            <span className="text-xs text-slate-400">{currentUser?.email}</span>
            <button
              onClick={handleLogout}
              className="flex items-center space-x-2 text-sm text-rose-400 hover:text-rose-300 font-medium"
            >
              <LogOut className="w-4 h-4" />
              <span>Logout</span>
            </button>
          </div>
        </div>
      )}
    </header>
  );
}
