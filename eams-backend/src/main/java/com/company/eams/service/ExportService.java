package com.company.eams.service;

import com.company.eams.dto.request.ExportJobRequest;
import com.company.eams.dto.response.ExportJobResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.OutputStream;
import java.time.Instant;

public interface ExportService {

    // Async Export Job Management
    ExportJobResponse createExportJob(ExportJobRequest request, UserPrincipal currentUser);

    ExportJobResponse getExportJobStatus(String jobUuid, UserPrincipal currentUser);

    PageResponse<ExportJobResponse> getUserExportJobs(Pageable pageable, UserPrincipal currentUser);

    File getExportJobFile(String jobUuid, UserPrincipal currentUser);

    // Synchronous Direct HTTP Streaming
    void streamEmployees(OutputStream os, ExportFormat format, Long departmentId, EmployeeStatus status, String search);

    void streamDepartments(OutputStream os, ExportFormat format);

    void streamAssets(OutputStream os, ExportFormat format, Long categoryId, AssetStatus status, String search);

    void streamAuditLogs(OutputStream os, ExportFormat format, String entityName, Long entityId, AuditAction action, String username, Long userId, Instant startDate, Instant endDate);

    void streamAssignmentHistory(OutputStream os, ExportFormat format, Long assetId);
}
