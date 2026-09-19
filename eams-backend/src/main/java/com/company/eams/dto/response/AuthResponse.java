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
@Schema(description = "Authentication token response")
public class AuthResponse {

    @Schema(description = "JWT Access Token for Bearer authorization", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token string (also returned in HttpOnly cookie)", example = "4e38e1b6-d245-42f1-a1e6-b6fb1a63c637")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Authorization scheme type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifespan in seconds", example = "900")
    private Long expiresIn;

    @Schema(description = "Authenticated user profile details")
    private UserSummaryDto user;
}
