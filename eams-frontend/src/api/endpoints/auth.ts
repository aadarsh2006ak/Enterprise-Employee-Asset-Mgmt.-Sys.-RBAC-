import { axiosInstance } from '../axiosInstance';
import { ApiResponse, AuthResponse, LoginRequest, UserSummaryDto, ForgotPasswordVerifyRequest, ForgotPasswordVerifyResponse, ResetPasswordRequest } from '../../types';

export const authApi = {
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const res = await axiosInstance.post<ApiResponse<AuthResponse>>('/auth/login', request);
    return res.data.data;
  },

  refreshToken: async (refreshToken?: string): Promise<AuthResponse> => {
    const res = await axiosInstance.post<ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken });
    return res.data.data;
  },

  logout: async (refreshToken?: string): Promise<void> => {
    await axiosInstance.post<ApiResponse<void>>('/auth/logout', { refreshToken });
  },

  getCurrentUser: async (): Promise<UserSummaryDto> => {
    const res = await axiosInstance.get<ApiResponse<UserSummaryDto>>('/auth/me');
    return res.data.data;
  },

  verifyForgotPassword: async (request: ForgotPasswordVerifyRequest): Promise<ForgotPasswordVerifyResponse> => {
    const res = await axiosInstance.post<ApiResponse<ForgotPasswordVerifyResponse>>('/auth/forgot-password/verify', request);
    return res.data.data;
  },

  resetPassword: async (request: ResetPasswordRequest): Promise<string> => {
    const res = await axiosInstance.post<ApiResponse<void>>('/auth/forgot-password/reset', request);
    return res.data.message || 'Password reset successfully';
  },
};


export default authApi;
