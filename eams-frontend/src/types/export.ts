export type ExportFormat = 'CSV' | 'XLSX';

export type ExportStatus = 'SUBMITTED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface ExportJobRequest {
  dataset: 'EMPLOYEES' | 'ASSETS' | 'DEPARTMENTS' | 'AUDIT_LOGS' | 'ASSIGNMENTS';
  format: ExportFormat;
  filters?: Record<string, any>;
}

export interface ExportJobResponse {
  id: number;
  jobUuid: string;
  dataset: string;
  format: ExportFormat;
  status: ExportStatus;
  progressPercent: number;
  totalRecords?: number | null;
  processedRecords?: number | null;
  fileName?: string | null;
  errorMessage?: string | null;
  createdByName?: string | null;
  createdAt: string;
  completedAt?: string | null;
}
