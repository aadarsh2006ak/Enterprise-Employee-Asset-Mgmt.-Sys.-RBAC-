package com.company.eams.dto.request;

import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin payload to update user role")
public class UserRoleUpdateRequest {

    @NotNull(message = "New role is required")
    @Schema(description = "Updated role enum", example = "MANAGER")
    private RoleType role;
}
