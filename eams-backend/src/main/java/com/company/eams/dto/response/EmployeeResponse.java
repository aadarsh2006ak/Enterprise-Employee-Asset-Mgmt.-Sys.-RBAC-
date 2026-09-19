package com.company.eams.dto.response;

import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed employee profile response")
public class EmployeeResponse {

    @Schema(description = "Employee profile ID", example = "1")
    private Long id;

    @Schema(description = "Linked User account ID", example = "1")
    private Long userId;

    @Schema(description = "System username", example = "sarah.connor")
    private String username;

    @Schema(description = "Email address", example = "sarah.connor@company.com")
    private String email;

    @Schema(description = "Assigned RBAC role", example = "EMPLOYEE")
    private RoleType role;

    @Schema(description = "User account active status", example = "true")
    private Boolean isUserActive;

    @Schema(description = "Unique employee code", example = "EMP-00042")
    private String employeeCode;

    @Schema(description = "Full name", example = "Sarah Connor")
    private String fullName;

    @Schema(description = "Department ID", example = "2")
    private Long departmentId;

    @Schema(description = "Department Name", example = "Engineering")
    private String departmentName;

    @Schema(description = "Designation", example = "Senior Security Analyst")
    private String designation;

    @Schema(description = "Date of joining", example = "2026-01-15")
    private LocalDate dateOfJoining;

    @Schema(description = "Reporting Manager Employee ID", example = "2")
    private Long reportingToId;

    @Schema(description = "Reporting Manager Full Name", example = "Engineering Manager")
    private String reportingToName;

    @Schema(description = "Reporting Manager Employee Code", example = "EMP-00002")
    private String reportingToCode;

    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status;

    @Schema(description = "Profile creation timestamp", example = "2026-09-18T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Profile last update timestamp", example = "2026-09-18T10:00:00Z")
    private Instant updatedAt;

    @Schema(description = "Optimistic locking version", example = "0")
    private Integer version;
}
