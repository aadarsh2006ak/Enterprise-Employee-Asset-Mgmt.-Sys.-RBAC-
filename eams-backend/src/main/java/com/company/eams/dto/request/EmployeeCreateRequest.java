package com.company.eams.dto.request;

import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee profile and linked user account creation payload")
public class EmployeeCreateRequest {

    // --- Linked User Credentials ---
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    @Schema(description = "Unique system username", example = "sarah.connor")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    @Schema(description = "Corporate email address", example = "sarah.connor@company.com")
    private String email;

    @Schema(description = "Account password (if left empty, defaults to 'Company@123')", example = "SecurePassword@123")
    private String password;

    @NotNull(message = "Role is required")
    @Schema(description = "Assigned RBAC role", example = "EMPLOYEE")
    private RoleType role;

    // --- Employee Profile Details ---
    @NotBlank(message = "Employee code is required")
    @Size(min = 2, max = 20, message = "Employee code must be between 2 and 20 characters")
    @Schema(description = "Unique employee alphanumeric code", example = "EMP-00042")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    @Schema(description = "Full name of the employee", example = "Sarah Connor")
    private String fullName;

    @Schema(description = "Department identifier", example = "1")
    private Long departmentId;

    @Size(max = 80, message = "Designation cannot exceed 80 characters")
    @Schema(description = "Job title or designation", example = "Senior Security Analyst")
    private String designation;

    @Schema(description = "Date of joining", example = "2026-01-15")
    private LocalDate dateOfJoining;

    @Schema(description = "Manager employee ID (reporting line)", example = "2")
    private Long reportingToId;

    @Builder.Default
    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}
