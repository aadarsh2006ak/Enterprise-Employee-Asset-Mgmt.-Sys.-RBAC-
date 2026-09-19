package com.company.eams.dto.request;

import com.company.eams.entity.enums.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Asset metadata update payload")
public class AssetUpdateRequest {

    @NotNull(message = "Category ID is required")
    @Schema(description = "Asset Category ID", example = "1")
    private Long categoryId;

    @Size(max = 120, message = "Model name cannot exceed 120 characters")
    @Schema(description = "Device or hardware model name", example = "Apple MacBook Pro 16-inch M3 Max (36GB)")
    private String modelName;

    @Size(max = 120, message = "Serial number cannot exceed 120 characters")
    @Schema(description = "Manufacturer serial number", example = "C02G1234MD6R")
    private String serialNumber;

    @Schema(description = "Purchase date", example = "2026-02-10")
    private LocalDate purchaseDate;

    @Schema(description = "Asset status", example = "AVAILABLE")
    private AssetStatus status;

    @Schema(description = "Optimistic locking version token for concurrent update protection", example = "0")
    private Integer version;
}
