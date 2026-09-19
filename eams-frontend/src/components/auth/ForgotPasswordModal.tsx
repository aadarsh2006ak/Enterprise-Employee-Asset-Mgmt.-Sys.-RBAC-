import React, { useState } from 'react';
import { 
  X, 
  KeyRound, 
  User, 
  Lock, 
  Eye, 
  EyeOff, 
  ArrowRight, 
  ArrowLeft, 
  CheckCircle2, 
  AlertCircle,
  ShieldCheck,
  Check
} from 'lucide-react';
import { authApi } from '../../api/endpoints/auth';
import { useToast } from '../common/Toast';

interface ForgotPasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (username: string) => void;
}

export const ForgotPasswordModal: React.FC<ForgotPasswordModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const { error: showError, success: showSuccess } = useToast();

  const [step, setStep] = useState<'verify' | 'reset' | 'success'>('verify');
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [loading, setLoading] = useState(false);

  // Verification result
  const [resetToken, setResetToken] = useState('');
  const [verifiedUsername, setVerifiedUsername] = useState('');
  const [maskedEmail, setMaskedEmail] = useState('');

  // New Password state
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  if (!isOpen) return null;

  // Password rules validation
  const hasMinLength = newPassword.length >= 8;
  const hasUppercase = /[A-Z]/.test(newPassword);
  const hasLowercase = /[a-z]/.test(newPassword);
  const hasNumber = /[0-9]/.test(newPassword);
  const hasSpecial = /[@#$%^&+=!._-]/.test(newPassword);
  const passwordsMatch = newPassword.length > 0 && newPassword === confirmPassword;
  const isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasNumber && hasSpecial && passwordsMatch;

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!usernameOrEmail.trim()) {
      showError('Please enter your company username or email address.');
      return;
    }

    setLoading(true);
    try {
      const data = await authApi.verifyForgotPassword({ usernameOrEmail: usernameOrEmail.trim() });
      setResetToken(data.resetToken);
      setVerifiedUsername(data.username);
      setMaskedEmail(data.maskedEmail);
      setStep('reset');
      showSuccess('Account verified. Please set your new password.');
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Could not verify account. Please check your username/email.';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isPasswordValid) {
      showError('Please ensure password meets all enterprise complexity requirements.');
      return;
    }

    setLoading(true);
    try {
      await authApi.resetPassword({
        resetToken,
        newPassword,
        confirmPassword,
      });
      setStep('success');
      showSuccess('Password has been reset successfully!');
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to update password. Please try again.';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleFinish = () => {
    onSuccess(verifiedUsername || usernameOrEmail);
    handleClose();
  };

  const handleClose = () => {
    setStep('verify');
    setUsernameOrEmail('');
    setResetToken('');
    setVerifiedUsername('');
    setMaskedEmail('');
    setNewPassword('');
    setConfirmPassword('');
    setShowNewPassword(false);
    setShowConfirmPassword(false);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in font-sans">
      <div className="relative w-full max-w-md rounded-3xl glass-panel p-6 sm:p-8 shadow-glass border border-slate-200 dark:border-white/[0.1] overflow-hidden">
        {/* Background gradient decorative element */}
        <div className="absolute -top-16 -right-16 w-36 h-36 bg-rose-500/10 dark:bg-rose-500/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-16 -left-16 w-36 h-36 bg-indigo-500/10 dark:bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />

        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 mb-5 border-b border-slate-200 dark:border-white/[0.08]">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl gradient-accent flex items-center justify-center shadow-md">
              <KeyRound className="w-5 h-5 text-white" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                {step === 'verify' && 'Forgot Password'}
                {step === 'reset' && 'Set New Password'}
                {step === 'success' && 'Password Changed'}
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                {step === 'verify' && 'Verify your company assigned account'}
                {step === 'reset' && `Updating password for ${verifiedUsername}`}
                {step === 'success' && 'Your account security has been updated'}
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-white/[0.05] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* STEP 1: Verify Account */}
        {step === 'verify' && (
          <form onSubmit={handleVerify} className="space-y-4">
            <div className="p-3.5 rounded-xl bg-slate-100 dark:bg-white/[0.03] border border-slate-200 dark:border-white/[0.08] text-xs text-slate-600 dark:text-slate-300 flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 text-indigo-500 flex-shrink-0 mt-0.5" />
              <span>
                Enter the company username or email address provided by your HR/Administrator.
              </span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider text-[10px]">
                Username or Company Email
              </label>
              <div className="relative">
                <User className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  autoFocus
                  value={usernameOrEmail}
                  onChange={(e) => setUsernameOrEmail(e.target.value)}
                  placeholder="e.g. admin, manager, employee or user@company.com"
                  className="w-full pl-10 pr-3.5 py-2.5 rounded-xl text-xs sm:text-sm glass-input font-medium"
                />
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="button"
                onClick={handleClose}
                className="w-1/3 py-2.5 px-4 rounded-xl text-xs font-bold text-slate-700 dark:text-slate-300 bg-slate-100 dark:bg-white/[0.05] hover:bg-slate-200 dark:hover:bg-white/[0.1] transition-all"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading || !usernameOrEmail.trim()}
                className="w-2/3 py-2.5 px-4 rounded-xl text-xs font-bold text-white btn-theme-primary flex items-center justify-center gap-2 transition-all disabled:opacity-50"
              >
                {loading ? (
                  <span>Verifying...</span>
                ) : (
                  <>
                    <span>Verify Account</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </div>
          </form>
        )}

        {/* STEP 2: Reset Password */}
        {step === 'reset' && (
          <form onSubmit={handleResetPassword} className="space-y-4">
            {/* Account Confirmation Badge */}
            <div className="p-3 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-between">
              <div>
                <div className="text-xs font-bold text-indigo-600 dark:text-indigo-400 flex items-center gap-1.5">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  Verified: <span className="font-mono">{verifiedUsername}</span>
                </div>
                <div className="text-[11px] text-slate-500 dark:text-slate-400 font-mono mt-0.5">
                  {maskedEmail}
                </div>
              </div>
              <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 flex items-center gap-1">
                <Check className="w-3 h-3" /> Active
              </span>
            </div>

            {/* New Password */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider text-[10px]">
                New Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type={showNewPassword ? 'text' : 'password'}
                  required
                  autoFocus
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new strong password"
                  className="w-full pl-10 pr-10 py-2.5 rounded-xl text-xs sm:text-sm glass-input font-medium"
                />
                <button
                  type="button"
                  onClick={() => setShowNewPassword(!showNewPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-1"
                >
                  {showNewPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Confirm Password */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5 uppercase tracking-wider text-[10px]">
                Confirm New Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter new password"
                  className="w-full pl-10 pr-10 py-2.5 rounded-xl text-xs sm:text-sm glass-input font-medium"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-1"
                >
                  {showConfirmPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Password Complexity Checklist */}
            <div className="p-3 rounded-xl bg-slate-100 dark:bg-black/30 border border-slate-200 dark:border-white/[0.08] space-y-1.5 text-[11px]">
              <div className="font-semibold text-slate-700 dark:text-slate-300 text-[10px] uppercase tracking-wider mb-1">
                Password Requirements:
              </div>
              <div className="grid grid-cols-2 gap-1">
                <div className={`flex items-center gap-1.5 ${hasMinLength ? 'text-emerald-500 font-semibold' : 'text-slate-400'}`}>
                  <Check className={`w-3 h-3 ${hasMinLength ? 'opacity-100' : 'opacity-40'}`} />
                  <span>8+ characters</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasUppercase ? 'text-emerald-500 font-semibold' : 'text-slate-400'}`}>
                  <Check className={`w-3 h-3 ${hasUppercase ? 'opacity-100' : 'opacity-40'}`} />
                  <span>Uppercase letter</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasLowercase ? 'text-emerald-500 font-semibold' : 'text-slate-400'}`}>
                  <Check className={`w-3 h-3 ${hasLowercase ? 'opacity-100' : 'opacity-40'}`} />
                  <span>Lowercase letter</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasNumber ? 'text-emerald-500 font-semibold' : 'text-slate-400'}`}>
                  <Check className={`w-3 h-3 ${hasNumber ? 'opacity-100' : 'opacity-40'}`} />
                  <span>Number (0-9)</span>
                </div>
                <div className={`flex items-center gap-1.5 ${hasSpecial ? 'text-emerald-500 font-semibold' : 'text-slate-400'}`}>
                  <Check className={`w-3 h-3 ${hasSpecial ? 'opacity-100' : 'opacity-40'}`} />
                  <span>Special char (@#$%)</span>
                </div>
                <div className={`flex items-center gap-1.5 ${passwordsMatch ? 'text-emerald-500 font-semibold' : 'text-slate-400'}`}>
                  <Check className={`w-3 h-3 ${passwordsMatch ? 'opacity-100' : 'opacity-40'}`} />
                  <span>Passwords match</span>
                </div>
              </div>
            </div>

            <div className="flex gap-3 pt-2">
              <button
                type="button"
                onClick={() => setStep('verify')}
                className="w-1/3 py-2.5 px-3 rounded-xl text-xs font-bold text-slate-700 dark:text-slate-300 bg-slate-100 dark:bg-white/[0.05] hover:bg-slate-200 dark:hover:bg-white/[0.1] transition-all flex items-center justify-center gap-1"
              >
                <ArrowLeft className="w-3.5 h-3.5" /> Back
              </button>
              <button
                type="submit"
                disabled={loading || !isPasswordValid}
                className="w-2/3 py-2.5 px-4 rounded-xl text-xs font-bold text-white btn-theme-primary flex items-center justify-center gap-2 transition-all disabled:opacity-50"
              >
                {loading ? (
                  <span>Updating...</span>
                ) : (
                  <>
                    <span>Save New Password</span>
                    <Check className="w-4 h-4" />
                  </>
                )}
              </button>
            </div>
          </form>
        )}

        {/* STEP 3: Success State */}
        {step === 'success' && (
          <div className="text-center py-4 space-y-4 animate-fade-in">
            <div className="w-16 h-16 mx-auto rounded-2xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-500 shadow-lg animate-bounce">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <div className="space-y-1">
              <h4 className="text-base font-bold text-slate-900 dark:text-white">
                Password Successfully Reset!
              </h4>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed max-w-xs mx-auto">
                Your password has been updated and all previous sessions have been secured. You can now sign in with your new credentials.
              </p>
            </div>

            <button
              type="button"
              onClick={handleFinish}
              className="w-full py-3 px-4 rounded-xl text-xs sm:text-sm font-bold text-white btn-theme-primary flex items-center justify-center gap-2 transition-all shadow-md mt-2"
            >
              <span>Back to Sign In</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ForgotPasswordModal;
