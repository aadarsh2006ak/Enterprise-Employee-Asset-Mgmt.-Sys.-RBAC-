package com.company.eams.controller;

import com.company.eams.dto.request.AssetCategoryRequest;
import com.company.eams.dto.response.AssetCategoryResponse;
import com.company.eams.dto.response.AssetResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import com.company.eams.service.AssetService;
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
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetService assetService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @WithMockUser(authorities = {"ASSET_READ"})
    @DisplayName("GET /api/v1/assets/categories returns 200 with categories")
    void testGetAllCategoriesSuccess() throws Exception {
        AssetCategoryResponse cat = AssetCategoryResponse.builder()
                .id(1L)
                .name("Laptops")
                .assetCount(5L)
                .build();

        when(assetService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/v1/assets/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Laptops"));
    }

    @Test
    @WithMockUser(authorities = {"ASSET_CREATE"})
    @DisplayName("POST /api/v1/assets/categories returns 201 when created")
    void testCreateCategorySuccess() throws Exception {
        AssetCategoryRequest request = new AssetCategoryRequest();
        request.setName("Monitors");

        AssetCategoryResponse response = AssetCategoryResponse.builder()
                .id(2L)
                .name("Monitors")
                .assetCount(0L)
                .build();

        when(assetService.createCategory(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/assets/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Monitors"));
    }

    @Test
    @WithMockUser(authorities = {"ASSET_READ"})
    @DisplayName("GET /api/v1/assets returns 200 with paginated assets")
    void testGetAllAssetsSuccess() throws Exception {
        AssetResponse asset = AssetResponse.builder()
                .id(10L)
                .assetTag("AST-001")
                .status(AssetStatus.AVAILABLE)
                .build();

        Page<AssetResponse> page = new PageImpl<>(List.of(asset));
        when(assetService.getAllAssets(any(Pageable.class), any(), any(), any()))
                .thenReturn(PageResponse.from(page));

        mockMvc.perform(get("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].assetTag").value("AST-001"));
    }
}
