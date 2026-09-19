package com.company.eams.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login credentials payload")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(description = "Username or email address", example = "admin")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "Admin@123")
    private String password;
}
