import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { AssetResponse } from '../../types';
import { assetApi } from '../../api';
import { useToast } from '../common/Toast';
import { RotateCcw } from 'lucide-react';

interface ReturnAssetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  asset: AssetResponse | null;
}

export const ReturnAssetModal: React.FC<ReturnAssetModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  asset,
}) => {
  const { success, error: showError } = useToast();
  const [conditionNotes, setConditionNotes] = useState('Returned in good working condition, inspected and verified.');
  const [loading, setLoading] = useState(false);

  const handleReturn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!asset) return;

    setLoading(true);
    try {
      await assetApi.returnAsset(asset.id, {
        conditionNotes: conditionNotes || undefined,
      });
      success(`Asset ${asset.assetTag} successfully returned to inventory`);
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Return operation failed';
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
      title={`Accept Return: ${asset.assetTag}`}
      subtitle={`Conclude assignment and restore asset to Available status`}
    >
      <div className="mb-4 p-3 rounded-xl bg-slate-800/80 border border-slate-700/60 text-xs space-y-1.5">
        <p className="text-slate-400">
          <strong>Equipment:</strong> <span className="text-white">{asset.categoryName} • {asset.modelName}</span>
        </p>
        {asset.activeAssignment && (
          <p className="text-slate-400">
            <strong>Currently Assigned To:</strong>{' '}
            <span className="text-indigo-300 font-medium">
              {asset.activeAssignment.employeeName} ({asset.activeAssignment.employeeCode})
            </span>
          </p>
        )}
      </div>

      <form onSubmit={handleReturn} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Return Inspection & Condition Notes
          </label>
          <textarea
            rows={3}
            value={conditionNotes}
            onChange={(e) => setConditionNotes(e.target.value)}
            placeholder="Record any physical damages, battery health, missing items, etc."
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
            disabled={loading}
            className="flex items-center gap-1.5 px-5 py-2 text-xs font-medium rounded-xl bg-amber-600 hover:bg-amber-500 text-white shadow-lg shadow-amber-600/25 transition-all disabled:opacity-50"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>{loading ? 'Processing...' : 'Accept Return'}</span>
          </button>
        </div>
      </form>
    </Modal>
  );
};
