package com.company.eams.export.service;

import com.company.eams.entity.*;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.entity.enums.ExportJobStatus;
import com.company.eams.export.generator.CsvStreamGenerator;
import com.company.eams.export.generator.ExcelStreamGenerator;
import com.company.eams.repository.*;
import com.company.eams.repository.specification.AssetSpecification;
import com.company.eams.repository.specification.AuditLogSpecification;
import com.company.eams.repository.specification.EmployeeSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncExportWorker {

    private final ExportJobRepository exportJobRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetRepository assetRepository;
    private final AuditLogRepository auditLogRepository;
    private final AssetAssignmentRepository assetAssignmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EXPORT_DIR = System.getProperty("java.io.tmpdir") + File.separator + "eams-exports";

    @Async("exportExecutor")
    @CircuitBreaker(name = "exportService", fallbackMethod = "handleExportCircuitBreakerFallback")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processExportJob(Long exportJobId) {
        ExportJob job = exportJobRepository.findById(exportJobId).orElse(null);
        if (job == null) {
            log.error("Export job id {} not found for processing", exportJobId);
            return;
        }

        try {
            updateJobStatus(job.getId(), ExportJobStatus.PROCESSING, 0, 0, 0, null, null, null, null);

            // Ensure export directory exists
            Path dirPath = Paths.get(EXPORT_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String fileExt = job.getFormat() == ExportFormat.CSV ? ".csv" : ".xlsx";
            String fileName = String.format("%s_%s%s", job.getExportType().name().toLowerCase(), job.getJobUuid(), fileExt);
            File outputFile = new File(dirPath.toFile(), fileName);

            int totalRecords = 0;

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                switch (job.getExportType()) {
                    case EMPLOYEES -> {
                        totalRecords = processEmployeesExport(fos, job.getFormat(), job.getFilterParams(), job.getId());
                    }
                    case DEPARTMENTS -> {
                        totalRecords = processDepartmentsExport(fos, job.getFormat(), job.getId());
                    }
                    case ASSETS -> {
                        totalRecords = processAssetsExport(fos, job.getFormat(), job.getFilterParams(), job.getId());
                    }
                    case AUDIT_LOGS -> {
                        totalRecords = processAuditLogsExport(fos, job.getFormat(), job.getFilterParams(), job.getId());
                    }
                    case ASSIGNMENT_HISTORY -> {
                        totalRecords = processAssignmentHistoryExport(fos, job.getFormat(), job.getFilterParams(), job.getId());
                    }
                }
            }

            long fileSizeBytes = outputFile.length();
            String contentType = job.getFormat() == ExportFormat.CSV
                    ? "text/csv; charset=UTF-8"
                    : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            updateJobCompleted(job.getId(), 100, totalRecords, totalRecords, outputFile.getAbsolutePath(), fileName, fileSizeBytes, contentType);
            log.info("Export job {} completed successfully. Generated file {} ({} bytes)",
                    job.getJobUuid(), fileName, fileSizeBytes);

        } catch (Exception ex) {
            log.error("Export job {} failed: {}", job.getJobUuid(), ex.getMessage(), ex);
            updateJobFailed(job.getId(), ex.getMessage());
        }
    }

    // =========================================================================
    // Core Entity Exporters
    // =========================================================================

    private int processEmployeesExport(OutputStream os, ExportFormat format, Map<String, Object> filters, Long jobId) throws Exception {
        List<String> headers = List.of("ID", "Employee Code", "Full Name", "Username", "Email",
                "Department", "Designation", "Date of Joining", "Reporting Manager", "Status", "Created At");

        Long departmentId = extractLong(filters, "departmentId");
        EmployeeStatus status = extractEnum(filters, "status", EmployeeStatus.class);
        String search = extractString(filters, "search");

        Specification<Employee> spec = EmployeeSpecification.filter(departmentId, status, search);
        long totalCount = employeeRepository.count(spec);
        int total = (int) totalCount;

        int page = 0;
        int size = 500;
        int processed = 0;

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
                        processed++;
                    }
                    updateProgress(jobId, total, processed);
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
                        processed++;
                    }
                    updateProgress(jobId, total, processed);
                    if (!batch.hasNext()) break;
                    page++;
                }
                excel.writeTo(os);
            }
        }
        return processed;
    }

    private int processDepartmentsExport(OutputStream os, ExportFormat format, Long jobId) throws Exception {
        List<String> headers = List.of("ID", "Department Name", "Manager Name", "Manager Code", "Total Employees");
        List<Department> departments = departmentRepository.findAllWithManager();
        int total = departments.size();
        int processed = 0;

        // Pre-fetch employee counts per department
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
                    processed++;
                    updateProgress(jobId, total, processed);
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
                    processed++;
                    updateProgress(jobId, total, processed);
                }
                excel.writeTo(os);
            }
        }
        return processed;
    }

    private int processAssetsExport(OutputStream os, ExportFormat format, Map<String, Object> filters, Long jobId) throws Exception {
        List<String> headers = List.of("ID", "Asset Tag", "Category", "Model Name", "Serial Number",
                "Purchase Date", "Status", "Created At");

        Long categoryId = extractLong(filters, "categoryId");
        AssetStatus status = extractEnum(filters, "status", AssetStatus.class);
        String search = extractString(filters, "search");

        Specification<Asset> spec = AssetSpecification.filter(categoryId, status, search);
        long totalCount = assetRepository.count(spec);
        int total = (int) totalCount;

        int page = 0;
        int size = 500;
        int processed = 0;

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
                        processed++;
                    }
                    updateProgress(jobId, total, processed);
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
                        processed++;
                    }
                    updateProgress(jobId, total, processed);
                    if (!batch.hasNext()) break;
                    page++;
                }
                excel.writeTo(os);
            }
        }
        return processed;
    }

    private int processAuditLogsExport(OutputStream os, ExportFormat format, Map<String, Object> filters, Long jobId) throws Exception {
        List<String> headers = List.of("ID", "Timestamp (UTC)", "User ID", "Username", "Action",
                "Entity Name", "Entity ID", "IP Address", "Old State (JSON)", "New State (JSON)");

        String entityName = extractString(filters, "entityName");
        Long entityId = extractLong(filters, "entityId");
        AuditAction action = extractEnum(filters, "action", AuditAction.class);
        String username = extractString(filters, "username");
        Long userId = extractLong(filters, "userId");
        Instant startDate = extractInstant(filters, "startDate");
        Instant endDate = extractInstant(filters, "endDate");

        Specification<AuditLog> spec = AuditLogSpecification.filter(entityName, entityId, action, username, userId, startDate, endDate);
        long totalCount = auditLogRepository.count(spec);
        int total = (int) totalCount;

        int page = 0;
        int size = 500;
        int processed = 0;

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
                        processed++;
                    }
                    updateProgress(jobId, total, processed);
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
                        processed++;
                    }
                    updateProgress(jobId, total, processed);
                    if (!batch.hasNext()) break;
                    page++;
                }
                excel.writeTo(os);
            }
        }
        return processed;
    }

    private int processAssignmentHistoryExport(OutputStream os, ExportFormat format, Map<String, Object> filters, Long jobId) throws Exception {
        List<String> headers = List.of("ID", "Asset Tag", "Asset Model", "Employee Code",
                "Employee Name", "Assigned By", "Assigned At", "Returned At", "Condition Notes");

        Long assetId = extractLong(filters, "assetId");
        List<AssetAssignment> assignments = assetId != null
                ? assetAssignmentRepository.findHistoryByAssetId(assetId)
                : assetAssignmentRepository.findAll();

        int total = assignments.size();
        int processed = 0;

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
                    processed++;
                    updateProgress(jobId, total, processed);
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
                    processed++;
                    updateProgress(jobId, total, processed);
                }
                excel.writeTo(os);
            }
        }
        return processed;
    }

    // =========================================================================
    // Helpers & State Mutators
    // =========================================================================

    private void updateProgress(Long jobId, int total, int processed) {
        if (jobId == null || total <= 0) return;
        int percentage = (int) (((double) processed / total) * 100);
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setProgressPercentage(percentage);
            exportJobRepository.save(job);
        });
    }

    private void updateJobStatus(Long jobId, ExportJobStatus status, int progress, int total, int processed,
                                String filePath, String fileName, Long fileSizeBytes, String contentType) {
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setProgressPercentage(progress);
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setStartedAt(Instant.now());
            if (filePath != null) job.setFilePath(filePath);
            if (fileName != null) job.setFileName(fileName);
            if (fileSizeBytes != null) job.setFileSizeBytes(fileSizeBytes);
            if (contentType != null) job.setContentType(contentType);
            exportJobRepository.save(job);
        });
    }

    private void updateJobCompleted(Long jobId, int progress, int total, int processed,
                                   String filePath, String fileName, Long fileSizeBytes, String contentType) {
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(ExportJobStatus.COMPLETED);
            job.setProgressPercentage(progress);
            job.setTotalRecords(total);
            job.setProcessedRecords(processed);
            job.setFilePath(filePath);
            job.setFileName(fileName);
            job.setFileSizeBytes(fileSizeBytes);
            job.setContentType(contentType);
            job.setCompletedAt(Instant.now());
            job.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
            exportJobRepository.save(job);
        });
    }

    private void updateJobFailed(Long jobId, String errorMsg) {
        exportJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(ExportJobStatus.FAILED);
            job.setErrorMessage(errorMsg != null ? errorMsg : "Unknown internal export error");
            job.setCompletedAt(Instant.now());
            exportJobRepository.save(job);
        });
    }

    private Long extractLong(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return null;
        Object val = map.get(key);
        if (val instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return null;
        String val = map.get(key).toString().trim();
        return val.isEmpty() ? null : val;
    }

    private <E extends Enum<E>> E extractEnum(Map<String, Object> map, String key, Class<E> enumClass) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return null;
        try {
            return Enum.valueOf(enumClass, map.get(key).toString().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private Instant extractInstant(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) return null;
        try {
            return Instant.parse(map.get(key).toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Resilience4j CircuitBreaker Fallback Handler:
     * Executed when exportService circuit is OPEN or call fails threshold criteria.
     */
    public void handleExportCircuitBreakerFallback(Long exportJobId, Throwable ex) {
        log.error("Circuit breaker 'exportService' tripped for export job ID {}: {}", exportJobId, ex.getMessage());
        updateJobFailed(exportJobId, "Export service temporarily unavailable due to high error rate / circuit open. Please retry later.");
    }
}
