package com.company.eams.dto.response;

import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User account administrative representation")
public class UserResponseDto {

    @Schema(description = "Unique User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "jsmith")
    private String username;

    @Schema(description = "Email address", example = "jsmith@company.com")
    private String email;

    @Schema(description = "Assigned Role", example = "EMPLOYEE")
    private RoleType role;

    @Schema(description = "Account Active State", example = "true")
    private Boolean isActive;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Updated timestamp")
    private Instant updatedAt;

    @Schema(description = "Optimistic locking version", example = "0")
    private Integer version;
}
