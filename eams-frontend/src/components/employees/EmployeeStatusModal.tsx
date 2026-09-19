import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { EmployeeSummaryResponse, EmployeeStatus } from '../../types';
import { employeeApi } from '../../api';
import { useToast } from '../common/Toast';
import { AlertTriangle } from 'lucide-react';

interface EmployeeStatusModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  employee: EmployeeSummaryResponse | null;
}

export const EmployeeStatusModal: React.FC<EmployeeStatusModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  employee,
}) => {
  const { success, error: showError } = useToast();
  const [status, setStatus] = useState<EmployeeStatus>(employee?.status || 'ACTIVE');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!employee) return;

    setLoading(true);
    try {
      await employeeApi.updateStatus(employee.id, { status });
      success(`Status of ${employee.fullName} updated to ${status}`);
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Status update failed';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (!employee) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Update Employment Lifecycle: ${employee.fullName}`}
      subtitle={`${employee.employeeCode} • Account: ${employee.username}`}
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 mb-2">
            Select Employment Status <span className="text-rose-400">*</span>
          </label>
          <div className="space-y-2">
            {[
              {
                value: 'ACTIVE',
                label: 'ACTIVE',
                desc: 'Employee is in good standing. System user account remains unlocked.',
                color: 'border-emerald-500/40 bg-emerald-950/20 text-emerald-300',
              },
              {
                value: 'RESIGNED',
                label: 'RESIGNED',
                desc: 'Employee submitted resignation / offboarding. User account is locked immediately.',
                color: 'border-amber-500/40 bg-amber-950/20 text-amber-300',
              },
              {
                value: 'TERMINATED',
                label: 'TERMINATED',
                desc: 'Employment terminated. User account is immediately revoked and blocked.',
                color: 'border-rose-500/40 bg-rose-950/20 text-rose-300',
              },
            ].map((opt) => (
              <label
                key={opt.value}
                className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer transition-all ${
                  status === opt.value
                    ? `${opt.color} ring-1 ring-indigo-500`
                    : 'border-slate-800 bg-slate-900/60 text-slate-400 hover:border-slate-700'
                }`}
              >
                <input
                  type="radio"
                  name="status"
                  value={opt.value}
                  checked={status === opt.value}
                  onChange={(e) => setStatus(e.target.value as EmployeeStatus)}
                  className="mt-1"
                />
                <div>
                  <span className="text-xs font-bold text-white tracking-wide">{opt.label}</span>
                  <p className="text-[11px] text-slate-400 mt-0.5 leading-4">{opt.desc}</p>
                </div>
              </label>
            ))}
          </div>
        </div>

        {status !== 'ACTIVE' && (
          <div className="p-3 rounded-xl bg-amber-950/40 border border-amber-500/30 text-amber-300 text-xs flex items-start gap-2.5">
            <AlertTriangle className="w-4 h-4 text-amber-400 flex-shrink-0 mt-0.5" />
            <p>
              <strong>Security Policy:</strong> Setting status to <em>{status}</em> will invalidate active JWT refresh tokens and prevent user login.
            </p>
          </div>
        )}

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-5 py-2 text-xs font-medium rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-500/25 transition-all disabled:opacity-50"
          >
            {loading ? 'Updating...' : 'Confirm Status Change'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
