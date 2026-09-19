package com.company.eams.service.impl;

import com.company.eams.dto.request.AssetCategoryRequest;
import com.company.eams.dto.request.AssetCreateRequest;
import com.company.eams.dto.response.AssetCategoryResponse;
import com.company.eams.dto.response.AssetResponse;
import com.company.eams.entity.Asset;
import com.company.eams.entity.AssetCategory;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.AssetAssignmentRepository;
import com.company.eams.repository.AssetCategoryRepository;
import com.company.eams.repository.AssetRepository;
import com.company.eams.repository.EmployeeRepository;
import com.company.eams.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetCategoryRepository categoryRepository;

    @Mock
    private AssetAssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetServiceImpl assetService;

    private AssetCategory category;
    private Asset asset;

    @BeforeEach
    void setUp() {
        category = AssetCategory.builder()
                .id(1L)
                .name("Laptops")
                .build();

        asset = Asset.builder()
                .id(10L)
                .assetTag("LAP-001")
                .category(category)
                .modelName("MacBook Pro 14")
                .serialNumber("SN12345678")
                .status(AssetStatus.AVAILABLE)
                .purchaseDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Create asset category successfully")
    void testCreateCategorySuccess() {
        AssetCategoryRequest request = new AssetCategoryRequest();
        request.setName("Laptops");

        when(categoryRepository.existsByNameIgnoreCase("Laptops")).thenReturn(false);
        when(categoryRepository.save(any(AssetCategory.class))).thenReturn(category);

        AssetCategoryResponse response = assetService.createCategory(request);

        assertNotNull(response);
        assertEquals("Laptops", response.getName());
        assertEquals(1L, response.getId());
        verify(categoryRepository).save(any(AssetCategory.class));
    }

    @Test
    @DisplayName("Create asset category throws exception when duplicate name")
    void testCreateCategoryDuplicateThrowsException() {
        AssetCategoryRequest request = new AssetCategoryRequest();
        request.setName("Laptops");

        when(categoryRepository.existsByNameIgnoreCase("Laptops")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> assetService.createCategory(request));
        verify(categoryRepository, never()).save(any(AssetCategory.class));
    }

    @Test
    @DisplayName("Create asset successfully")
    void testCreateAssetSuccess() {
        AssetCreateRequest request = new AssetCreateRequest();
        request.setAssetTag("LAP-001");
        request.setCategoryId(1L);
        request.setModelName("MacBook Pro 14");
        request.setSerialNumber("SN12345678");
        request.setPurchaseDate(LocalDate.now());

        when(assetRepository.existsByAssetTagIgnoreCase("LAP-001")).thenReturn(false);
        when(assetRepository.existsBySerialNumberIgnoreCase("SN12345678")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);

        AssetResponse response = assetService.createAsset(request);

        assertNotNull(response);
        assertEquals("LAP-001", response.getAssetTag());
        assertEquals("MacBook Pro 14", response.getModelName());
        assertEquals(AssetStatus.AVAILABLE, response.getStatus());
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("Create asset throws exception when asset tag already exists")
    void testCreateAssetDuplicateTagThrowsException() {
        AssetCreateRequest request = new AssetCreateRequest();
        request.setAssetTag("LAP-001");

        when(assetRepository.existsByAssetTagIgnoreCase("LAP-001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> assetService.createAsset(request));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    @DisplayName("Get asset by ID returns AssetResponse")
    void testGetAssetByIdSuccess() {
        when(assetRepository.findById(10L)).thenReturn(Optional.of(asset));

        AssetResponse response = assetService.getAssetById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("LAP-001", response.getAssetTag());
    }

    @Test
    @DisplayName("Get asset by non-existent ID throws ResourceNotFoundException")
    void testGetAssetByInvalidIdThrowsNotFound() {
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> assetService.getAssetById(999L));
    }
}
