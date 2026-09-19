package com.company.eams.dto.request;

import com.company.eams.entity.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin payload to create user account")
public class UserCreateAdminRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    @Schema(description = "Unique Username", example = "jsmith")
    private String username;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Corporate Email Address", example = "jsmith@company.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Account initial password", example = "Password@123")
    private String password;

    @NotNull(message = "Assigned role is required")
    @Schema(description = "Assigned RBAC role", example = "EMPLOYEE")
    private RoleType role;
}
