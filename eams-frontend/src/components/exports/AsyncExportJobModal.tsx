import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { ExportJobRequest, ExportFormat } from '../../types';
import { exportApi } from '../../api';
import { useToast } from '../common/Toast';
import { Cpu, FileText, Table } from 'lucide-react';

interface AsyncExportJobModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const AsyncExportJobModal: React.FC<AsyncExportJobModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const { success, error: showError } = useToast();
  const [dataset, setDataset] = useState<ExportJobRequest['dataset']>('ASSETS');
  const [format, setFormat] = useState<ExportFormat>('CSV');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await exportApi.createExportJob({
        dataset,
        format,
      });
      success(`Async background export job for ${dataset} submitted to executor pool`);
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to submit export job';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Dispatch Async Export Job"
      subtitle="Background Bulkhead thread pool executor with progress polling"
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Dataset to Export <span className="text-rose-400">*</span>
          </label>
          <select
            value={dataset}
            onChange={(e) => setDataset(e.target.value as ExportJobRequest['dataset'])}
            className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
          >
            <option value="ASSETS" className="bg-slate-900 text-white">Assets Inventory & Hardware</option>
            <option value="EMPLOYEES" className="bg-slate-900 text-white">Employees Directory</option>
            <option value="DEPARTMENTS" className="bg-slate-900 text-white">Departments & Headcount</option>
            <option value="ASSIGNMENTS" className="bg-slate-900 text-white">Asset Assignments History</option>
            <option value="AUDIT_LOGS" className="bg-slate-900 text-white">Security Audit Trail Logs</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Export Format <span className="text-rose-400">*</span>
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setFormat('CSV')}
              className={`flex items-center gap-2 p-3 rounded-xl border text-xs font-medium transition-all ${
                format === 'CSV'
                  ? 'border-indigo-500 bg-indigo-500/10 text-indigo-300 ring-1 ring-indigo-500'
                  : 'border-slate-800 bg-slate-900 text-slate-400 hover:border-slate-700'
              }`}
            >
              <FileText className="w-4 h-4 text-indigo-400" />
              <span>CSV (Streaming)</span>
            </button>

            <button
              type="button"
              onClick={() => setFormat('XLSX')}
              className={`flex items-center gap-2 p-3 rounded-xl border text-xs font-medium transition-all ${
                format === 'XLSX'
                  ? 'border-emerald-500 bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500'
                  : 'border-slate-800 bg-slate-900 text-slate-400 hover:border-slate-700'
              }`}
            >
              <Table className="w-4 h-4 text-emerald-400" />
              <span>Excel (.XLSX)</span>
            </button>
          </div>
        </div>

        <div className="p-3 rounded-xl bg-slate-850 border border-slate-700/60 text-xs text-slate-400 flex items-start gap-2.5">
          <Cpu className="w-4 h-4 text-indigo-400 flex-shrink-0 mt-0.5" />
          <p>
            Async export jobs run in a bounded background thread pool without blocking active web HTTP request handlers.
          </p>
        </div>

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
            {loading ? 'Submitting...' : 'Dispatch Export Job'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
