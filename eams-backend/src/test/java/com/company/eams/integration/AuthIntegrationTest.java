package com.company.eams.integration;

import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.repository.RoleRepository;
import com.company.eams.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByUsernameIgnoreCase("test_admin")) {
            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ADMIN).description("Admin").build()));

            userRepository.save(User.builder()
                    .username("test_admin")
                    .email("test_admin@company.com")
                    .passwordHash(passwordEncoder.encode("AdminPassword@123"))
                    .role(adminRole)
                    .isActive(true)
                    .build());
        }
    }

    @Test
    @DisplayName("End-to-End Auth: Login -> Receive JWT -> Rotate Token -> Logout")
    void testAuthLifecycle() {
        // 1. Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("test_admin");
        loginRequest.setPassword("AdminPassword@123");

        ResponseEntity<ApiResponse<AuthResponse>> loginResp = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertNotNull(loginResp.getBody());
        assertTrue(loginResp.getBody().isSuccess());

        AuthResponse authData = loginResp.getBody().getData();
        assertNotNull(authData.getAccessToken());
        assertNotNull(authData.getRefreshToken());

        // 2. Call /api/v1/auth/me with the Access Token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authData.getAccessToken());

        ResponseEntity<String> meResp = restTemplate.exchange(
                "/api/v1/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.OK, meResp.getStatusCode());
        assertTrue(meResp.getBody().contains("test_admin"));
    }
}
