package com.company.eams.controller;

import com.company.eams.dto.request.EmployeeCreateRequest;
import com.company.eams.dto.response.EmployeeResponse;
import com.company.eams.dto.response.EmployeeSummaryResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import com.company.eams.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @WithMockUser(authorities = {"EMPLOYEE_READ"})
    @DisplayName("GET /api/v1/employees returns 200 with paginated employee list")
    void testGetAllEmployeesSuccess() throws Exception {
        EmployeeSummaryResponse summary = EmployeeSummaryResponse.builder()
                .id(1L)
                .employeeCode("EMP-001")
                .fullName("John Doe")
                .status(EmployeeStatus.ACTIVE)
                .build();

        Page<EmployeeSummaryResponse> page = new PageImpl<>(List.of(summary));
        when(employeeService.getAllEmployees(any(Pageable.class), any(), any(), any()))
                .thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].employeeCode").value("EMP-001"));
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE_CREATE"})
    @DisplayName("POST /api/v1/employees returns 201 when created")
    void testCreateEmployeeSuccess() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmployeeCode("EMP-002");
        request.setFullName("Jane Smith");
        request.setUsername("janesmith");
        request.setEmail("jane.smith@company.com");
        request.setRole(RoleType.EMPLOYEE);
        request.setStatus(EmployeeStatus.ACTIVE);
        request.setDateOfJoining(LocalDate.now());

        EmployeeResponse response = EmployeeResponse.builder()
                .id(2L)
                .employeeCode("EMP-002")
                .fullName("Jane Smith")
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeService.createEmployee(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeCode").value("EMP-002"));
    }

    @Test
    @WithMockUser(authorities = {"ASSET_READ"})
    @DisplayName("GET /api/v1/employees returns 403 Forbidden without EMPLOYEE_READ authority")
    void testGetAllEmployeesForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
