import { RoleType } from './auth';

export type EmployeeStatus = 'ACTIVE' | 'RESIGNED' | 'TERMINATED';

export interface EmployeeSummaryResponse {
  id: number;
  userId: number;
  username: string;
  employeeCode: string;
  fullName: string;
  email: string;
  departmentId?: number | null;
  departmentName?: string | null;
  designation?: string | null;
  status: EmployeeStatus;
  dateOfJoining?: string | null;
}

export interface EmployeeResponse {
  id: number;
  userId: number;
  username: string;
  employeeCode: string;
  fullName: string;
  email: string;
  departmentId?: number | null;
  departmentName?: string | null;
  designation?: string | null;
  dateOfJoining?: string | null;
  reportingToId?: number | null;
  reportingToName?: string | null;
  status: EmployeeStatus;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface EmployeeCreateRequest {
  username: string;
  email: string;
  password: string;
  role: RoleType;
  fullName: string;
  departmentId?: number;
  designation?: string;
  dateOfJoining?: string;
  reportingToId?: number;
}

export interface EmployeeUpdateRequest {
  fullName: string;
  departmentId?: number;
  designation?: string;
  dateOfJoining?: string;
  reportingToId?: number;
  version: number;
}

export interface EmployeeStatusUpdateRequest {
  status: EmployeeStatus;
}
