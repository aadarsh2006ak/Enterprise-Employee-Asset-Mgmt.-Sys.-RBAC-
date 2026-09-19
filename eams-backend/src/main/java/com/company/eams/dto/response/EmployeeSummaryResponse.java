package com.company.eams.dto.response;

import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lightweight employee summary for paginated listings")
public class EmployeeSummaryResponse {

    @Schema(description = "Employee ID", example = "1")
    private Long id;

    @Schema(description = "Employee alphanumeric code", example = "EMP-00001")
    private String employeeCode;

    @Schema(description = "Full name", example = "Sarah Connor")
    private String fullName;

    @Schema(description = "Email address", example = "sarah.connor@company.com")
    private String email;

    @Schema(description = "Department Name", example = "Engineering")
    private String departmentName;

    @Schema(description = "Designation", example = "Lead Security Analyst")
    private String designation;

    @Schema(description = "Role", example = "EMPLOYEE")
    private RoleType role;

    @Schema(description = "Employment status", example = "ACTIVE")
    private EmployeeStatus status;

    @Schema(description = "Date of joining", example = "2026-01-15")
    private LocalDate dateOfJoining;
}
