export type RoleType = 'ADMIN' | 'MANAGER' | 'EMPLOYEE';

export interface UserSummaryDto {
  id: number;
  username: string;
  email: string;
  role: RoleType;
  permissions: string[];
  isActive: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummaryDto;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface ForgotPasswordVerifyRequest {
  usernameOrEmail: string;
}

export interface ForgotPasswordVerifyResponse {
  resetToken: string;
  username: string;
  maskedEmail: string;
  expiresIn: number;
  message: string;
}

export interface ResetPasswordRequest {
  resetToken: string;
  newPassword: string;
  confirmPassword: string;
}

