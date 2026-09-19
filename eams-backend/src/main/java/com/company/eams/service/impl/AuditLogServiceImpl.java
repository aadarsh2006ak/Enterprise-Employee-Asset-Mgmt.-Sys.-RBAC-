package com.company.eams.service.impl;

import com.company.eams.dto.response.AuditLogResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.AuditLog;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.AuditLogRepository;
import com.company.eams.repository.specification.AuditLogSpecification;
import com.company.eams.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAllAuditLogs(
            Pageable pageable,
            String entityName,
            Long entityId,
            AuditAction action,
            String username,
            Long userId,
            Instant startDate,
            Instant endDate) {

        Specification<AuditLog> spec = AuditLogSpecification.filter(
                entityName, entityId, action, username, userId, startDate, endDate);

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        Page<AuditLogResponse> dtoPage = page.map(this::mapToResponse);

        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", "id", id));
        return mapToResponse(auditLog);
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .usernameSnapshot(log.getUsernameSnapshot())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
