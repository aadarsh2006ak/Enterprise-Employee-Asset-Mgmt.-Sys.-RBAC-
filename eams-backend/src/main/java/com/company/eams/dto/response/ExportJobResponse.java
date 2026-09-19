package com.company.eams.dto.response;

import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.entity.enums.ExportJobStatus;
import com.company.eams.entity.enums.ExportType;
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
@Schema(description = "Status, progress, and download details of an asynchronous export job")
public class ExportJobResponse {

    @Schema(description = "Unique Job UUID token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String jobUuid;

    @Schema(description = "Domain entity type being exported", example = "EMPLOYEES")
    private ExportType exportType;

    @Schema(description = "Exported file format", example = "CSV")
    private ExportFormat format;

    @Schema(description = "Job execution status", example = "COMPLETED")
    private ExportJobStatus status;

    @Schema(description = "Real-time progress percentage (0 - 100)", example = "100")
    private Integer progressPercentage;

    @Schema(description = "Total number of matching records", example = "1500")
    private Integer totalRecords;

    @Schema(description = "Number of records processed so far", example = "1500")
    private Integer processedRecords;

    @Schema(description = "Generated file name", example = "employees_export_20260918_141500.csv")
    private String fileName;

    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSizeBytes;

    @Schema(description = "Error message if job failed", example = "null")
    private String errorMessage;

    @Schema(description = "Job submission timestamp in UTC", example = "2026-09-18T08:30:00Z")
    private Instant createdAt;

    @Schema(description = "Job completion timestamp in UTC", example = "2026-09-18T08:30:05Z")
    private Instant completedAt;

    @Schema(description = "Expiration timestamp when file is scheduled for deletion", example = "2026-09-19T08:30:05Z")
    private Instant expiresAt;

    @Schema(description = "Direct download URL", example = "/api/v1/exports/jobs/550e8400-e29b-41d4-a716-446655440000/download")
    private String downloadUrl;
}
