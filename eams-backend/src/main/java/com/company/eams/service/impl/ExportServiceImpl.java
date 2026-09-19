package com.company.eams.service.impl;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.dto.request.ExportJobRequest;
import com.company.eams.dto.response.ExportJobResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.*;
import com.company.eams.entity.enums.*;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.export.generator.CsvStreamGenerator;
import com.company.eams.export.generator.ExcelStreamGenerator;
import com.company.eams.export.service.AsyncExportWorker;
import com.company.eams.repository.*;
import com.company.eams.repository.specification.AssetSpecification;
import com.company.eams.repository.specification.AuditLogSpecification;
import com.company.eams.repository.specification.EmployeeSpecification;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetRepository assetRepository;
    private final AuditLogRepository auditLogRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final AsyncExportWorker asyncExportWorker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "ExportJob", description = "Initiate asynchronous export job")
    public ExportJobResponse createExportJob(ExportJobRequest request, UserPrincipal currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        String jobUuid = UUID.randomUUID().toString();
        ExportJob job = ExportJob.builder()
                .jobUuid(jobUuid)
                .user(user)
                .exportType(request.getExportType())
                .format(request.getFormat())
                .status(ExportJobStatus.PENDING)
                .progressPercentage(0)
                .filterParams(request.getFilters() != null ? request.getFilters() : new HashMap<>())
                .createdAt(Instant.now())
                .build();

        ExportJob saved = exportJobRepository.save(job);
        log.info("Registered async export job: uuid={} type={} format={} user={}",
                jobUuid, request.getExportType(), request.getFormat(), currentUser.getUsername());

        // Dispatch background worker
        asyncExportWorker.processExportJob(saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportJobResponse getExportJobStatus(String jobUuid, UserPrincipal currentUser) {
        ExportJob job = exportJobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", "jobUuid", jobUuid));

        validateJobAccess(job, currentUser);
        return mapToResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExportJobResponse> getUserExportJobs(Pageable pageable, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<ExportJob> page = isAdmin
                ? exportJobRepository.findAll(pageable)
                : exportJobRepository.findByUserId(currentUser.getId(), pageable);

        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public File getExportJobFile(String jobUuid, UserPrincipal currentUser) {
        ExportJob job = exportJobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", "jobUuid", jobUuid));

        validateJobAccess(job, currentUser);

        if (job.getStatus() != ExportJobStatus.COMPLETED) {
            throw new IllegalStateException("Export job is not completed yet (current status: " + job.getStatus() + ")");
        }

        if (job.getFilePath() == null) {
            throw new ResourceNotFoundException("Export file path is missing for job: " + jobUuid);
        }

        File file = new File(job.getFilePath());
        if (!file.exists() || !file.canRead()) {
            throw new ResourceNotFoundException("Export file not found on disk: " + job.getFileName());
        }

        return file;
    }

    // =========================================================================
    // Synchronous Direct HTTP Streaming
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public void streamEmployees(OutputStream os, ExportFormat format, Long departmentId, EmployeeStatus status, String search) {
        try {
            List<String> headers = List.of("ID", "Employee Code", "Full Name", "Username", "Email",
                    "Department", "Designation", "Date of Joining", "Reporting Manager", "Status", "Created At");

            Specification<Employee> spec = EmployeeSpecification.filter(departmentId, status, search);
            int page = 0;
            int size = 500;

            if (format == ExportFormat.CSV) {
                try (CsvStreamGenerator csv = new CsvStreamGenerator(os, headers)) {
                    while (true) {
                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
                        Page<Employee> batch = employeeRepository.findAll(spec, pageable);
                        for (Employee emp : batch.getContent()) {
                            csv.writeRow(
                                    emp.getId(),
                                    emp.getEmployeeCode(),
                                    emp.getFullName(),
                                    emp.getUser() != null ? emp.getUser().getUsername() : "",
                                    emp.getUser() != null ? emp.getUser().getEmail() : "",
                                    emp.getDepartment() != null ? emp.getDepartment().getName() : "",
                                    emp.getDesignation() != null ? emp.getDesignation() : "",
                                    emp.getDateOfJoining() != null ? emp.getDateOfJoining().toString() : "",
                                    emp.getReportingTo() != null ? emp.getReportingTo().getFullName() : "",
                                    emp.getStatus() != null ? emp.getStatus().name() : "",
                                    emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : ""
                            );
                        }
                        if (!batch.hasNext()) break;
                        page++;
                    }
                }
            } else {
                try (ExcelStreamGenerator excel = new ExcelStreamGenerator("Employees", headers)) {
                    while (true) {
                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
                        Page<Employee> batch = employeeRepository.findAll(spec, pageable);
                        for (Employee emp : batch.getContent()) {
                            excel.writeRow(List.of(
                                    emp.getId() != null ? emp.getId() : "",
                                    emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "",
                                    emp.getFullName() != null ? emp.getFullName() : "",
                                    emp.getUser() != null ? emp.getUser().getUsername() : "",
                                    emp.getUser() != null ? emp.getUser().getEmail() : "",
                                    emp.getDepartment() != null ? emp.getDepartment().getName() : "",
                                    emp.getDesignation() != null ? emp.getDesignation() : "",
                                    emp.getDateOfJoining() != null ? emp.getDateOfJoining().toString() : "",
                                    emp.getReportingTo() != null ? emp.getReportingTo().getFullName() : "",
                                    emp.getStatus() != null ? emp.getStatus().name() : "",
                                    emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : ""
                            ));
                        }
                        if (!batch.hasNext()) break;
                        page++;
                    }
                    excel.writeTo(os);
                }
            }
        } catch (Exception e) {
            log.error("Error during direct employees streaming export: {}", e.getMessage(), e);
            throw new RuntimeException("Export streaming error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamDepartments(OutputStream os, ExportFormat format) {
        try {
            List<String> headers = List.of("ID", "Department Name", "Manager Name", "Manager Code", "Total Employees");
            List<Department> departments = departmentRepository.findAllWithManager();

            Map<Long, Long> deptCounts = new HashMap<>();
            List<Object[]> counts = departmentRepository.countEmployeesGroupedByDepartment();
            for (Object[] row : counts) {
                deptCounts.put((Long) row[0], (Long) row[1]);
            }

            if (format == ExportFormat.CSV) {
                try (CsvStreamGenerator csv = new CsvStreamGenerator(os, headers)) {
                    for (Department dept : departments) {
                        long empCount = deptCounts.getOrDefault(dept.getId(), 0L);
                        csv.writeRow(
                                dept.getId(),
                                dept.getName(),
                                dept.getManager() != null ? dept.getManager().getFullName() : "",
                                dept.getManager() != null ? dept.getManager().getEmployeeCode() : "",
                                empCount
                        );
                    }
                }
            } else {
                try (ExcelStreamGenerator excel = new ExcelStreamGenerator("Departments", headers)) {
                    for (Department dept : departments) {
                        long empCount = deptCounts.getOrDefault(dept.getId(), 0L);
                        excel.writeRow(List.of(
                                dept.getId() != null ? dept.getId() : "",
                                dept.getName() != null ? dept.getName() : "",
                                dept.getManager() != null ? dept.getManager().getFullName() : "",
                                dept.getManager() != null ? dept.getManager().getEmployeeCode() : "",
                                empCount
                        ));
                    }
                    excel.writeTo(os);
                }
            }
        } catch (Exception e) {
            log.error("Error during direct departments streaming export: {}", e.getMessage(), e);
            throw new RuntimeException("Export streaming error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamAssets(OutputStream os, ExportFormat format, Long categoryId, AssetStatus status, String search) {
        try {
            List<String> headers = List.of("ID", "Asset Tag", "Category", "Model Name", "Serial Number",
                    "Purchase Date", "Status", "Created At");

            Specification<Asset> spec = AssetSpecification.filter(categoryId, status, search);
            int page = 0;
            int size = 500;

            if (format == ExportFormat.CSV) {
                try (CsvStreamGenerator csv = new CsvStreamGenerator(os, headers)) {
                    while (true) {
                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
                        Page<Asset> batch = assetRepository.findAll(spec, pageable);
                        for (Asset asset : batch.getContent()) {
                            csv.writeRow(
                                    asset.getId(),
                                    asset.getAssetTag(),
                                    asset.getCategory() != null ? asset.getCategory().getName() : "",
                                    asset.getModelName() != null ? asset.getModelName() : "",
                                    asset.getSerialNumber() != null ? asset.getSerialNumber() : "",
                                    asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : "",
                                    asset.getStatus() != null ? asset.getStatus().name() : "",
                                    asset.getCreatedAt() != null ? asset.getCreatedAt().toString() : ""
                            );
                        }
                        if (!batch.hasNext()) break;
                        page++;
                    }
                }
            } else {
                try (ExcelStreamGenerator excel = new ExcelStreamGenerator("Assets", headers)) {
                    while (true) {
                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
                        Page<Asset> batch = assetRepository.findAll(spec, pageable);
                        for (Asset asset : batch.getContent()) {
                            excel.writeRow(List.of(
                                    asset.getId() != null ? asset.getId() : "",
                                    asset.getAssetTag() != null ? asset.getAssetTag() : "",
                                    asset.getCategory() != null ? asset.getCategory().getName() : "",
                                    asset.getModelName() != null ? asset.getModelName() : "",
                                    asset.getSerialNumber() != null ? asset.getSerialNumber() : "",
                                    asset.getPurchaseDate() != null ? asset.getPurchaseDate().toString() : "",
                                    asset.getStatus() != null ? asset.getStatus().name() : "",
                                    asset.getCreatedAt() != null ? asset.getCreatedAt().toString() : ""
                            ));
                        }
                        if (!batch.hasNext()) break;
                        page++;
                    }
                    excel.writeTo(os);
                }
            }
        } catch (Exception e) {
            log.error("Error during direct assets streaming export: {}", e.getMessage(), e);
            throw new RuntimeException("Export streaming error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamAuditLogs(OutputStream os, ExportFormat format, String entityName, Long entityId,
                                AuditAction action, String username, Long userId, Instant startDate, Instant endDate) {
        try {
            List<String> headers = List.of("ID", "Timestamp (UTC)", "User ID", "Username", "Action",
                    "Entity Name", "Entity ID", "IP Address", "Old State (JSON)", "New State (JSON)");

            Specification<AuditLog> spec = AuditLogSpecification.filter(entityName, entityId, action, username, userId, startDate, endDate);
            int page = 0;
            int size = 500;

            if (format == ExportFormat.CSV) {
                try (CsvStreamGenerator csv = new CsvStreamGenerator(os, headers)) {
                    while (true) {
                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                        Page<AuditLog> batch = auditLogRepository.findAll(spec, pageable);
                        for (AuditLog logEntry : batch.getContent()) {
                            csv.writeRow(
                                    logEntry.getId(),
                                    logEntry.getCreatedAt() != null ? logEntry.getCreatedAt().toString() : "",
                                    logEntry.getUser() != null ? logEntry.getUser().getId() : "",
                                    logEntry.getUsernameSnapshot() != null ? logEntry.getUsernameSnapshot() : "",
                                    logEntry.getAction() != null ? logEntry.getAction().name() : "",
                                    logEntry.getEntityName(),
                                    logEntry.getEntityId() != null ? logEntry.getEntityId() : "",
                                    logEntry.getIpAddress() != null ? logEntry.getIpAddress() : "",
                                    logEntry.getOldValue() != null ? objectMapper.writeValueAsString(logEntry.getOldValue()) : "",
                                    logEntry.getNewValue() != null ? objectMapper.writeValueAsString(logEntry.getNewValue()) : ""
                            );
                        }
                        if (!batch.hasNext()) break;
                        page++;
                    }
                }
            } else {
                try (ExcelStreamGenerator excel = new ExcelStreamGenerator("AuditLogs", headers)) {
                    while (true) {
                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                        Page<AuditLog> batch = auditLogRepository.findAll(spec, pageable);
                        for (AuditLog logEntry : batch.getContent()) {
                            excel.writeRow(List.of(
                                    logEntry.getId() != null ? logEntry.getId() : "",
                                    logEntry.getCreatedAt() != null ? logEntry.getCreatedAt().toString() : "",
                                    logEntry.getUser() != null ? logEntry.getUser().getId() : "",
                                    logEntry.getUsernameSnapshot() != null ? logEntry.getUsernameSnapshot() : "",
                                    logEntry.getAction() != null ? logEntry.getAction().name() : "",
                                    logEntry.getEntityName(),
                                    logEntry.getEntityId() != null ? logEntry.getEntityId() : "",
                                    logEntry.getIpAddress() != null ? logEntry.getIpAddress() : "",
                                    logEntry.getOldValue() != null ? objectMapper.writeValueAsString(logEntry.getOldValue()) : "",
                                    logEntry.getNewValue() != null ? objectMapper.writeValueAsString(logEntry.getNewValue()) : ""
                            ));
                        }
                        if (!batch.hasNext()) break;
                        page++;
                    }
                    excel.writeTo(os);
                }
            }
        } catch (Exception e) {
            log.error("Error during direct audit logs streaming export: {}", e.getMessage(), e);
            throw new RuntimeException("Export streaming error: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void streamAssignmentHistory(OutputStream os, ExportFormat format, Long assetId) {
        try {
            List<String> headers = List.of("ID", "Asset Tag", "Asset Model", "Employee Code",
                    "Employee Name", "Assigned By", "Assigned At", "Returned At", "Condition Notes");

            List<AssetAssignment> assignments = assetId != null
                    ? assetAssignmentRepository.findHistoryByAssetId(assetId)
                    : assetAssignmentRepository.findAll();

            if (format == ExportFormat.CSV) {
                try (CsvStreamGenerator csv = new CsvStreamGenerator(os, headers)) {
                    for (AssetAssignment aa : assignments) {
                        csv.writeRow(
                                aa.getId(),
                                aa.getAsset() != null ? aa.getAsset().getAssetTag() : "",
                                aa.getAsset() != null ? aa.getAsset().getModelName() : "",
                                aa.getEmployee() != null ? aa.getEmployee().getEmployeeCode() : "",
                                aa.getEmployee() != null ? aa.getEmployee().getFullName() : "",
                                aa.getAssignedBy() != null ? aa.getAssignedBy().getUsername() : "",
                                aa.getAssignedAt() != null ? aa.getAssignedAt().toString() : "",
                                aa.getReturnedAt() != null ? aa.getReturnedAt().toString() : "ACTIVE",
                                aa.getConditionNotes() != null ? aa.getConditionNotes() : ""
                        );
                    }
                }
            } else {
                try (ExcelStreamGenerator excel = new ExcelStreamGenerator("Assignments", headers)) {
                    for (AssetAssignment aa : assignments) {
                        excel.writeRow(List.of(
                                aa.getId() != null ? aa.getId() : "",
                                aa.getAsset() != null ? aa.getAsset().getAssetTag() : "",
                                aa.getAsset() != null ? aa.getAsset().getModelName() : "",
                                aa.getEmployee() != null ? aa.getEmployee().getEmployeeCode() : "",
                                aa.getEmployee() != null ? aa.getEmployee().getFullName() : "",
                                aa.getAssignedBy() != null ? aa.getAssignedBy().getUsername() : "",
                                aa.getAssignedAt() != null ? aa.getAssignedAt().toString() : "",
                                aa.getReturnedAt() != null ? aa.getReturnedAt().toString() : "ACTIVE",
                                aa.getConditionNotes() != null ? aa.getConditionNotes() : ""
                        ));
                    }
                    excel.writeTo(os);
                }
            }
        } catch (Exception e) {
            log.error("Error during direct assignment history streaming export: {}", e.getMessage(), e);
            throw new RuntimeException("Export streaming error: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void validateJobAccess(ExportJob job, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && (job.getUser() == null || !job.getUser().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You are not authorized to view or download this export job");
        }
    }

    private ExportJobResponse mapToResponse(ExportJob job) {
        return ExportJobResponse.builder()
                .jobUuid(job.getJobUuid())
                .exportType(job.getExportType())
                .format(job.getFormat())
                .status(job.getStatus())
                .progressPercentage(job.getProgressPercentage())
                .totalRecords(job.getTotalRecords())
                .processedRecords(job.getProcessedRecords())
                .fileName(job.getFileName())
                .fileSizeBytes(job.getFileSizeBytes())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .expiresAt(job.getExpiresAt())
                .downloadUrl(job.getStatus() == ExportJobStatus.COMPLETED
                        ? "/api/v1/exports/jobs/" + job.getJobUuid() + "/download"
                        : null)
                .build();
    }
}
