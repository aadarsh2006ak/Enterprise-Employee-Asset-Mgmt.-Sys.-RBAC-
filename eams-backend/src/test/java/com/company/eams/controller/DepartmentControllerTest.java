package com.company.eams.controller;

import com.company.eams.dto.request.DepartmentCreateRequest;
import com.company.eams.dto.response.DepartmentResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import com.company.eams.service.DepartmentService;
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
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("GET /api/v1/departments/all returns 200 with list of departments")
    void testGetAllDepartmentsListSuccess() throws Exception {
        DepartmentResponse dept = DepartmentResponse.builder()
                .id(1L)
                .name("Engineering")
                .employeeCount(15L)
                .build();

        when(departmentService.getAllDepartmentsList()).thenReturn(List.of(dept));

        mockMvc.perform(get("/api/v1/departments/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Engineering"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("GET /api/v1/departments returns 200 with paginated departments")
    void testGetAllDepartmentsPaginatedSuccess() throws Exception {
        DepartmentResponse dept = DepartmentResponse.builder()
                .id(1L)
                .name("Engineering")
                .employeeCount(15L)
                .build();

        Page<DepartmentResponse> page = new PageImpl<>(List.of(dept));
        when(departmentService.getAllDepartments(any(Pageable.class), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Engineering"));
    }

    @Test
    @WithMockUser(authorities = {"DEPARTMENT_MANAGE"})
    @DisplayName("POST /api/v1/departments returns 201 when created")
    void testCreateDepartmentSuccess() throws Exception {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setName("Finance");

        DepartmentResponse response = DepartmentResponse.builder()
                .id(2L)
                .name("Finance")
                .employeeCount(0L)
                .build();

        when(departmentService.createDepartment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Finance"));
    }
}
