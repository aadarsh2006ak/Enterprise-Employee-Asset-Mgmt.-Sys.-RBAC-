package com.company.eams.dto.request;

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
@Schema(description = "Asset assignment payload")
public class AssetAssignRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "ID of the employee receiving the asset", example = "1")
    private Long employeeId;

    @Schema(description = "Condition notes upon handover", example = "Brand new in box with standard 140W USB-C charger and cable")
    private String conditionNotes;
}
