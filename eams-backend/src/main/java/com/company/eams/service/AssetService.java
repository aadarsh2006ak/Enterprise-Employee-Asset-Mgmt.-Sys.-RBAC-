package com.company.eams.service;

import com.company.eams.dto.request.*;
import com.company.eams.dto.response.*;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssetService {

    // Category Management
    AssetCategoryResponse createCategory(AssetCategoryRequest request);

    List<AssetCategoryResponse> getAllCategories();

    // Asset CRUD
    AssetResponse createAsset(AssetCreateRequest request);

    PageResponse<AssetResponse> getAllAssets(Pageable pageable, Long categoryId, AssetStatus status, String search);

    AssetResponse getAssetById(Long id);

    AssetResponse updateAsset(Long id, AssetUpdateRequest request);

    void deleteAsset(Long id);

    // Concurrency Assignment & Return Workflows (Pessimistic Locking)
    AssetAssignmentResponse assignAsset(Long assetId, AssetAssignRequest request, UserPrincipal assignedBy);

    AssetAssignmentResponse returnAsset(Long assetId, AssetReturnRequest request);

    List<AssetAssignmentResponse> getAssetAssignmentHistory(Long assetId);

    List<AssetAssignmentResponse> getMyActiveAssets(UserPrincipal userPrincipal);
}
