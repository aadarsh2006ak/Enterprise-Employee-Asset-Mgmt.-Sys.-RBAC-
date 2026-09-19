package com.company.eams.controller;

import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.AuditLogResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Audit Logging & Querying", description = "Endpoints for inspecting immutable audit trail and historical state transitions")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    @Operation(summary = "Query audit logs with multi-field filtering", description = "Retrieve paginated immutable audit logs filtered by entity, action, user, date range, or ID")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<AuditLogResponse> response = auditLogService.getAllAuditLogs(
                pageable, entityName, entityId, action, username, userId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    @Operation(summary = "Get audit log details by ID", description = "Retrieve single audit log entry with complete pre- and post-execution JSON snapshots")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(@PathVariable Long id) {
        AuditLogResponse response = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Audit log details retrieved"));
    }
}
