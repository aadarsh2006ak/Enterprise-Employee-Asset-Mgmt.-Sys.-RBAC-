package com.company.eams.controller;

import com.company.eams.dto.request.ExportJobRequest;
import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.ExportJobResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Data Export & Background Jobs", description = "Endpoints for low-memory streaming CSV/Excel reports and async background export jobs with polling")
public class ExportController {

    private final ExportService exportService;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // =========================================================================
    // Direct Synchronous Streaming Endpoints
    // =========================================================================

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_READ', 'EXPORT_DATA')")
    @Operation(summary = "Direct streaming export of employees", description = "Streams employees in CSV or Excel (XLSX) format directly over HTTP without memory accumulation")
    public ResponseEntity<StreamingResponseBody> streamEmployees(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String search) {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String ext = format == ExportFormat.CSV ? "csv" : "xlsx";
        String filename = String.format("employees_%s.%s", timestamp, ext);
        String contentType = format == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        StreamingResponseBody responseBody = outputStream ->
                exportService.streamEmployees(outputStream, format, departmentId, status, search);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(responseBody);
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyAuthority('DEPARTMENT_MANAGE', 'EXPORT_DATA')")
    @Operation(summary = "Direct streaming export of departments", description = "Streams department list with manager and headcount metadata")
    public ResponseEntity<StreamingResponseBody> streamDepartments(
            @RequestParam(defaultValue = "CSV") ExportFormat format) {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String ext = format == ExportFormat.CSV ? "csv" : "xlsx";
        String filename = String.format("departments_%s.%s", timestamp, ext);
        String contentType = format == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        StreamingResponseBody responseBody = outputStream ->
                exportService.streamDepartments(outputStream, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(responseBody);
    }

    @GetMapping("/assets")
    @PreAuthorize("hasAnyAuthority('ASSET_READ', 'EXPORT_DATA')")
    @Operation(summary = "Direct streaming export of asset inventory", description = "Streams hardware and software assets with category and status filters")
    public ResponseEntity<StreamingResponseBody> streamAssets(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) String search) {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String ext = format == ExportFormat.CSV ? "csv" : "xlsx";
        String filename = String.format("assets_%s.%s", timestamp, ext);
        String contentType = format == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        StreamingResponseBody responseBody = outputStream ->
                exportService.streamAssets(outputStream, format, categoryId, status, search);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(responseBody);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    @Operation(summary = "Direct streaming export of audit logs", description = "Streams immutable system audit trail with JSON snapshot diffs (Admin/Manager only)")
    public ResponseEntity<StreamingResponseBody> streamAuditLogs(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String ext = format == ExportFormat.CSV ? "csv" : "xlsx";
        String filename = String.format("audit_logs_%s.%s", timestamp, ext);
        String contentType = format == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        StreamingResponseBody responseBody = outputStream ->
                exportService.streamAuditLogs(outputStream, format, entityName, entityId, action, username, userId, startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(responseBody);
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyAuthority('ASSET_READ', 'EXPORT_DATA')")
    @Operation(summary = "Direct streaming export of asset assignments", description = "Streams active and historical asset assignment records")
    public ResponseEntity<StreamingResponseBody> streamAssignments(
            @RequestParam(defaultValue = "CSV") ExportFormat format,
            @RequestParam(required = false) Long assetId) {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String ext = format == ExportFormat.CSV ? "csv" : "xlsx";
        String filename = String.format("assignments_%s.%s", timestamp, ext);
        String contentType = format == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        StreamingResponseBody responseBody = outputStream ->
                exportService.streamAssignmentHistory(outputStream, format, assetId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(responseBody);
    }

    // =========================================================================
    // Asynchronous Export Job Workflows (Bulkhead Pattern)
    // =========================================================================

    @PostMapping("/jobs")
    @PreAuthorize("hasAuthority('EXPORT_DATA')")
    @Operation(summary = "Submit async export job", description = "Dispatches export task to dedicated background executor thread pool and returns job tracking token")
    public ResponseEntity<ApiResponse<ExportJobResponse>> createExportJob(
            @Valid @RequestBody ExportJobRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ExportJobResponse response = exportService.createExportJob(request, currentUser);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Export job submitted successfully"));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAuthority('EXPORT_DATA')")
    @Operation(summary = "List export jobs", description = "Retrieve paginated export jobs submitted by the current user (Admins can view all jobs)")
    public ResponseEntity<ApiResponse<PageResponse<ExportJobResponse>>> getUserExportJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<ExportJobResponse> response = exportService.getUserExportJobs(pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Export jobs retrieved successfully"));
    }

    @GetMapping("/jobs/{jobUuid}")
    @PreAuthorize("hasAuthority('EXPORT_DATA')")
    @Operation(summary = "Poll export job status", description = "Check real-time progress percentage (0-100) and execution status of an async export job")
    public ResponseEntity<ApiResponse<ExportJobResponse>> getExportJobStatus(
            @PathVariable String jobUuid,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ExportJobResponse response = exportService.getExportJobStatus(jobUuid, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Export job status retrieved"));
    }

    @GetMapping("/jobs/{jobUuid}/download")
    @PreAuthorize("hasAuthority('EXPORT_DATA')")
    @Operation(summary = "Download generated export file", description = "Streams the completed report file generated by the async export job")
    public ResponseEntity<Resource> downloadJobFile(
            @PathVariable String jobUuid,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ExportJobResponse job = exportService.getExportJobStatus(jobUuid, currentUser);
        File file = exportService.getExportJobFile(jobUuid, currentUser);
        Resource resource = new FileSystemResource(file);

        String contentType = job.getFormat() == ExportFormat.CSV
                ? "text/csv; charset=UTF-8"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()))
                .body(resource);
    }
}
