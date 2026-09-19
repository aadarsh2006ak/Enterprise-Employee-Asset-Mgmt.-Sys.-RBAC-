import { axiosInstance } from '../axiosInstance';
import { ApiResponse, PageResponse, AuditLogResponse, AuditAction } from '../../types';

export interface AuditFilterParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  entityName?: string;
  entityId?: number;
  action?: AuditAction;
  username?: string;
  userId?: number;
  startDate?: string;
  endDate?: string;
}

export const auditApi = {
  getAllAuditLogs: async (params: AuditFilterParams = {}): Promise<PageResponse<AuditLogResponse>> => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<AuditLogResponse>>>('/audit-logs', {
      params,
    });
    return res.data.data;
  },

  getAuditLogById: async (id: number): Promise<AuditLogResponse> => {
    const res = await axiosInstance.get<ApiResponse<AuditLogResponse>>(`/audit-logs/${id}`);
    return res.data.data;
  },
};

export default auditApi;
