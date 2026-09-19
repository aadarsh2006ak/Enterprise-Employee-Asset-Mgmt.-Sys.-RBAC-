import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { AssetResponse, AssetAssignmentResponse } from '../../types';
import { assetApi } from '../../api';
import { Calendar, User, CheckCircle, Clock } from 'lucide-react';

interface AssetHistoryTimelineModalProps {
  isOpen: boolean;
  onClose: () => void;
  asset: AssetResponse | null;
}

export const AssetHistoryTimelineModal: React.FC<AssetHistoryTimelineModalProps> = ({
  isOpen,
  onClose,
  asset,
}) => {
  const [history, setHistory] = useState<AssetAssignmentResponse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && asset) {
      setLoading(true);
      assetApi
        .getAssetHistory(asset.id)
        .then((data) => setHistory(data))
        .catch(() => setHistory([]))
        .finally(() => setLoading(false));
    }
  }, [isOpen, asset]);

  if (!asset) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Assignment Timeline: ${asset.assetTag}`}
      subtitle={`${asset.categoryName} • ${asset.modelName}`}
      maxWidth="2xl"
    >
      {loading ? (
        <div className="py-12 flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading historical timeline...</p>
        </div>
      ) : history.length === 0 ? (
        <div className="py-12 text-center text-slate-400 text-sm">
          No assignment records found for this asset.
        </div>
      ) : (
        <div className="relative pl-6 space-y-6 before:absolute before:left-2 before:top-3 before:bottom-3 before:w-0.5 before:bg-slate-800">
          {history.map((record, index) => {
            const isActive = !record.returnedAt;
            return (
              <div key={record.id || index} className="relative group">
                {/* Timeline node icon */}
                <div
                  className={`absolute -left-6 top-1 w-4 h-4 rounded-full border-2 flex items-center justify-center ${
                    isActive
                      ? 'border-indigo-400 bg-indigo-950 ring-4 ring-indigo-500/20'
                      : 'border-slate-600 bg-slate-900'
                  }`}
                />

                <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800/80 shadow-md">
                  <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2">
                      <User className="w-4 h-4 text-indigo-400" />
                      <span className="text-sm font-semibold text-white">
                        {record.employeeName}
                      </span>
                      <span className="text-xs text-slate-400 font-mono">
                        ({record.employeeCode})
                      </span>
                    </div>
                    {isActive ? (
                      <span className="text-[11px] font-medium px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center gap-1">
                        <Clock className="w-3 h-3" /> Active Assignment
                      </span>
                    ) : (
                      <span className="text-[11px] font-medium px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700/60 flex items-center gap-1">
                        <CheckCircle className="w-3 h-3 text-slate-400" /> Returned
                      </span>
                    )}
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-slate-400 mb-2">
                    <div className="flex items-center gap-1.5">
                      <Calendar className="w-3.5 h-3.5 text-slate-500" />
                      <span>
                        Assigned: {new Date(record.assignedAt).toLocaleString()}
                      </span>
                    </div>
                    {record.returnedAt && (
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-slate-500" />
                        <span>
                          Returned: {new Date(record.returnedAt).toLocaleString()}
                        </span>
                      </div>
                    )}
                  </div>

                  {record.conditionNotes && (
                    <div className="p-2.5 rounded-lg bg-slate-950/60 text-xs text-slate-300 font-mono border border-slate-800/60">
                      <p className="text-[10px] text-slate-500 uppercase tracking-wider mb-1 font-sans">
                        Notes:
                      </p>
                      {record.conditionNotes}
                    </div>
                  )}

                  <div className="text-[10px] text-slate-500 mt-2 text-right">
                    Assigned by: <span className="text-slate-400 font-mono">{record.assignedByUsername}</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <div className="flex justify-end pt-4 border-t border-slate-800 mt-6">
        <button
          onClick={onClose}
          className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
        >
          Close
        </button>
      </div>
    </Modal>
  );
};
