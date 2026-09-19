export interface DepartmentResponse {
  id: number;
  name: string;
  managerId?: number | null;
  managerName?: string | null;
  employeeCount?: number;
}

export interface DepartmentCreateRequest {
  name: string;
  managerId?: number;
}

export interface DepartmentUpdateRequest {
  name: string;
  managerId?: number;
}
