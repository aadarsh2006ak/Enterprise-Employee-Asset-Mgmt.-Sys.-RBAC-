import { axiosInstance } from '../axiosInstance';
import {
  ApiResponse,
  PageResponse,
  AssetResponse,
  AssetCategoryResponse,
  AssetCategoryRequest,
  AssetCreateRequest,
  AssetUpdateRequest,
  AssetAssignRequest,
  AssetReturnRequest,
  AssetAssignmentResponse,
  AssetStatus,
} from '../../types';

export interface AssetFilterParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  categoryId?: number;
  status?: AssetStatus;
  search?: string;
}

export const assetApi = {
  // Categories
  getCategories: async (): Promise<AssetCategoryResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<AssetCategoryResponse[]>>('/assets/categories');
    return res.data.data;
  },

  createCategory: async (request: AssetCategoryRequest): Promise<AssetCategoryResponse> => {
    const res = await axiosInstance.post<ApiResponse<AssetCategoryResponse>>('/assets/categories', request);
    return res.data.data;
  },

  // Assets
  getAllAssets: async (params: AssetFilterParams = {}): Promise<PageResponse<AssetResponse>> => {
    const res = await axiosInstance.get<ApiResponse<PageResponse<AssetResponse>>>('/assets', {
      params,
    });
    return res.data.data;
  },

  getAssetById: async (id: number): Promise<AssetResponse> => {
    const res = await axiosInstance.get<ApiResponse<AssetResponse>>(`/assets/${id}`);
    return res.data.data;
  },

  getMyActiveAssets: async (): Promise<AssetAssignmentResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<AssetAssignmentResponse[]>>('/assets/my-assets');
    return res.data.data;
  },

  createAsset: async (request: AssetCreateRequest): Promise<AssetResponse> => {
    const res = await axiosInstance.post<ApiResponse<AssetResponse>>('/assets', request);
    return res.data.data;
  },

  updateAsset: async (id: number, request: AssetUpdateRequest): Promise<AssetResponse> => {
    const res = await axiosInstance.put<ApiResponse<AssetResponse>>(`/assets/${id}`, request);
    return res.data.data;
  },

  deleteAsset: async (id: number): Promise<void> => {
    await axiosInstance.delete<ApiResponse<void>>(`/assets/${id}`);
  },

  // Concurrency Actions (Pessimistic locking)
  assignAsset: async (id: number, request: AssetAssignRequest): Promise<AssetAssignmentResponse> => {
    const res = await axiosInstance.post<ApiResponse<AssetAssignmentResponse>>(`/assets/${id}/assign`, request);
    return res.data.data;
  },

  returnAsset: async (id: number, request?: AssetReturnRequest): Promise<AssetAssignmentResponse> => {
    const res = await axiosInstance.post<ApiResponse<AssetAssignmentResponse>>(`/assets/${id}/return`, request || {});
    return res.data.data;
  },

  getAssetHistory: async (id: number): Promise<AssetAssignmentResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<AssetAssignmentResponse[]>>(`/assets/${id}/history`);
    return res.data.data;
  },
};

export default assetApi;
