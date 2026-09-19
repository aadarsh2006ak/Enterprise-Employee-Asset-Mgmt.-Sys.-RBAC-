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
@Schema(description = "Request payload to verify registered company username or email for password reset")
public class ForgotPasswordVerifyRequest {

    @NotBlank(message = "Username or company email address is required")
    @Schema(description = "Registered username or company email address", example = "admin or employee@company.com")
    private String usernameOrEmail;
}
