import { axiosInstance } from '../axiosInstance';
import {
  ApiResponse,
  PageResponse,
  DepartmentResponse,
  DepartmentCreateRequest,
  DepartmentUpdateRequest,
} from '../../types';

export interface DepartmentFilterParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  search?: string;
}

export const departmentApi = {
  getAllDepartments: async (params: DepartmentFilterParams = {}): Promise<PageResponse<DepartmentResponse>> => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<DepartmentResponse>>>('/departments', {
      params,
    });
    return res.data.data;
  },

  getAllDepartmentsList: async (): Promise<DepartmentResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<DepartmentResponse[]>>('/departments/all');
    return res.data.data;
  },

  getDepartmentById: async (id: number): Promise<DepartmentResponse> => {
    const res = await axiosInstance.get<ApiResponse<DepartmentResponse>>(`/departments/${id}`);
    return res.data.data;
  },

  createDepartment: async (request: DepartmentCreateRequest): Promise<DepartmentResponse> => {
    const res = await axiosInstance.post<ApiResponse<DepartmentResponse>>('/departments', request);
    return res.data.data;
  },

  updateDepartment: async (id: number, request: DepartmentUpdateRequest): Promise<DepartmentResponse> => {
    const res = await axiosInstance.put<ApiResponse<DepartmentResponse>>(`/departments/${id}`, request);
    return res.data.data;
  },

  deleteDepartment: async (id: number): Promise<void> => {
    await axiosInstance.delete<ApiResponse<void>>(`/departments/${id}`);
  },
};

export default departmentApi;
