package com.company.eams.audit.service;

import com.company.eams.audit.dto.AuditLogEvent;
import com.company.eams.entity.AuditLog;
import com.company.eams.entity.User;
import com.company.eams.repository.AuditLogRepository;
import com.company.eams.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAuditWriter {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Non-blocking asynchronous audit log writer dispatched to bounded 'auditLogExecutor' thread pool.
     * Uses REQUIRES_NEW propagation so audit logging has its own isolated transaction lifecycle.
     */
    @Async("auditLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuditLog(AuditLogEvent event) {
        try {
            log.debug("Async persisting audit log: action={} entity={} id={}",
                    event.getAction(), event.getEntityName(), event.getEntityId());

            User user = null;
            if (event.getUserId() != null) {
                user = userRepository.findById(event.getUserId()).orElse(null);
            }

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .usernameSnapshot(event.getUsernameSnapshot())
                    .entityName(event.getEntityName())
                    .entityId(event.getEntityId())
                    .action(event.getAction())
                    .oldValue(event.getOldValue())
                    .newValue(event.getNewValue())
                    .ipAddress(event.getIpAddress())
                    .createdAt(event.getCreatedAt() != null ? event.getCreatedAt() : Instant.now())
                    .build();

            AuditLog saved = auditLogRepository.save(auditLog);
            log.info("Audit log #{} recorded for {} on {}/{}",
                    saved.getId(), event.getAction(), event.getEntityName(), event.getEntityId());

        } catch (Exception ex) {
            log.error("Failed to write asynchronous audit log for action={} entity={}: {}",
                    event.getAction(), event.getEntityName(), ex.getMessage(), ex);
        }
    }
}
