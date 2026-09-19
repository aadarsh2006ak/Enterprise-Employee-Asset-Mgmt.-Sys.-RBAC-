package com.company.eams.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response structure")
public class ApiErrorResponse {

    @Builder.Default
    @Schema(description = "Indicates failure", example = "false")
    private boolean success = false;

    @Schema(description = "HTTP status code", example = "401")
    private int status;

    @Schema(description = "Error title / reason phrase", example = "Unauthorized")
    private String error;

    @Schema(description = "Detailed error message", example = "Bad credentials")
    private String message;

    @Schema(description = "Target path of the failed request", example = "/api/v1/auth/login")
    private String path;

    @Schema(description = "Field validation errors map (if applicable)")
    private Map<String, String> fieldErrors;

    @Schema(description = "General error messages list")
    private List<String> details;

    @Builder.Default
    @Schema(description = "Timestamp when the error occurred", example = "2026-09-18T07:45:00Z")
    private Instant timestamp = Instant.now();
}
