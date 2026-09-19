import React from 'react';
import { Modal } from '../common/Modal';
import { AuditLogResponse } from '../../types';
import { AuditActionBadge } from '../common/Badge';
import { ShieldAlert, Terminal } from 'lucide-react';

interface AuditDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  auditLog: AuditLogResponse | null;
}

export const AuditDetailModal: React.FC<AuditDetailModalProps> = ({
  isOpen,
  onClose,
  auditLog,
}) => {
  if (!auditLog) return null;

  const renderJson = (val: any) => {
    if (!val) return <span className="text-slate-500 italic">null / none</span>;
    try {
      const obj = typeof val === 'string' ? JSON.parse(val) : val;
      return JSON.stringify(obj, null, 2);
    } catch {
      return String(val);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Audit Snapshot Entry #${auditLog.id}`}
      subtitle={`${auditLog.entityName} • ID: ${auditLog.entityId ?? 'N/A'}`}
      maxWidth="3xl"
    >
      <div className="space-y-4">
        {/* Meta Header */}
        <div className="p-4 rounded-xl bg-slate-850 border border-slate-700/60 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
          <div>
            <span className="text-slate-400 block mb-0.5">Action Executed</span>
            <AuditActionBadge action={auditLog.action} />
          </div>
          <div>
            <span className="text-slate-400 block mb-0.5">Operator</span>
            <span className="font-mono text-indigo-300 font-semibold">
              {auditLog.usernameSnapshot || (auditLog.userId ? `User #${auditLog.userId}` : 'System')}
            </span>
          </div>
          <div>
            <span className="text-slate-400 block mb-0.5">IP Address</span>
            <span className="font-mono text-slate-300">{auditLog.ipAddress || 'Internal'}</span>
          </div>
          <div>
            <span className="text-slate-400 block mb-0.5">Timestamp</span>
            <span className="text-slate-300">{new Date(auditLog.createdAt).toLocaleString()}</span>
          </div>
        </div>

        {/* JSON Diff Side by Side */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-rose-400 uppercase tracking-wider">
              <ShieldAlert className="w-3.5 h-3.5" />
              <span>Previous State (old_value)</span>
            </div>
            <pre className="p-3.5 rounded-xl bg-slate-950 border border-rose-900/30 text-rose-300/90 font-mono text-[11px] h-64 overflow-auto scrollbar-thin">
              {renderJson(auditLog.oldValue)}
            </pre>
          </div>

          <div className="space-y-1.5">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400 uppercase tracking-wider">
              <Terminal className="w-3.5 h-3.5" />
              <span>New State (new_value)</span>
            </div>
            <pre className="p-3.5 rounded-xl bg-slate-950 border border-emerald-900/30 text-emerald-300/90 font-mono text-[11px] h-64 overflow-auto scrollbar-thin">
              {renderJson(auditLog.newValue)}
            </pre>
          </div>
        </div>

        <div className="flex justify-end pt-2 border-t border-slate-800">
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </Modal>
  );
};
