package com.company.eams.service.impl;

import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.entity.RefreshToken;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.exception.TokenRefreshException;
import com.company.eams.repository.RefreshTokenRepository;
import com.company.eams.repository.UserRepository;
import com.company.eams.security.JwtService;
import com.company.eams.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .id(1L)
                .name(RoleType.ADMIN)
                .permissions(Collections.emptySet())
                .build();

        testUser = User.builder()
                .id(10L)
                .username("admin_test")
                .email("admin@test.com")
                .passwordHash("$2a$12$dummyHashedPassword")
                .role(role)
                .isActive(true)
                .build();

        userPrincipal = UserPrincipal.builder()
                .id(10L)
                .username("admin_test")
                .email("admin@test.com")
                .role(RoleType.ADMIN)
                .permissions(Set.of("USER_CREATE"))
                .active(true)
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Login succeeds with valid credentials and returns AuthResponse")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("admin_test");
        request.setPassword("Password123!");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(userPrincipal)).thenReturn("mock.access.token");
        when(jwtService.generateRawRefreshToken()).thenReturn("mock-raw-refresh-token");
        when(jwtService.hashToken("mock-raw-refresh-token")).thenReturn("mock-hashed-refresh-token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtService.getCookieName()).thenReturn("eams_refresh_token");

        AuthResponse response = authService.login(request, httpServletResponse);

        assertNotNull(response);
        assertEquals("mock.access.token", response.getAccessToken());
        assertEquals("mock-raw-refresh-token", response.getRefreshToken());
        assertEquals("admin_test", response.getUser().getUsername());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login fails with BadCredentialsException when credentials invalid")
    void testLoginInvalidCredentialsThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("admin_test");
        request.setPassword("WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request, httpServletResponse));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Login fails with DisabledException when user account is deactivated")
    void testLoginDeactivatedUserThrowsDisabledException() {
        testUser.setIsActive(false);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("admin_test");
        request.setPassword("Password123!");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));

        assertThrows(DisabledException.class, () -> authService.login(request, httpServletResponse));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh token throws TokenRefreshException when empty token provided")
    void testRefreshTokenEmptyThrowsException() {
        assertThrows(TokenRefreshException.class, () -> authService.refreshToken("", httpServletResponse));
    }
}
