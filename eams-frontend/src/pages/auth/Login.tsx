import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ShieldCheck, Lock, User, ArrowRight, Eye, EyeOff, CheckCircle2, Sparkles } from 'lucide-react';
import { useToast } from '../../components/common/Toast';
import { ThemeToggle } from '../../components/common/ThemeToggle';
import { ForgotPasswordModal } from '../../components/auth/ForgotPasswordModal';

export const Login: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { error: showError, success: showSuccess } = useToast();

  const [usernameOrEmail, setUsernameOrEmail] = useState('admin');
  const [password, setPassword] = useState('Admin@123');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isForgotOpen, setIsForgotOpen] = useState(false);


  const from = (location.state as any)?.from?.pathname || '/';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await login({ usernameOrEmail, password });
      showSuccess('Authentication successful! Welcome to EAMS.');
      navigate(from, { replace: true });
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Invalid credentials';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickDemo = (user: string, pass: string) => {
    setUsernameOrEmail(user);
    setPassword(pass);
  };

  return (
    <div className="min-h-screen ambient-mesh-bg flex flex-col justify-center items-center p-4 relative overflow-hidden font-sans selection:bg-indigo-500 selection:text-white">
      {/* Top Right Theme Selector */}
      <div className="absolute top-5 right-5 z-20">
        <ThemeToggle />
      </div>

      {/* Decorative subtle grid overlay */}
      <div 
        className="absolute inset-0 bg-[linear-gradient(to_right,#00000008_1px,transparent_1px),linear-gradient(to_bottom,#00000008_1px,transparent_1px)] dark:bg-[linear-gradient(to_right,#ffffff05_1px,transparent_1px),linear-gradient(to_bottom,#ffffff05_1px,transparent_1px)] bg-[size:32px_32px] pointer-events-none" 
      />

      <div className="w-full max-w-md z-10 space-y-6">
        {/* Brand Logo & Header */}
        <div className="text-center space-y-3">
          <div className="inline-flex p-1 rounded-2xl gradient-accent shadow-xl mb-1 animate-float">
            <div className="w-14 h-14 bg-white dark:bg-slate-950 rounded-[14px] flex items-center justify-center shadow-inner">
              <ShieldCheck className="w-8 h-8 text-indigo-600 dark:text-white" />
            </div>
          </div>
          <div>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-white tracking-tight">
              Enterprise Asset Portal
            </h2>
            <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 font-medium">
              Role-Based Access Control (RBAC) & Inventory Management
            </p>
          </div>
        </div>

        {/* Main Card */}
        <div className="glass-panel p-8 rounded-3xl shadow-glass space-y-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider text-[10px]">
                Username or Email Address
              </label>
              <div className="relative">
                <User className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  placeholder="admin or user@company.com"
                  className="w-full pl-10 pr-3.5 py-2.5 rounded-xl text-xs sm:text-sm glass-input font-medium"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider text-[10px]">
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => setIsForgotOpen(true)}
                  className="text-[11px] font-semibold text-indigo-600 dark:text-indigo-400 hover:text-indigo-700 dark:hover:text-indigo-300 hover:underline transition-all"
                >
                  Forgot Password?
                </button>
              </div>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-10 py-2.5 rounded-xl text-xs sm:text-sm glass-input font-medium"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-1"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl font-bold text-xs sm:text-sm text-white btn-theme-primary flex items-center justify-center gap-2 transition-all duration-200 active:scale-[0.98] disabled:opacity-50"
            >
              {loading ? (
                <span>Authenticating...</span>
              ) : (
                <>
                  <span>Sign In to Dashboard</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Quick Demo Credentials */}
          <div className="pt-4 border-t border-slate-200 dark:border-white/[0.08] space-y-3">
            <div className="flex items-center justify-between">
              <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">
                Quick Demo Role Login
              </p>
              <span className="text-[10px] text-indigo-600 dark:text-indigo-400 font-semibold flex items-center gap-1">
                <Sparkles className="w-3 h-3" /> Auto-Fill
              </span>
            </div>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => handleQuickDemo('admin', 'Admin@123')}
                className="p-2.5 rounded-xl bg-slate-100 dark:bg-black/40 hover:bg-slate-200 dark:hover:bg-black/70 border border-slate-200 dark:border-white/[0.1] hover:border-rose-500/60 text-left transition-all group shadow-sm"
              >
                <div className="flex items-center gap-1 text-[11px] font-bold text-rose-500 dark:text-rose-400">
                  <CheckCircle2 className="w-3 h-3 flex-shrink-0" /> Admin
                </div>
                <div className="text-[10px] text-slate-500 dark:text-slate-400 font-mono mt-1">admin</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickDemo('manager', 'Manager@123')}
                className="p-2.5 rounded-xl bg-slate-100 dark:bg-black/40 hover:bg-slate-200 dark:hover:bg-black/70 border border-slate-200 dark:border-white/[0.1] hover:border-indigo-500/60 text-left transition-all group shadow-sm"
              >
                <div className="flex items-center gap-1 text-[11px] font-bold text-indigo-600 dark:text-indigo-400">
                  <CheckCircle2 className="w-3 h-3 flex-shrink-0" /> Manager
                </div>
                <div className="text-[10px] text-slate-500 dark:text-slate-400 font-mono mt-1">manager</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickDemo('employee', 'Employee@123')}
                className="p-2.5 rounded-xl bg-slate-100 dark:bg-black/40 hover:bg-slate-200 dark:hover:bg-black/70 border border-slate-200 dark:border-white/[0.1] hover:border-emerald-500/60 text-left transition-all group shadow-sm"
              >
                <div className="flex items-center gap-1 text-[11px] font-bold text-emerald-600 dark:text-emerald-400">
                  <CheckCircle2 className="w-3 h-3 flex-shrink-0" /> Employee
                </div>
                <div className="text-[10px] text-slate-500 dark:text-slate-400 font-mono mt-1">employee</div>
              </button>
            </div>
          </div>
        </div>

        {/* Footer info */}
        <p className="text-center text-[11px] text-slate-500 font-medium">
          Enterprise Employee & Asset Management System • Spring Boot 3 & React
        </p>
      </div>

      {/* Forgot Password Modal */}
      <ForgotPasswordModal
        isOpen={isForgotOpen}
        onClose={() => setIsForgotOpen(false)}
        onSuccess={(user) => {
          setUsernameOrEmail(user);
          setPassword('');
          showSuccess('Password reset successful! Please sign in with your new password.');
        }}
      />
    </div>
  );
};

export default Login;

