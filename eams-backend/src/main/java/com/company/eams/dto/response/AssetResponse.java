package com.company.eams.dto.response;

import com.company.eams.entity.enums.AssetStatus;
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
@Schema(description = "Asset details response")
public class AssetResponse {

    @Schema(description = "Asset ID", example = "1")
    private Long id;

    @Schema(description = "Asset Tag / Barcode", example = "AST-MBP-2026-001")
    private String assetTag;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Category Name", example = "Laptop")
    private String categoryName;

    @Schema(description = "Model Name", example = "Apple MacBook Pro 16-inch M3 Max")
    private String modelName;

    @Schema(description = "Manufacturer Serial Number", example = "C02G1234MD6R")
    private String serialNumber;

    @Schema(description = "Purchase Date", example = "2026-02-10")
    private LocalDate purchaseDate;

    @Schema(description = "Current Asset Status", example = "AVAILABLE")
    private AssetStatus status;

    // --- Active Assignment Details (if currently assigned) ---
    @Schema(description = "Assigned Employee ID (if currently assigned)", example = "1")
    private Long assignedToEmployeeId;

    @Schema(description = "Assigned Employee Full Name", example = "Sarah Connor")
    private String assignedToEmployeeName;

    @Schema(description = "Assigned Employee Code", example = "EMP-00042")
    private String assignedToEmployeeCode;

    @Schema(description = "Assignment Timestamp (if currently assigned)", example = "2026-09-18T10:00:00Z")
    private Instant assignedAt;

    @Schema(description = "Created timestamp", example = "2026-09-18T08:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp", example = "2026-09-18T08:00:00Z")
    private Instant updatedAt;

    @Schema(description = "Optimistic locking version token", example = "0")
    private Integer version;
}
