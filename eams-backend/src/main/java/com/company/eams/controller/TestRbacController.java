package com.company.eams.controller;

import com.company.eams.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "RBAC Verification Test Endpoints", description = "Endpoints for verifying Role-Based Access Control and fine-grained permissions")
public class TestRbacController {

    @GetMapping("/public")
    @Operation(summary = "Public endpoint", description = "Accessible without authentication")
    public ResponseEntity<ApiResponse<Map<String, String>>> publicAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("access", "PUBLIC", "message", "Public content accessible by everyone")
        ));
    }

    @GetMapping("/authenticated")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Authenticated user endpoint", description = "Accessible by any authenticated user")
    public ResponseEntity<ApiResponse<Map<String, String>>> authenticatedAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("access", "AUTHENTICATED", "message", "Welcome! You are an authenticated user.")
        ));
    }

    @GetMapping("/admin")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin-only endpoint", description = "Requires ROLE_ADMIN authority")
    public ResponseEntity<ApiResponse<Map<String, String>>> adminOnlyAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("role", "ADMIN", "message", "Access granted to ADMIN resource.")
        ));
    }

    @GetMapping("/manager")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Manager and Admin endpoint", description = "Requires ROLE_ADMIN or ROLE_MANAGER authority")
    public ResponseEntity<ApiResponse<Map<String, String>>> managerAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("role", "ADMIN_OR_MANAGER", "message", "Access granted to Manager/Admin resource.")
        ));
    }

    @GetMapping("/employee")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Employee endpoint", description = "Requires any valid system role")
    public ResponseEntity<ApiResponse<Map<String, String>>> employeeAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("role", "ANY_ROLE", "message", "Access granted to standard Employee resource.")
        ));
    }

    @GetMapping("/permission/user-create")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Operation(summary = "Permission USER_CREATE test", description = "Requires granular USER_CREATE permission")
    public ResponseEntity<ApiResponse<Map<String, String>>> permissionUserCreate() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("permission", "USER_CREATE", "message", "Granted: Authorized to create users.")
        ));
    }

    @GetMapping("/permission/asset-assign")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('ASSET_ASSIGN')")
    @Operation(summary = "Permission ASSET_ASSIGN test", description = "Requires granular ASSET_ASSIGN permission")
    public ResponseEntity<ApiResponse<Map<String, String>>> permissionAssetAssign() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("permission", "ASSET_ASSIGN", "message", "Granted: Authorized to assign assets.")
        ));
    }

    @GetMapping("/permission/asset-request-create")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAuthority('ASSET_REQUEST_CREATE')")
    @Operation(summary = "Permission ASSET_REQUEST_CREATE test", description = "Requires granular ASSET_REQUEST_CREATE permission")
    public ResponseEntity<ApiResponse<Map<String, String>>> permissionAssetRequestCreate() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("permission", "ASSET_REQUEST_CREATE", "message", "Granted: Authorized to create asset requests.")
        ));
    }
}
