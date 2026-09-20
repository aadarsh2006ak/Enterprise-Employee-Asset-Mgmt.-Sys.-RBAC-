package com.company.eams.controller;

import com.company.eams.dto.response.PageResponse;
import com.company.eams.dto.response.UserResponseDto;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import com.company.eams.service.UserService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("GET /api/v1/admin/users returns 200 with paginated user list for Admin")
    void testGetAllUsersAdminSuccess() throws Exception {
        UserResponseDto dto = UserResponseDto.builder()
                .id(1L)
                .username("admin")
                .role(RoleType.ADMIN)
                .isActive(true)
                .build();

        Page<UserResponseDto> page = new PageImpl<>(List.of(dto));
        when(userService.getAllUsers(any(Pageable.class), any())).thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"));
    }

    @Test
    @WithMockUser(roles = {"EMPLOYEE"})
    @DisplayName("GET /api/v1/admin/users returns 403 Forbidden for non-admin")
    void testGetAllUsersForbiddenForEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
