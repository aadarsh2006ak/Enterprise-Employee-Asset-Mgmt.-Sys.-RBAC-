import { axiosInstance } from '../axiosInstance';
import { ApiResponse, PageResponse, RoleType } from '../../types';

export interface AdminUserResponse {
  id: number;
  username: string;
  email: string;
  role: RoleType;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface UserCreateAdminRequest {
  username: string;
  email: string;
  password: string;
  role: RoleType;
}

export interface UserRoleUpdateRequest {
  role: RoleType;
}

export const usersApi = {
  getAllUsers: async (params: { page?: number; size?: number; search?: string } = {}): Promise<PageResponse<AdminUserResponse>> => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<AdminUserResponse>>>('/admin/users', { params });
    return res.data.data;
  },

  createUser: async (request: UserCreateAdminRequest): Promise<AdminUserResponse> => {
    const res = await axiosInstance.post<ApiResponse<AdminUserResponse>>('/admin/users', request);
    return res.data.data;
  },

  updateUserRole: async (id: number, request: UserRoleUpdateRequest): Promise<AdminUserResponse> => {
    const res = await axiosInstance.put<ApiResponse<AdminUserResponse>>(`/admin/users/${id}/role`, request);
    return res.data.data;
  },

  deactivateUser: async (id: number): Promise<void> => {
    await axiosInstance.delete<ApiResponse<void>>(`/admin/users/${id}`);
  },
};
