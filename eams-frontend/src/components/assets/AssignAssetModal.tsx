import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { AssetResponse, EmployeeSummaryResponse } from '../../types';
import { assetApi, employeeApi } from '../../api';
import { useToast } from '../common/Toast';
import { ShieldCheck } from 'lucide-react';

interface AssignAssetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  asset: AssetResponse | null;
}

export const AssignAssetModal: React.FC<AssignAssetModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  asset,
}) => {
  const { success, error: showError } = useToast();
  const [employees, setEmployees] = useState<EmployeeSummaryResponse[]>([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | ''>('');
  const [conditionNotes, setConditionNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [fetchingEmployees, setFetchingEmployees] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setFetchingEmployees(true);
      employeeApi
        .getAllEmployees({ size: 100, status: 'ACTIVE' })
        .then((res) => {
          setEmployees(res.content);
          if (res.content.length > 0) {
            setSelectedEmployeeId(res.content[0].id);
          }
        })
        .catch(() => showError('Failed to load active employees list'))
        .finally(() => setFetchingEmployees(false));

      setConditionNotes('Issued in brand new / working condition with original accessories.');
    }
  }, [isOpen, showError]);

  const handleAssign = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!asset || !selectedEmployeeId) return;

    setLoading(true);
    try {
      await assetApi.assignAsset(asset.id, {
        employeeId: Number(selectedEmployeeId),
        conditionNotes: conditionNotes || undefined,
      });
      success(`Asset ${asset.assetTag} assigned successfully`);
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Assignment failed';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (!asset) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Assign Asset: ${asset.assetTag}`}
      subtitle={`${asset.categoryName} • ${asset.modelName}`}
    >
      <div className="mb-4 p-3 rounded-xl bg-indigo-950/40 border border-indigo-500/20 text-indigo-300 text-xs flex items-start gap-2.5">
        <ShieldCheck className="w-4 h-4 mt-0.5 flex-shrink-0 text-indigo-400" />
        <p>
          <strong>Concurrency Protected:</strong> This action acquires a database row-level pessimistic write lock (<code className="bg-indigo-900/50 px-1 py-0.5 rounded">PESSIMISTIC_WRITE</code>) to prevent double-assignment conflicts.
        </p>
      </div>

      <form onSubmit={handleAssign} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Assign To Active Employee <span className="text-rose-400">*</span>
          </label>
          {fetchingEmployees ? (
            <div className="text-xs text-slate-400 py-2">Loading active employees...</div>
          ) : (
            <select
              required
              value={selectedEmployeeId}
              onChange={(e) => setSelectedEmployeeId(Number(e.target.value))}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            >
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id} className="bg-slate-900 text-white">
                  {emp.fullName} ({emp.employeeCode}) — {emp.departmentName || 'No Department'}
                </option>
              ))}
            </select>
          )}
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Condition & Handover Notes
          </label>
          <textarea
            rows={3}
            value={conditionNotes}
            onChange={(e) => setConditionNotes(e.target.value)}
            placeholder="Describe hardware condition, included chargers/accessories, etc."
            className="w-full px-3.5 py-2 rounded-xl text-sm glass-input resize-none"
          />
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800/80 text-slate-300 hover:bg-slate-800 hover:text-white transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading || fetchingEmployees || employees.length === 0}
            className="px-5 py-2 text-xs font-medium rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-500/25 transition-all disabled:opacity-50"
          >
            {loading ? 'Assigning...' : 'Confirm Assignment'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
