package com.company.eams.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Asset assignment record details")
public class AssetAssignmentResponse {

    @Schema(description = "Assignment record ID", example = "1")
    private Long id;

    @Schema(description = "Asset ID", example = "1")
    private Long assetId;

    @Schema(description = "Asset Tag", example = "AST-MBP-2026-001")
    private String assetTag;

    @Schema(description = "Asset Model Name", example = "Apple MacBook Pro 16-inch M3 Max")
    private String assetModelName;

    @Schema(description = "Category Name", example = "Laptop")
    private String categoryName;

    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;

    @Schema(description = "Employee Full Name", example = "Sarah Connor")
    private String employeeName;

    @Schema(description = "Employee Code", example = "EMP-00042")
    private String employeeCode;

    @Schema(description = "User ID who performed the assignment", example = "1")
    private Long assignedByUserId;

    @Schema(description = "Username who performed the assignment", example = "admin")
    private String assignedByUsername;

    @Schema(description = "Date and time when the asset was assigned", example = "2026-09-18T10:00:00Z")
    private Instant assignedAt;

    @Schema(description = "Date and time when the asset was returned (null if active)", example = "2026-09-25T14:30:00Z")
    private Instant returnedAt;

    @Schema(description = "Condition notes recorded during handover or return", example = "Brand new in box with 140W power adapter")
    private String conditionNotes;

    @Schema(description = "Indicates whether the assignment is currently active", example = "true")
    private boolean isActive;
}
