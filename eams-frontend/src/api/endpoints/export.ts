import { axiosInstance } from '../axiosInstance';
import {
  ApiResponse,
  PageResponse,
  ExportJobResponse,
  ExportJobRequest,
  ExportFormat,
} from '../../types';

export interface ExportJobFilterParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export const exportApi = {
  // Direct Streaming Downloads
  streamEmployees: async (format: ExportFormat = 'CSV', params: Record<string, any> = {}): Promise<void> => {
    const response = await axiosInstance.get('/exports/employees', {
      params: { format, ...params },
      responseType: 'blob',
    });
    downloadBlob(response.data, `employees_${Date.now()}.${format.toLowerCase()}`);
  },

  streamDepartments: async (format: ExportFormat = 'CSV'): Promise<void> => {
    const response = await axiosInstance.get('/exports/departments', {
      params: { format },
      responseType: 'blob',
    });
    downloadBlob(response.data, `departments_${Date.now()}.${format.toLowerCase()}`);
  },

  streamAssets: async (format: ExportFormat = 'CSV', params: Record<string, any> = {}): Promise<void> => {
    const response = await axiosInstance.get('/exports/assets', {
      params: { format, ...params },
      responseType: 'blob',
    });
    downloadBlob(response.data, `assets_${Date.now()}.${format.toLowerCase()}`);
  },

  streamAuditLogs: async (format: ExportFormat = 'CSV', params: Record<string, any> = {}): Promise<void> => {
    const response = await axiosInstance.get('/exports/audit-logs', {
      params: { format, ...params },
      responseType: 'blob',
    });
    downloadBlob(response.data, `audit_logs_${Date.now()}.${format.toLowerCase()}`);
  },

  streamAssignments: async (format: ExportFormat = 'CSV', assetId?: number): Promise<void> => {
    const response = await axiosInstance.get('/exports/assignments', {
      params: { format, ...(assetId ? { assetId } : {}) },
      responseType: 'blob',
    });
    downloadBlob(response.data, `assignments_${Date.now()}.${format.toLowerCase()}`);
  },

  // Async Background Jobs
  createExportJob: async (request: ExportJobRequest): Promise<ExportJobResponse> => {
    const res = await axiosInstance.post<ApiResponse<ExportJobResponse>>('/exports/jobs', request);
    return res.data.data;
  },

  getUserExportJobs: async (params: ExportJobFilterParams = {}): Promise<PageResponse<ExportJobResponse>> => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<ExportJobResponse>>>('/exports/jobs', {
      params,
    });
    return res.data.data;
  },

  getExportJobStatus: async (jobUuid: string): Promise<ExportJobResponse> => {
    const res = await axiosInstance.get<ApiResponse<ExportJobResponse>>(`/exports/jobs/${jobUuid}`);
    return res.data.data;
  },

  downloadJobFile: async (jobUuid: string, fileName?: string): Promise<void> => {
    const response = await axiosInstance.get(`/exports/jobs/${jobUuid}/download`, {
      responseType: 'blob',
    });
    downloadBlob(response.data, fileName || `export_${jobUuid}.file`);
  },
};

function downloadBlob(blob: Blob, fileName: string) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  link.parentNode?.removeChild(link);
  window.URL.revokeObjectURL(url);
}

export default exportApi;
