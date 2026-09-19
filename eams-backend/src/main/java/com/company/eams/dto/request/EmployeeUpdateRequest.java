package com.company.eams.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Employee profile update payload")
public class EmployeeUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    @Schema(description = "Full name of the employee", example = "Sarah Connor")
    private String fullName;

    @Schema(description = "Department identifier", example = "2")
    private Long departmentId;

    @Size(max = 80, message = "Designation cannot exceed 80 characters")
    @Schema(description = "Job title or designation", example = "Lead Security Analyst")
    private String designation;

    @Schema(description = "Date of joining", example = "2026-01-15")
    private LocalDate dateOfJoining;

    @Schema(description = "Manager employee ID (reporting line)", example = "2")
    private Long reportingToId;
}
