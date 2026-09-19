import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../../api';
import { AuditLogResponse, AuditAction } from '../../types';
import { AuditActionBadge } from '../../components/common/Badge';
import { Pagination } from '../../components/common/Pagination';
import { AuditDetailModal } from '../../components/audit/AuditDetailModal';
import { History, Search, Filter, Eye, ShieldCheck } from 'lucide-react';

export const AuditLogViewer: React.FC = () => {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [entityName, setEntityName] = useState('');
  const [action, setAction] = useState<AuditAction | ''>('');
  const [username, setUsername] = useState('');

  const [selectedAuditLog, setSelectedAuditLog] = useState<AuditLogResponse | null>(null);

  const { data: auditData, isLoading } = useQuery({
    queryKey: ['audit-logs', page, size, entityName, action, username],
    queryFn: () =>
      auditApi.getAllAuditLogs({
        page,
        size,
        entityName: entityName || undefined,
        action: action ? (action as AuditAction) : undefined,
        username: username || undefined,
      }),
  });

  const auditLogs = auditData?.content || [];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <History className="w-7 h-7 text-indigo-400" />
            <span>Immutable Security Audit Trail</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Complete cryptographic change logging with JSONB pre- and post-execution state diffs
          </p>
        </div>

        <div className="flex items-center gap-2 text-xs font-semibold px-3 py-1.5 rounded-xl bg-indigo-950/40 text-indigo-300 border border-indigo-500/20">
          <ShieldCheck className="w-4 h-4 text-indigo-400" />
          <span>Non-Repudiation Guaranteed</span>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-panel p-4 rounded-2xl border border-slate-800 flex flex-col md:flex-row gap-3 items-center justify-between">
        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          <div className="relative w-full sm:w-56">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={entityName}
              onChange={(e) => {
                setEntityName(e.target.value);
                setPage(0);
              }}
              placeholder="Entity (e.g. Asset, Employee)..."
              className="w-full pl-10 pr-3 py-2 rounded-xl text-xs glass-input"
            />
          </div>

          <div className="relative w-full sm:w-48">
            <input
              type="text"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value);
                setPage(0);
              }}
              placeholder="Operator username..."
              className="w-full px-3 py-2 rounded-xl text-xs glass-input"
            />
          </div>
        </div>

        <div className="flex items-center gap-2.5 w-full md:w-auto">
          <div className="flex items-center gap-1 text-xs text-slate-400">
            <Filter className="w-3.5 h-3.5" />
            <span>Action:</span>
          </div>

          <select
            value={action}
            onChange={(e) => {
              setAction(e.target.value as AuditAction | '');
              setPage(0);
            }}
            className="px-3 py-2 rounded-xl text-xs glass-input bg-slate-900"
          >
            <option value="">All Actions</option>
            <option value="CREATE">CREATE</option>
            <option value="UPDATE">UPDATE</option>
            <option value="DELETE">DELETE</option>
            <option value="ASSIGN">ASSIGN</option>
            <option value="RETURN">RETURN</option>
            <option value="STATUS_CHANGE">STATUS_CHANGE</option>
            <option value="LOGIN">LOGIN</option>
            <option value="LOGOUT">LOGOUT</option>
          </select>
        </div>
      </div>

      {/* Audit Logs Table */}
      <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
              <tr>
                <th className="py-3.5 px-4">Log ID</th>
                <th className="py-3.5 px-4">Action</th>
                <th className="py-3.5 px-4">Entity Target</th>
                <th className="py-3.5 px-4">Operator</th>
                <th className="py-3.5 px-4">IP Address</th>
                <th className="py-3.5 px-4">Timestamp</th>
                <th className="py-3.5 px-4 text-right">JSON Diff</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center gap-3">
                      <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
                      <span>Loading audit logs...</span>
                    </div>
                  </td>
                </tr>
              ) : auditLogs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    No audit records matching criteria.
                  </td>
                </tr>
              ) : (
                auditLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-850/50 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-bold text-slate-400">
                      #{log.id}
                    </td>
                    <td className="py-3.5 px-4">
                      <AuditActionBadge action={log.action} />
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="font-semibold text-white">{log.entityName}</span>{' '}
                      {log.entityId && (
                        <span className="text-[11px] font-mono text-indigo-400">
                          (ID: {log.entityId})
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-slate-200 font-mono">
                      {log.usernameSnapshot || (log.userId ? `User #${log.userId}` : 'System')}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400 font-mono">
                      {log.ipAddress || 'Internal'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {new Date(log.createdAt).toLocaleString()}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <button
                        onClick={() => setSelectedAuditLog(log)}
                        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-300 text-xs font-medium transition-colors"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>Inspect Diff</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {auditData && (
          <Pagination
            pageNumber={auditData.pageNumber}
            totalPages={auditData.totalPages}
            totalElements={auditData.totalElements}
            pageSize={size}
            onPageChange={(newPage) => setPage(newPage)}
            onPageSizeChange={(newSize) => {
              setSize(newSize);
              setPage(0);
            }}
          />
        )}
      </div>

      {/* Detail Modal */}
      <AuditDetailModal
        isOpen={!!selectedAuditLog}
        onClose={() => setSelectedAuditLog(null)}
        auditLog={selectedAuditLog}
      />
    </div>
  );
};

export default AuditLogViewer;
