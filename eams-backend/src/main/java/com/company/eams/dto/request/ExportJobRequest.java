package com.company.eams.dto.request;

import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.entity.enums.ExportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to trigger asynchronous bulk export")
public class ExportJobRequest {

    @NotNull(message = "Export type is required")
    @Schema(description = "Domain entity type to export", example = "EMPLOYEES")
    private ExportType exportType;

    @NotNull(message = "Export format is required")
    @Schema(description = "File format for output", example = "CSV")
    private ExportFormat format;

    @Schema(description = "Optional filter criteria key-value pairs (e.g., departmentId, status, search, startDate, endDate)")
    private Map<String, Object> filters;
}
