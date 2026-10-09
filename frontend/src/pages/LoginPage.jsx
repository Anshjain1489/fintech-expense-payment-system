import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Wallet, LogIn, Lock, Mail, ArrowRight, ShieldCheck } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, loginWithGoogle, loginDevMock } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      setLoading(true);
      await login(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to sign in. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    try {
      setLoading(true);
      await loginWithGoogle();
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to sign in with Google.');
    } finally {
      setLoading(false);
    }
  };

  const handleDevMock = (role) => {
    loginDevMock(role);
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-slate-950 relative overflow-hidden">
      {/* Dynamic Ambient Background Glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-teal-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md bg-slate-900/90 border border-slate-800 rounded-3xl p-8 shadow-2xl backdrop-blur-xl z-10">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 bg-gradient-to-tr from-teal-500 to-cyan-400 rounded-2xl shadow-lg shadow-teal-500/20 text-slate-950 font-bold mb-4">
            <Wallet className="w-7 h-7" />
          </div>
          <h1 className="text-2xl font-bold text-slate-100">Welcome Back</h1>
          <p className="text-slate-400 text-sm mt-1">Sign in to your FinTech management account</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-rose-950/50 border border-rose-800/60 rounded-xl text-xs text-rose-300">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Email Address
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                <Mail className="w-4 h-4" />
              </span>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@company.com"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none focus:ring-1 focus:ring-teal-500 transition-colors"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Password
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                <Lock className="w-4 h-4" />
              </span>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-800 focus:border-teal-500 rounded-xl text-slate-100 text-sm focus:outline-none focus:ring-1 focus:ring-teal-500 transition-colors"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 px-4 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-400 hover:to-cyan-400 text-slate-950 font-bold rounded-xl text-sm transition-all shadow-lg shadow-teal-500/20 active:scale-[0.99] flex items-center justify-center space-x-2 disabled:opacity-50"
          >
            <span>{loading ? 'Authenticating...' : 'Sign In'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="mt-6 pt-6 border-t border-slate-800 text-center">
          <p className="text-xs text-slate-400 mb-3">Or quick dev test login (emulator / test mode):</p>
          <div className="grid grid-cols-2 gap-3 mb-6">
            <button
              onClick={() => handleDevMock('user')}
              className="py-2 px-3 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold border border-slate-700/60 transition-colors"
            >
              Demo User
            </button>
            <button
              onClick={() => handleDevMock('admin')}
              className="py-2 px-3 bg-slate-800 hover:bg-slate-700 text-teal-400 rounded-xl text-xs font-semibold border border-teal-800/40 transition-colors"
            >
              Demo Admin
            </button>
          </div>

          <p className="text-sm text-slate-400">
            Don't have an account?{' '}
            <Link to="/signup" className="text-teal-400 font-semibold hover:underline">
              Create account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
