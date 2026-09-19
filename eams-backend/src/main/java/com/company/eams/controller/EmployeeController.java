package com.company.eams.controller;

import com.company.eams.dto.request.EmployeeCreateRequest;
import com.company.eams.dto.request.EmployeeStatusUpdateRequest;
import com.company.eams.dto.request.EmployeeUpdateRequest;
import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.EmployeeResponse;
import com.company.eams.dto.response.EmployeeSummaryResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.EmployeeService;
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

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Employee Management", description = "Endpoints for managing employee profiles, hierarchy, department assignments, and status lifecycles")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @Operation(summary = "Create employee profile", description = "Provisions a new system user and creates the linked employee profile (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get paginated employees", description = "Search and filter employees by department, status, or keyword with pagination")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeSummaryResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<EmployeeSummaryResponse> response = employeeService.getAllEmployees(pageable, departmentId, status, search);
        return ResponseEntity.ok(ApiResponse.success(response, "Employees retrieved successfully"));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Get current logged-in employee profile", description = "Resolves employee profile for the authenticated principal")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyEmployeeProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        EmployeeResponse response = employeeService.getEmployeeByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Employee profile retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_READ')")
    @Operation(summary = "Get employee by ID", description = "Retrieve full employee profile including manager and department details")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee details retrieved"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE_UPDATE')")
    @Operation(summary = "Update employee details", description = "Update employee profile fields such as name, department, designation, and reporting manager")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EMPLOYEE_STATUS_CHANGE')")
    @Operation(summary = "Change employee status", description = "Update status (ACTIVE, RESIGNED, TERMINATED) with real-time linked user account lockouts (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployeeStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Employee status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Permanently removes an employee and linked user account (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.message("Employee deleted successfully"));
    }
}
