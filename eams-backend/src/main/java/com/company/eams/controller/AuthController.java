package com.company.eams.controller;

import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.request.RefreshTokenRequest;
import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.dto.response.UserSummaryDto;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & RBAC", description = "Endpoints for user authentication, token rotation, session management, and current user profile")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates with username/email and password, returning JWT access token and refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Authentication successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token", description = "Exchange a valid refresh token for a new access token and rotated refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "eams_refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        String token = (request != null && StringUtils.hasText(request.getRefreshToken()))
                ? request.getRefreshToken()
                : cookieRefreshToken;

        AuthResponse authResponse = authService.refreshToken(token, response);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes refresh token and clears session authentication cookies")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = "eams_refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {

        String token = (request != null && StringUtils.hasText(request.getRefreshToken()))
                ? request.getRefreshToken()
                : cookieRefreshToken;

        authService.logout(token, response);
        return ResponseEntity.ok(ApiResponse.message("User logged out successfully"));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get current authenticated profile", description = "Returns metadata, assigned role, and permissions for the currently authenticated user")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserSummaryDto userSummary = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(userSummary, "Current user profile retrieved"));
    }
}
