package com.company.eams.service.impl;

import com.company.eams.dto.response.AuditLogResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.AuditLog;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.AuditLogRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.mockito.ArgumentMatchers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("admin").build();
        sampleLog = AuditLog.builder()
                .id(100L)
                .user(user)
                .usernameSnapshot("admin")
                .entityName("Asset")
                .entityId(50L)
                .action(AuditAction.CREATE)
                .ipAddress("127.0.0.1")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Get all audit logs with filter specifications")
    void testGetAllAuditLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of(sampleLog), pageable, 1);

        when(auditLogRepository.findAll(ArgumentMatchers.<Specification<AuditLog>>any(), eq(pageable))).thenReturn(page);

        PageResponse<AuditLogResponse> response = auditLogService.getAllAuditLogs(
                pageable, "Asset", 50L, AuditAction.CREATE, "admin", 1L, null, null
        );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Asset", response.getContent().get(0).getEntityName());
        assertEquals(AuditAction.CREATE, response.getContent().get(0).getAction());
    }

    @Test
    @DisplayName("Get audit log by ID success")
    void testGetAuditLogByIdSuccess() {
        when(auditLogRepository.findById(100L)).thenReturn(Optional.of(sampleLog));

        AuditLogResponse response = auditLogService.getAuditLogById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("admin", response.getUsernameSnapshot());
    }

    @Test
    @DisplayName("Get audit log by non-existent ID throws ResourceNotFoundException")
    void testGetAuditLogByIdNotFound() {
        when(auditLogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditLogService.getAuditLogById(999L));
    }
}
