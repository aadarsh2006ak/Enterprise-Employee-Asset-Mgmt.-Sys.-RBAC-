package com.company.eams.dto.response;

import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authenticated user summary profile")
public class UserSummaryDto {

    @Schema(description = "Unique User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "admin")
    private String username;

    @Schema(description = "Email address", example = "admin@company.com")
    private String email;

    @Schema(description = "Assigned Role", example = "ADMIN")
    private RoleType role;

    @Schema(description = "Granular authorities and permissions", example = "[\"USER_CREATE\", \"ASSET_ASSIGN\"]")
    private Set<String> permissions;

    @Schema(description = "Account active state", example = "true")
    private Boolean isActive;
}
