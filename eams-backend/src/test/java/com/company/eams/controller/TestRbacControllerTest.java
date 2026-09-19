package com.company.eams.controller;

import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestRbacControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Public endpoint is accessible without authentication (200 OK)")
    void testPublicEndpointPermitAll() throws Exception {
        mockMvc.perform(get("/api/v1/test/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access").value("PUBLIC"));
    }

    @Test
    @DisplayName("Admin endpoint returns 401 Unauthorized for unauthenticated request")
    void testAdminEndpointUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    @DisplayName("Admin endpoint returns 200 OK for user with ROLE_ADMIN")
    void testAdminEndpointAuthorized() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "emp_user", roles = {"EMPLOYEE"})
    @DisplayName("Admin endpoint returns 403 Forbidden for user with ROLE_EMPLOYEE")
    void testAdminEndpointForbiddenForEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager_user", roles = {"MANAGER"})
    @DisplayName("Manager endpoint returns 200 OK for user with ROLE_MANAGER")
    void testManagerEndpointAuthorizedForManager() throws Exception {
        mockMvc.perform(get("/api/v1/test/manager")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN_OR_MANAGER"));
    }

    @Test
    @WithMockUser(username = "emp_user", roles = {"EMPLOYEE"})
    @DisplayName("Manager endpoint returns 403 Forbidden for standard employee")
    void testManagerEndpointForbiddenForEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/test/manager")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "custom_user", authorities = {"USER_CREATE"})
    @DisplayName("Granular permission USER_CREATE allows access to user-create test endpoint")
    void testGranularPermissionUserCreateGranted() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/user-create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.permission").value("USER_CREATE"));
    }

    @Test
    @WithMockUser(username = "custom_user", authorities = {"ASSET_READ"})
    @DisplayName("Granular permission endpoint returns 403 Forbidden when authority is missing")
    void testGranularPermissionUserCreateForbiddenWithoutAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/user-create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
