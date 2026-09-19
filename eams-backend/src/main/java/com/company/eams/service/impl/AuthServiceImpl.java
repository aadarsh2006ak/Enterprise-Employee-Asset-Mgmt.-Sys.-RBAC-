package com.company.eams.service.impl;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.dto.request.ForgotPasswordVerifyRequest;
import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.request.ResetPasswordRequest;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.dto.response.ForgotPasswordVerifyResponse;
import com.company.eams.dto.response.UserSummaryDto;
import com.company.eams.entity.RefreshToken;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.exception.TokenRefreshException;
import com.company.eams.repository.RefreshTokenRepository;
import com.company.eams.repository.UserRepository;
import com.company.eams.security.JwtService;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String RESET_TOKEN_PREFIX = "eams:pwd_reset:";
    private static final long RESET_TOKEN_TTL_SECONDS = 900; // 15 minutes
    private static final Map<String, ResetTokenEntry> IN_MEMORY_RESET_TOKENS = new ConcurrentHashMap<>();

    private record ResetTokenEntry(String username, Instant expiresAt) {}


    @Override
    @Transactional
    @Auditable(action = AuditAction.LOGIN, entityName = "User", description = "User logged in")
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        log.info("Attempting authentication for user: {}", request.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("User account is deactivated.");
        }

        // 1. Generate JWT Access Token
        String accessToken = jwtService.generateAccessToken(userPrincipal);

        // 2. Generate and Store Hashed Refresh Token
        String rawRefreshToken = jwtService.generateRawRefreshToken();
        String hashedToken = jwtService.hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashedToken)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 3. Set HttpOnly Cookie for security
        setRefreshTokenCookie(response, rawRefreshToken, jwtService.getRefreshTokenExpirationMs() / 1000);

        log.info("User {} successfully authenticated", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .user(mapToUserSummaryDto(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken, HttpServletResponse response) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            throw new TokenRefreshException("Refresh token is required.");
        }

        String hashedToken = jwtService.hashToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new TokenRefreshException(rawRefreshToken, "Refresh token not found in database records."));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new TokenRefreshException(rawRefreshToken, "Refresh token has been revoked. Please sign in again.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(rawRefreshToken, "Refresh token has expired. Please sign in again.");
        }

        User user = refreshToken.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("User account is deactivated.");
        }

        // Token Rotation: Revoke previous token and issue a fresh one
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newRawRefreshToken = jwtService.generateRawRefreshToken();
        String newHashedToken = jwtService.hashToken(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(newHashedToken)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(newRefreshToken);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = jwtService.generateAccessToken(userPrincipal);

        setRefreshTokenCookie(response, newRawRefreshToken, jwtService.getRefreshTokenExpirationMs() / 1000);

        log.info("Rotated refresh token for user: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs() / 1000)
                .user(mapToUserSummaryDto(user))
                .build();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.LOGOUT, entityName = "User", description = "User logged out")
    public void logout(String rawRefreshToken, HttpServletResponse response) {
        if (StringUtils.hasText(rawRefreshToken)) {
            String hashedToken = jwtService.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(hashedToken).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token for user: {}", token.getUser().getUsername());
            });
        }

        clearRefreshTokenCookie(response);
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "userProfile", key = "#userPrincipal.username")
    public UserSummaryDto getCurrentUser(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new ResourceNotFoundException("No authenticated principal found.");
        }

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return mapToUserSummaryDto(user);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAgeSeconds) {
        if (response == null) return;
        ResponseCookie cookie = ResponseCookie.from(jwtService.getCookieName(), token)
                .httpOnly(true)
                .secure(false) // Set to true in production behind HTTPS
                .path("/api/v1/auth")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        if (response == null) return;
        ResponseCookie cookie = ResponseCookie.from(jwtService.getCookieName(), "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private UserSummaryDto mapToUserSummaryDto(User user) {
        Set<String> permissions = null;
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            permissions = user.getRole().getPermissions().stream()
                    .map(p -> p.getCode())
                    .collect(Collectors.toSet());
        }

        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .permissions(permissions)
                .isActive(user.getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ForgotPasswordVerifyResponse verifyForPasswordReset(ForgotPasswordVerifyRequest request) {
        String identifier = request.getUsernameOrEmail() != null ? request.getUsernameOrEmail().trim() : "";
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException("Username or company email is required.");
        }

        log.info("Verifying user account for password reset: {}", identifier);

        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findByUsernameOrEmail(identifier, identifier))
                .orElseThrow(() -> new ResourceNotFoundException("No company account found matching: " + identifier));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("This employee account has been deactivated. Please contact company HR or Administrator.");
        }

        String resetToken = UUID.randomUUID().toString();
        String redisKey = RESET_TOKEN_PREFIX + resetToken;

        try {
            if (stringRedisTemplate != null) {
                stringRedisTemplate.opsForValue().set(redisKey, user.getUsername(), Duration.ofSeconds(RESET_TOKEN_TTL_SECONDS));
            }
        } catch (Exception ex) {
            log.warn("Redis unavailable for password reset token, using in-memory store: {}", ex.getMessage());
        }

        IN_MEMORY_RESET_TOKENS.put(resetToken, new ResetTokenEntry(user.getUsername(), Instant.now().plusSeconds(RESET_TOKEN_TTL_SECONDS)));

        String maskedEmail = maskEmail(user.getEmail());

        log.info("Password reset token generated for user: {}", user.getUsername());

        return ForgotPasswordVerifyResponse.builder()
                .resetToken(resetToken)
                .username(user.getUsername())
                .maskedEmail(maskedEmail)
                .expiresIn(RESET_TOKEN_TTL_SECONDS)
                .message("Account verified successfully. Please enter your new password.")
                .build();
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "User", description = "Password reset successfully")
    public void resetPassword(ResetPasswordRequest request) {
        if (!StringUtils.hasText(request.getResetToken())) {
            throw new IllegalArgumentException("Reset token is required.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        String resetToken = request.getResetToken().trim();
        String username = null;

        // 1. Check Redis
        try {
            if (stringRedisTemplate != null) {
                username = stringRedisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + resetToken);
            }
        } catch (Exception ex) {
            log.warn("Redis lookup failed during password reset: {}", ex.getMessage());
        }

        // 2. Check in-memory fallback
        if (!StringUtils.hasText(username)) {
            ResetTokenEntry entry = IN_MEMORY_RESET_TOKENS.get(resetToken);
            if (entry != null) {
                if (entry.expiresAt().isAfter(Instant.now())) {
                    username = entry.username();
                } else {
                    IN_MEMORY_RESET_TOKENS.remove(resetToken);
                }
            }
        }

        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Password reset session has expired or is invalid. Please verify your account again.");
        }

        final String targetUsername = username;
        User user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", targetUsername));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("Account is deactivated. Cannot reset password.");
        }

        // 3. Update password hash
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 4. Revoke all existing refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // 5. Invalidate the reset token
        try {
            if (stringRedisTemplate != null) {
                stringRedisTemplate.delete(RESET_TOKEN_PREFIX + resetToken);
            }
        } catch (Exception ignored) {}
        IN_MEMORY_RESET_TOKENS.remove(resetToken);

        log.info("Password successfully reset for user: {}", targetUsername);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "******";
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }
}

