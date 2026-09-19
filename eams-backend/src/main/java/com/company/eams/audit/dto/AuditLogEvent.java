package com.company.eams.audit.dto;

import com.company.eams.entity.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent {

    private Long userId;
    private String usernameSnapshot;
    private String entityName;
    private Long entityId;
    private AuditAction action;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private String ipAddress;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
