package com.company.eams.controller;

import com.company.eams.dto.request.*;
import com.company.eams.dto.response.*;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Asset & Inventory Management", description = "Endpoints for asset cataloging, pessimistic locking assignment, return workflows, and audit history")
public class AssetController {

    private final AssetService assetService;

    // =========================================================================
    // Category Management
    // =========================================================================

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('ASSET_CREATE')")
    @Operation(summary = "Create asset category", description = "Registers a new equipment category (Admin only)")
    public ResponseEntity<ApiResponse<AssetCategoryResponse>> createCategory(
            @Valid @RequestBody AssetCategoryRequest request) {
        AssetCategoryResponse response = assetService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Asset category created successfully"));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('ASSET_READ')")
    @Operation(summary = "Get all categories", description = "Retrieve all asset categories with associated asset counts")
    public ResponseEntity<ApiResponse<List<AssetCategoryResponse>>> getAllCategories() {
        List<AssetCategoryResponse> categories = assetService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }

    // =========================================================================
    // Asset CRUD
    // =========================================================================

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET_CREATE')")
    @Operation(summary = "Register new asset", description = "Adds a new hardware/software asset to inventory (Admin only)")
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(
            @Valid @RequestBody AssetCreateRequest request) {
        AssetResponse response = assetService.createAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Asset registered successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_READ')")
    @Operation(summary = "Get paginated assets", description = "Filter assets by category, status, or search query with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "assetTag") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<AssetResponse> response = assetService.getAllAssets(pageable, categoryId, status, search);
        return ResponseEntity.ok(ApiResponse.success(response, "Assets retrieved successfully"));
    }

    @GetMapping("/my-assets")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get active assets for current employee", description = "Returns all active hardware/software assets currently assigned to the logged-in user")
    public ResponseEntity<ApiResponse<List<AssetAssignmentResponse>>> getMyActiveAssets(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<AssetAssignmentResponse> response = assetService.getMyActiveAssets(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response, "Active assigned assets retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_READ')")
    @Operation(summary = "Get asset by ID", description = "Retrieve asset details with active assignment metadata")
    public ResponseEntity<ApiResponse<AssetResponse>> getAssetById(@PathVariable Long id) {
        AssetResponse response = assetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Asset details retrieved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Update asset metadata", description = "Updates asset information with optimistic locking protection (Admin only)")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest request) {
        AssetResponse response = assetService.updateAsset(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Asset updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_DELETE')")
    @Operation(summary = "Delete asset", description = "Removes an asset from inventory if not actively assigned (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.message("Asset deleted successfully"));
    }

    // =========================================================================
    // Concurrency Workflows: Assign & Return
    // =========================================================================

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ASSET_ASSIGN')")
    @Operation(summary = "Assign asset to employee", description = "Locks asset row (PESSIMISTIC_WRITE) and assigns it to an employee (Admin/Manager only)")
    public ResponseEntity<ApiResponse<AssetAssignmentResponse>> assignAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetAssignRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        AssetAssignmentResponse response = assetService.assignAsset(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Asset assigned successfully"));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ASSET_RETURN')")
    @Operation(summary = "Accept returned asset", description = "Locks asset row (PESSIMISTIC_WRITE), concludes active assignment, and updates asset status (Admin/Manager only)")
    public ResponseEntity<ApiResponse<AssetAssignmentResponse>> returnAsset(
            @PathVariable Long id,
            @RequestBody(required = false) AssetReturnRequest request) {
        AssetAssignmentResponse response = assetService.returnAsset(id, request != null ? request : new AssetReturnRequest());
        return ResponseEntity.ok(ApiResponse.success(response, "Asset returned successfully"));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('ASSET_READ')")
    @Operation(summary = "Get asset assignment history", description = "Retrieve full assignment history timeline for an asset")
    public ResponseEntity<ApiResponse<List<AssetAssignmentResponse>>> getAssetAssignmentHistory(@PathVariable Long id) {
        List<AssetAssignmentResponse> history = assetService.getAssetAssignmentHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history, "Asset assignment history retrieved"));
    }
}
