package com.company.eams.controller;

import com.company.eams.dto.response.AuditLogResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import com.company.eams.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(authorities = {"AUDIT_READ"})
    @DisplayName("GET /api/v1/audit-logs returns 200 with paginated audit logs")
    void testGetAllAuditLogsSuccess() throws Exception {
        AuditLogResponse logResponse = AuditLogResponse.builder()
                .id(1L)
                .usernameSnapshot("admin")
                .entityName("Asset")
                .action(AuditAction.CREATE)
                .createdAt(Instant.now())
                .build();

        Page<AuditLogResponse> page = new PageImpl<>(List.of(logResponse));
        when(auditLogService.getAllAuditLogs(any(Pageable.class), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].entityName").value("Asset"));
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE_READ"})
    @DisplayName("GET /api/v1/audit-logs returns 403 Forbidden without AUDIT_READ authority")
    void testGetAllAuditLogsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
