export type AssetStatus = 'AVAILABLE' | 'ASSIGNED' | 'UNDER_MAINTENANCE' | 'RETIRED';

export interface AssetCategory {
  id: number;
  name: string;
  assetCount?: number;
}

export interface AssetCategoryResponse {
  id: number;
  name: string;
  assetCount: number;
}

export interface AssetCategoryRequest {
  name: string;
}

export interface AssetAssignmentResponse {
  id: number;
  assetId: number;
  assetTag: string;
  assetModel: string;
  categoryName?: string;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  departmentName?: string;
  assignedByUsername: string;
  assignedAt: string;
  returnedAt?: string | null;
  conditionNotes?: string | null;
}

export interface AssetResponse {
  id: number;
  assetTag: string;
  categoryId: number;
  categoryName: string;
  modelName: string;
  serialNumber: string;
  purchaseDate?: string | null;
  status: AssetStatus;
  version: number;
  activeAssignment?: AssetAssignmentResponse | null;
  createdAt: string;
  updatedAt: string;
}

export interface AssetCreateRequest {
  assetTag: string;
  categoryId: number;
  modelName: string;
  serialNumber?: string;
  purchaseDate?: string;
}

export interface AssetUpdateRequest {
  categoryId: number;
  modelName: string;
  serialNumber?: string;
  purchaseDate?: string;
  status: AssetStatus;
  version: number;
}

export interface AssetAssignRequest {
  employeeId: number;
  conditionNotes?: string;
}

export interface AssetReturnRequest {
  conditionNotes?: string;
}
