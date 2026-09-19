package com.company.eams.dto.request;

import com.company.eams.entity.enums.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Asset registration payload")
public class AssetCreateRequest {

    @NotBlank(message = "Asset tag is required")
    @Size(min = 2, max = 30, message = "Asset tag must be between 2 and 30 characters")
    @Schema(description = "Unique organizational asset tag / barcode", example = "AST-MBP-2026-001")
    private String assetTag;

    @NotNull(message = "Category ID is required")
    @Schema(description = "Asset Category ID", example = "1")
    private Long categoryId;

    @Size(max = 120, message = "Model name cannot exceed 120 characters")
    @Schema(description = "Device or hardware model name", example = "Apple MacBook Pro 16-inch M3 Max")
    private String modelName;

    @Size(max = 120, message = "Serial number cannot exceed 120 characters")
    @Schema(description = "Manufacturer serial number", example = "C02G1234MD6R")
    private String serialNumber;

    @Schema(description = "Purchase or procurement date", example = "2026-02-10")
    private LocalDate purchaseDate;

    @Builder.Default
    @Schema(description = "Initial inventory status (defaults to AVAILABLE)", example = "AVAILABLE")
    private AssetStatus status = AssetStatus.AVAILABLE;
}
