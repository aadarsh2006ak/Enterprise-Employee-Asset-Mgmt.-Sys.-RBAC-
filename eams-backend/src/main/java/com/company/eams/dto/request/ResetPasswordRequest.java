package com.company.eams.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to update account password using a verified reset token")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Reset session token issued in verification step", example = "4f8a9e2c-3b1d-4e5f-a6b7-8c9d0e1f2a3b")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-]).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "New password meeting enterprise complexity requirements", example = "NewPass@2026")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Password confirmation matching newPassword", example = "NewPass@2026")
    private String confirmPassword;
}
