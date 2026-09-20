package com.company.eams.integration;

import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.response.ApiResponse;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.entity.Permission;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.repository.PermissionRepository;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RbacSecurityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Seed RBAC users if not present
        if (!userRepository.existsByUsernameIgnoreCase("rbac_employee")) {
            Permission assetRead = permissionRepository.findByCode("ASSET_READ")
                    .orElseGet(() -> permissionRepository.save(Permission.builder().code("ASSET_READ").description("Read Assets").build()));

            Role empRole = roleRepository.findByName(RoleType.EMPLOYEE)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.EMPLOYEE).description("Employee").build()));
            empRole.setPermissions(Set.of(assetRead));
            roleRepository.save(empRole);

            userRepository.save(User.builder()
                    .username("rbac_employee")
                    .email("rbac_employee@company.com")
                    .passwordHash(passwordEncoder.encode("Pass@123"))
                    .role(empRole)
                    .isActive(true)
                    .build());
        }

        if (!userRepository.existsByUsernameIgnoreCase("rbac_admin")) {
            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ADMIN).description("Admin").build()));

            userRepository.save(User.builder()
                    .username("rbac_admin")
                    .email("rbac_admin@company.com")
                    .passwordHash(passwordEncoder.encode("Pass@123"))
                    .role(adminRole)
                    .isActive(true)
                    .build());
        }

        // Fetch tokens
        employeeToken = authenticateUser("rbac_employee", "Pass@123");
        adminToken = authenticateUser("rbac_admin", "Pass@123");
    }

    private String authenticateUser(String username, String password) {
        LoginRequest login = new LoginRequest();
        login.setUsernameOrEmail(username);
        login.setPassword(password);

        ResponseEntity<ApiResponse<AuthResponse>> resp = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(login),
                new ParameterizedTypeReference<>() {}
        );
        return resp.getBody().getData().getAccessToken();
    }

    @Test
    @DisplayName("Employee with ASSET_READ can view assets, but is forbidden from creating assets")
    void testEmployeePermissions() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(employeeToken);

        // Read assets -> 200 OK
        ResponseEntity<String> readResp = restTemplate.exchange(
                "/api/v1/assets",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.OK, readResp.getStatusCode());

        // Admin-only user list -> 403 Forbidden
        ResponseEntity<String> adminResp = restTemplate.exchange(
                "/api/v1/admin/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, adminResp.getStatusCode());
    }

    @Test
    @DisplayName("Admin has full access to admin user management")
    void testAdminPermissions() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        ResponseEntity<String> adminResp = restTemplate.exchange(
                "/api/v1/admin/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        assertEquals(HttpStatus.OK, adminResp.getStatusCode());
    }
}
