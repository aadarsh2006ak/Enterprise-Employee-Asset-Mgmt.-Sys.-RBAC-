package com.company.eams.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Department update payload")
public class DepartmentUpdateRequest {

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    @Schema(description = "Updated department name", example = "Core Platform Engineering")
    private String name;

    @Schema(description = "Optional ID of the Employee managing this department (set null to unassign)", example = "2")
    private Long managerId;
}
