package com.company.eams.dto.request;

import com.company.eams.entity.enums.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employee status change payload")
public class EmployeeStatusUpdateRequest {

    @NotNull(message = "Employee status is required")
    @Schema(description = "New employment status (ACTIVE, RESIGNED, TERMINATED)", example = "RESIGNED")
    private EmployeeStatus status;
}
