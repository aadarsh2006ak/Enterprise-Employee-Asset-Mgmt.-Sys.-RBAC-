import { axiosInstance } from '../axiosInstance';
import {
  ApiResponse,
  PageResponse,
  EmployeeResponse,
  EmployeeSummaryResponse,
  EmployeeCreateRequest,
  EmployeeUpdateRequest,
  EmployeeStatusUpdateRequest,
  EmployeeStatus,
} from '../../types';

export interface EmployeeFilterParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  departmentId?: number;
  status?: EmployeeStatus;
  search?: string;
}

export const employeeApi = {
  getAllEmployees: async (params: EmployeeFilterParams = {}): Promise<PageResponse<EmployeeSummaryResponse>> => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<EmployeeSummaryResponse>>>('/employees', {
      params,
    });
    return res.data.data;
  },

  getMyProfile: async (): Promise<EmployeeResponse> => {
    const res = await axiosInstance.get<ApiResponse<EmployeeResponse>>('/employees/me');
    return res.data.data;
  },

  getEmployeeById: async (id: number): Promise<EmployeeResponse> => {
    const res = await axiosInstance.get<ApiResponse<EmployeeResponse>>(`/employees/${id}`);
    return res.data.data;
  },

  createEmployee: async (request: EmployeeCreateRequest): Promise<EmployeeResponse> => {
    const res = await axiosInstance.post<ApiResponse<EmployeeResponse>>('/employees', request);
    return res.data.data;
  },

  updateEmployee: async (id: number, request: EmployeeUpdateRequest): Promise<EmployeeResponse> => {
    const res = await axiosInstance.put<ApiResponse<EmployeeResponse>>(`/employees/${id}`, request);
    return res.data.data;
  },

  updateStatus: async (id: number, request: EmployeeStatusUpdateRequest): Promise<EmployeeResponse> => {
    const res = await axiosInstance.patch<ApiResponse<EmployeeResponse>>(`/employees/${id}/status`, request);
    return res.data.data;
  },

  deleteEmployee: async (id: number): Promise<void> => {
    await axiosInstance.delete<ApiResponse<void>>(`/employees/${id}`);
  },
};

export default employeeApi;
