package com.company.eams.controller;

import com.company.eams.dto.request.ExportJobRequest;
import com.company.eams.dto.response.ExportJobResponse;
import com.company.eams.entity.enums.ExportFormat;
import com.company.eams.entity.enums.ExportJobStatus;
import com.company.eams.entity.enums.ExportType;
import com.company.eams.security.CustomUserDetailsService;
import com.company.eams.security.JwtService;
import com.company.eams.service.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @WithMockUser(authorities = {"EXPORT_DATA"})
    @DisplayName("POST /api/v1/exports/jobs returns 202 Accepted when creating job")
    void testCreateExportJobSuccess() throws Exception {
        ExportJobRequest request = new ExportJobRequest();
        request.setExportType(ExportType.ASSETS);
        request.setFormat(ExportFormat.CSV);

        ExportJobResponse response = ExportJobResponse.builder()
                .jobUuid("job-uuid-123")
                .exportType(ExportType.ASSETS)
                .format(ExportFormat.CSV)
                .status(ExportJobStatus.PENDING)
                .build();

        when(exportService.createExportJob(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/exports/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobUuid").value("job-uuid-123"));
    }

    @Test
    @WithMockUser(authorities = {"EXPORT_DATA"})
    @DisplayName("GET /api/v1/exports/jobs/{jobUuid} returns 200 with job status")
    void testGetExportJobStatusSuccess() throws Exception {
        ExportJobResponse response = ExportJobResponse.builder()
                .jobUuid("job-uuid-123")
                .status(ExportJobStatus.COMPLETED)
                .progressPercentage(100)
                .build();

        when(exportService.getExportJobStatus(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/exports/jobs/job-uuid-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
