package com.company.eams.service;

import com.company.eams.dto.response.AuditLogResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.AuditAction;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditLogService {

    PageResponse<AuditLogResponse> getAllAuditLogs(
            Pageable pageable,
            String entityName,
            Long entityId,
            AuditAction action,
            String username,
            Long userId,
            Instant startDate,
            Instant endDate);

    AuditLogResponse getAuditLogById(Long id);
}
