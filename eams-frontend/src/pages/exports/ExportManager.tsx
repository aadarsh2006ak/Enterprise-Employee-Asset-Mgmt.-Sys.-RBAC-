import React, { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { exportApi } from '../../api';
import { ExportFormat } from '../../types';
import { ExportStatusBadge } from '../../components/common/Badge';
import { Pagination } from '../../components/common/Pagination';
import { AsyncExportJobModal } from '../../components/exports/AsyncExportJobModal';
import { useToast } from '../../components/common/Toast';
import {
  FileSpreadsheet,
  Download,
  Plus,
  Boxes,
  Users,
  Building2,
  History,
  Clock,
  Sparkles,
  CheckCircle2,
} from 'lucide-react';

export const ExportManager: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, error: showError } = useToast();

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [isJobModalOpen, setIsJobModalOpen] = useState(false);
  const [streamingLoading, setStreamingLoading] = useState<string | null>(null);

  // Poll background export jobs every 3 seconds
  const { data: jobsData, isLoading } = useQuery({
    queryKey: ['export-jobs', page, size],
    queryFn: () => exportApi.getUserExportJobs({ page, size }),
    refetchInterval: 3000,
  });

  const handleStreamDownload = async (
    name: string,
    streamFn: (format: ExportFormat) => Promise<void>,
    format: ExportFormat
  ) => {
    setStreamingLoading(`${name}_${format}`);
    try {
      await streamFn(format);
      success(`${name} (${format}) downloaded successfully`);
    } catch (err: any) {
      showError(err.response?.data?.message || `Failed to stream ${name} export`);
    } finally {
      setStreamingLoading(null);
    }
  };

  const handleDownloadJob = async (jobUuid: string, fileName?: string | null) => {
    try {
      await exportApi.downloadJobFile(jobUuid, fileName || undefined);
      success('Export report downloaded');
    } catch (err: any) {
      showError(err.response?.data?.message || 'Failed to download report file');
    }
  };

  const streamCards = [
    {
      title: 'Employees Directory',
      desc: 'All staff profiles, email, designations & dates of joining',
      icon: Users,
      color: 'indigo',
      onCsv: () => handleStreamDownload('Employees', (f) => exportApi.streamEmployees(f), 'CSV'),
      onXlsx: () => handleStreamDownload('Employees', (f) => exportApi.streamEmployees(f), 'XLSX'),
      key: 'Employees',
    },
    {
      title: 'Asset Inventory',
      desc: 'Hardware items, categories, serials, and current assignment status',
      icon: Boxes,
      color: 'emerald',
      onCsv: () => handleStreamDownload('Assets', (f) => exportApi.streamAssets(f), 'CSV'),
      onXlsx: () => handleStreamDownload('Assets', (f) => exportApi.streamAssets(f), 'XLSX'),
      key: 'Assets',
    },
    {
      title: 'Departments & Headcount',
      desc: 'Organizational units, department managers, and member counts',
      icon: Building2,
      color: 'blue',
      onCsv: () => handleStreamDownload('Departments', (f) => exportApi.streamDepartments(f), 'CSV'),
      onXlsx: () => handleStreamDownload('Departments', (f) => exportApi.streamDepartments(f), 'XLSX'),
      key: 'Departments',
    },
    {
      title: 'Assignment History Timeline',
      desc: 'Complete log of past and current employee equipment allocations',
      icon: Clock,
      color: 'purple',
      onCsv: () => handleStreamDownload('Assignments', (f) => exportApi.streamAssignments(f), 'CSV'),
      onXlsx: () => handleStreamDownload('Assignments', (f) => exportApi.streamAssignments(f), 'XLSX'),
      key: 'Assignments',
    },
    {
      title: 'Security Audit Logs',
      desc: 'Full immutable system log with change snapshot JSON diffs',
      icon: History,
      color: 'amber',
      onCsv: () => handleStreamDownload('AuditLogs', (f) => exportApi.streamAuditLogs(f), 'CSV'),
      onXlsx: () => handleStreamDownload('AuditLogs', (f) => exportApi.streamAuditLogs(f), 'XLSX'),
      key: 'AuditLogs',
    },
  ];

  const jobs = jobsData?.content || [];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <FileSpreadsheet className="w-7 h-7 text-indigo-400" />
            <span>Data Export & Background Reports</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Low-memory direct streaming CSV/Excel reports and background asynchronous job processing
          </p>
        </div>

        <button
          onClick={() => setIsJobModalOpen(true)}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-500/25 transition-all self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Dispatch Background Job</span>
        </button>
      </div>

      {/* Section 1: Instant Streaming Exports */}
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-indigo-400" />
          <h3 className="text-sm font-bold text-white uppercase tracking-wider">
            1. Direct Streaming Instant Downloads
          </h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {streamCards.map((card) => {
            const Icon = card.icon;
            const isCsvLoading = streamingLoading === `${card.key}_CSV`;
            const isXlsxLoading = streamingLoading === `${card.key}_XLSX`;

            return (
              <div
                key={card.key}
                className="glass-panel glass-panel-hover rounded-2xl p-5 border border-slate-800 flex flex-col justify-between space-y-4"
              >
                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-850 border border-slate-700/60 text-indigo-400">
                    <Icon className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-bold text-white">{card.title}</h4>
                    <p className="text-xs text-slate-400 mt-0.5 leading-relaxed">{card.desc}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-2 border-t border-slate-800/80">
                  <button
                    onClick={card.onCsv}
                    disabled={!!streamingLoading}
                    className="flex-1 flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-750 text-slate-200 text-xs font-medium border border-slate-700/60 disabled:opacity-50 transition-colors"
                  >
                    <Download className="w-3.5 h-3.5 text-indigo-400" />
                    <span>{isCsvLoading ? 'Streaming...' : 'CSV'}</span>
                  </button>

                  <button
                    onClick={card.onXlsx}
                    disabled={!!streamingLoading}
                    className="flex-1 flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-950/40 hover:bg-emerald-900/40 text-emerald-300 text-xs font-medium border border-emerald-500/30 disabled:opacity-50 transition-colors"
                  >
                    <Download className="w-3.5 h-3.5 text-emerald-400" />
                    <span>{isXlsxLoading ? 'Streaming...' : 'Excel (XLSX)'}</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Section 2: Async Background Jobs Queue */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-indigo-400" />
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">
              2. Async Background Job Queue (Live Polling)
            </h3>
          </div>
          <span className="text-[11px] text-slate-400 flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Auto-refreshing every 3s
          </span>
        </div>

        <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900/90 border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
                <tr>
                  <th className="py-3.5 px-4">Job Token</th>
                  <th className="py-3.5 px-4">Dataset & Format</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4">Progress</th>
                  <th className="py-3.5 px-4">Created At</th>
                  <th className="py-3.5 px-4 text-right">Download</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {isLoading ? (
                  <tr>
                    <td colSpan={6} className="py-12 text-center text-slate-400">
                      Loading export jobs...
                    </td>
                  </tr>
                ) : jobs.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-12 text-center text-slate-400">
                      No asynchronous export jobs submitted yet. Click "Dispatch Background Job" above.
                    </td>
                  </tr>
                ) : (
                  jobs.map((job) => (
                    <tr key={job.jobUuid} className="hover:bg-slate-850/50 transition-colors">
                      <td className="py-3.5 px-4 font-mono text-indigo-300 text-[11px]">
                        {job.jobUuid.substring(0, 13)}...
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="font-semibold text-white">{job.dataset}</span>{' '}
                        <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-400">
                          {job.format}
                        </span>
                      </td>
                      <td className="py-3.5 px-4">
                        <ExportStatusBadge status={job.status} />
                      </td>
                      <td className="py-3.5 px-4 min-w-[140px]">
                        <div className="space-y-1">
                          <div className="flex justify-between text-[10px] text-slate-400">
                            <span>{job.progressPercent}%</span>
                            {job.totalRecords && (
                              <span>
                                {job.processedRecords || 0} / {job.totalRecords}
                              </span>
                            )}
                          </div>
                          <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-indigo-500 rounded-full transition-all duration-500"
                              style={{ width: `${job.progressPercent}%` }}
                            />
                          </div>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-slate-400">
                        {new Date(job.createdAt).toLocaleTimeString()}
                      </td>
                      <td className="py-3.5 px-4 text-right">
                        {job.status === 'COMPLETED' ? (
                          <button
                            onClick={() => handleDownloadJob(job.jobUuid, job.fileName)}
                            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-semibold transition-colors"
                          >
                            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                            <span>Download</span>
                          </button>
                        ) : job.status === 'FAILED' ? (
                          <span className="text-[11px] text-rose-400">
                            {job.errorMessage || 'Error'}
                          </span>
                        ) : (
                          <span className="text-[11px] text-slate-500 italic">Processing...</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {jobsData && (
            <Pagination
              pageNumber={jobsData.pageNumber}
              totalPages={jobsData.totalPages}
              totalElements={jobsData.totalElements}
              pageSize={size}
              onPageChange={(newPage) => setPage(newPage)}
              onPageSizeChange={(newSize) => {
                setSize(newSize);
                setPage(0);
              }}
            />
          )}
        </div>
      </div>

      {/* Background Job Dispatch Modal */}
      <AsyncExportJobModal
        isOpen={isJobModalOpen}
        onClose={() => setIsJobModalOpen(false)}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['export-jobs'] });
        }}
      />
    </div>
  );
};

export default ExportManager;
