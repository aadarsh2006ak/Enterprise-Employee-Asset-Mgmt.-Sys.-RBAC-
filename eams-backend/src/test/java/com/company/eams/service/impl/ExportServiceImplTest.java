package com.company.eams.service.impl;

import com.company.eams.dto.request.ExportJobRequest;
import com.company.eams.dto.response.ExportJobResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.ExportJob;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.entity.enums.ExportJobStatus;
import com.company.eams.entity.enums.ExportType;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.export.service.AsyncExportWorker;
import com.company.eams.repository.*;
import com.company.eams.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private ExportJobRepository exportJobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AssetAssignmentRepository assetAssignmentRepository;

    @Mock
    private AsyncExportWorker asyncExportWorker;

    @InjectMocks
    private ExportServiceImpl exportService;

    private User sampleUser;
    private UserPrincipal adminPrincipal;
    private UserPrincipal employeePrincipal;
    private ExportJob sampleJob;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@company.com")
                .build();

        adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .username("admin")
                .role(RoleType.ADMIN)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        employeePrincipal = UserPrincipal.builder()
                .id(2L)
                .username("employee")
                .role(RoleType.EMPLOYEE)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                .build();

        sampleJob = ExportJob.builder()
                .id(10L)
                .jobUuid("uuid-1234")
                .user(sampleUser)
                .exportType(ExportType.ASSETS)
                .format(ExportFormat.CSV)
                .status(ExportJobStatus.COMPLETED)
                .fileName("assets.csv")
                .filePath("target/test.csv")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Create export job successfully")
    void testCreateExportJobSuccess() {
        ExportJobRequest request = new ExportJobRequest();
        request.setExportType(ExportType.ASSETS);
        request.setFormat(ExportFormat.CSV);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(exportJobRepository.save(any(ExportJob.class))).thenReturn(sampleJob);

        ExportJobResponse response = exportService.createExportJob(request, adminPrincipal);

        assertNotNull(response);
        assertEquals("uuid-1234", response.getJobUuid());
        assertEquals(ExportType.ASSETS, response.getExportType());
        verify(asyncExportWorker).processExportJob(sampleJob.getId());
    }

    @Test
    @DisplayName("Get export job status success for owner")
    void testGetExportJobStatusSuccess() {
        when(exportJobRepository.findByJobUuid("uuid-1234")).thenReturn(Optional.of(sampleJob));

        ExportJobResponse response = exportService.getExportJobStatus("uuid-1234", adminPrincipal);

        assertNotNull(response);
        assertEquals("uuid-1234", response.getJobUuid());
        assertEquals(ExportJobStatus.COMPLETED, response.getStatus());
    }

    @Test
    @DisplayName("Get export job status throws AccessDeniedException for unauthorized user")
    void testGetExportJobStatusUnauthorizedThrows() {
        when(exportJobRepository.findByJobUuid("uuid-1234")).thenReturn(Optional.of(sampleJob));

        assertThrows(AccessDeniedException.class, () ->
                exportService.getExportJobStatus("uuid-1234", employeePrincipal)
        );
    }

    @Test
    @DisplayName("Get user export jobs paginated")
    void testGetUserExportJobs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExportJob> page = new PageImpl<>(List.of(sampleJob), pageable, 1);

        when(exportJobRepository.findAll(pageable)).thenReturn(page);

        PageResponse<ExportJobResponse> response = exportService.getUserExportJobs(pageable, adminPrincipal);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("uuid-1234", response.getContent().get(0).getJobUuid());
    }

    @Test
    @DisplayName("Stream assignment history without N+1 queries")
    void testStreamAssignmentHistory() {
        when(assetAssignmentRepository.findAllWithDetails()).thenReturn(List.of());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> exportService.streamAssignmentHistory(os, ExportFormat.CSV, null));
        verify(assetAssignmentRepository).findAllWithDetails();
    }
}
