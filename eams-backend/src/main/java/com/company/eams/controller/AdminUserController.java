package com.company.eams.controller;

import com.company.eams.dto.request.UserCreateAdminRequest;
import com.company.eams.dto.request.UserRoleUpdateRequest;
import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.dto.response.UserResponseDto;
import com.company.eams.service.UserService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User & Access Control (Admin)", description = "Endpoints for administering system users, roles, and deactivations (Section 8.2)")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get paginated user accounts", description = "List all user accounts in the system (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<UserResponseDto> response = userService.getAllUsers(pageable, search);
        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Create user account", description = "Provisions a new system user with assigned role (Admin only)")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody UserCreateAdminRequest request) {

        UserResponseDto response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User account created successfully"));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Change user's RBAC role and revoke active sessions (Admin only)")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request) {

        UserResponseDto response = userService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate user account", description = "Soft delete/deactivate user account and revoke sessions (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.message("User deactivated successfully"));
    }
}
