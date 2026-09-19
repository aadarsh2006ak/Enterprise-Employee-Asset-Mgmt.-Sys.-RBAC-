package com.company.eams.dto.response;

import com.company.eams.entity.enums.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Immutable audit log entry with JSONB state snapshots")
public class AuditLogResponse {

    @Schema(description = "Audit Log ID", example = "1001")
    private Long id;

    @Schema(description = "User ID who performed the operation", example = "1")
    private Long userId;

    @Schema(description = "Username snapshot captured at operation time", example = "admin")
    private String usernameSnapshot;

    @Schema(description = "Target domain entity name", example = "Asset")
    private String entityName;

    @Schema(description = "Target entity primary key identifier", example = "42")
    private Long entityId;

    @Schema(description = "Action performed", example = "ASSIGN")
    private AuditAction action;

    @Schema(description = "Pre-execution state or input arguments snapshot (JSONB)")
    private Map<String, Object> oldValue;

    @Schema(description = "Post-execution state or output payload snapshot (JSONB)")
    private Map<String, Object> newValue;

    @Schema(description = "Originating IP address", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Timestamp when the audit log was recorded in UTC", example = "2026-09-18T10:15:30Z")
    private Instant createdAt;
}
