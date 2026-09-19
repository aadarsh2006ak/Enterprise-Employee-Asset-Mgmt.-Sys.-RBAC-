package com.company.eams.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Verification response containing reset session token and masked account preview")
public class ForgotPasswordVerifyResponse {

    @Schema(description = "Secure short-lived reset token", example = "4f8a9e2c-3b1d-4e5f-a6b7-8c9d0e1f2a3b")
    private String resetToken;

    @Schema(description = "Registered username", example = "admin")
    private String username;

    @Schema(description = "Masked registered email for identity confirmation", example = "a***n@company.com")
    private String maskedEmail;

    @Schema(description = "Token expiration in seconds", example = "900")
    private long expiresIn;

    @Schema(description = "Informative status message", example = "Account verified. Please enter your new password.")
    private String message;
}
