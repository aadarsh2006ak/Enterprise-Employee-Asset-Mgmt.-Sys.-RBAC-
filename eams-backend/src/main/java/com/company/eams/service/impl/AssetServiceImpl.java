package com.company.eams.service.impl;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.dto.request.*;
import com.company.eams.dto.response.*;
import com.company.eams.entity.*;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.*;
import com.company.eams.repository.specification.AssetSpecification;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    // =========================================================================
    // Category Management
    // =========================================================================

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public AssetCategoryResponse createCategory(AssetCategoryRequest request) {
        String trimmedName = request.getName().trim();
        log.info("Creating asset category: {}", trimmedName);

        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Category with name '" + trimmedName + "' already exists");
        }

        AssetCategory category = AssetCategory.builder()
                .name(trimmedName)
                .build();

        AssetCategory saved = categoryRepository.save(category);
        return AssetCategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .assetCount(0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<AssetCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(cat -> AssetCategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .assetCount(assetRepository.countByCategoryId(cat.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Asset CRUD
    // =========================================================================

    @Override
    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categories"}, allEntries = true)
    @Auditable(action = AuditAction.CREATE, entityName = "Asset", description = "Registered new asset in inventory")
    public AssetResponse createAsset(AssetCreateRequest request) {
        String tag = request.getAssetTag().trim();
        log.info("Registering asset with tag: {}", tag);

        if (assetRepository.existsByAssetTagIgnoreCase(tag)) {
            throw new IllegalArgumentException("Asset tag '" + tag + "' is already in use");
        }

        if (StringUtils.hasText(request.getSerialNumber()) &&
                assetRepository.existsBySerialNumberIgnoreCase(request.getSerialNumber().trim())) {
            throw new IllegalArgumentException("Serial number '" + request.getSerialNumber().trim() + "' is already registered");
        }

        AssetCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("AssetCategory", "id", request.getCategoryId()));

        Asset asset = Asset.builder()
                .assetTag(tag)
                .category(category)
                .modelName(request.getModelName())
                .serialNumber(StringUtils.hasText(request.getSerialNumber()) ? request.getSerialNumber().trim() : null)
                .purchaseDate(request.getPurchaseDate())
                .status(request.getStatus() != null ? request.getStatus() : AssetStatus.AVAILABLE)
                .build();

        Asset saved = assetRepository.save(asset);
        log.info("Registered asset ID: {} tag: {}", saved.getId(), saved.getAssetTag());

        return mapToAssetResponse(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AssetResponse> getAllAssets(Pageable pageable, Long categoryId, AssetStatus status, String search) {
        log.debug("Fetching paginated assets: categoryId={}, status={}, search={}", categoryId, status, search);

        Specification<Asset> spec = AssetSpecification.filter(categoryId, status, search);
        Page<Asset> page = assetRepository.findAll(spec, pageable);

        Page<AssetResponse> responsePage = page.map(asset -> {
            Optional<AssetAssignment> activeAssignment = assignmentRepository.findActiveAssignmentByAssetId(asset.getId());
            return mapToAssetResponse(asset, activeAssignment.orElse(null));
        });

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        Optional<AssetAssignment> activeAssignment = assignmentRepository.findActiveAssignmentByAssetId(id);
        return mapToAssetResponse(asset, activeAssignment.orElse(null));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categories"}, allEntries = true)
    @Auditable(action = AuditAction.UPDATE, entityName = "Asset", description = "Updated asset metadata")
    public AssetResponse updateAsset(Long id, AssetUpdateRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        // Optimistic locking token validation if provided by client
        if (request.getVersion() != null && !request.getVersion().equals(asset.getVersion())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Asset.class, id);
        }

        if (StringUtils.hasText(request.getSerialNumber()) &&
                assetRepository.existsBySerialNumberIgnoreCaseAndIdNot(request.getSerialNumber().trim(), id)) {
            throw new IllegalArgumentException("Serial number '" + request.getSerialNumber().trim() + "' is already registered to another asset");
        }

        AssetCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("AssetCategory", "id", request.getCategoryId()));

        asset.setCategory(category);
        asset.setModelName(request.getModelName());
        asset.setSerialNumber(StringUtils.hasText(request.getSerialNumber()) ? request.getSerialNumber().trim() : null);
        asset.setPurchaseDate(request.getPurchaseDate());

        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }

        Asset updated = assetRepository.save(asset);
        Optional<AssetAssignment> activeAssignment = assignmentRepository.findActiveAssignmentByAssetId(id);
        log.info("Updated asset metadata for ID: {}", id);

        return mapToAssetResponse(updated, activeAssignment.orElse(null));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboardSummary", "categories"}, allEntries = true)
    @Auditable(action = AuditAction.DELETE, entityName = "Asset", description = "Deleted asset from inventory")
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        if (asset.getStatus() == AssetStatus.ASSIGNED || assignmentRepository.isAssetCurrentlyAssigned(id)) {
            throw new IllegalStateException("Cannot delete asset '" + asset.getAssetTag() + "' because it is currently assigned to an employee.");
        }

        assetRepository.delete(asset);
        log.info("Deleted asset ID: {}", id);
    }

    // =========================================================================
    // Concurrency Assignment & Return Workflows (Pessimistic Locking)
    // =========================================================================

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", allEntries = true)
    @Auditable(action = AuditAction.ASSIGN, entityName = "AssetAssignment", description = "Assigned asset to employee")
    public AssetAssignmentResponse assignAsset(Long assetId, AssetAssignRequest request, UserPrincipal currentUser) {
        log.info("Starting pessimistic lock acquisition for assigning asset ID: {} to employee ID: {}", assetId, request.getEmployeeId());

        // 1. Acquire exclusive row-level write lock (SELECT ... FOR UPDATE)
        Asset asset = assetRepository.findByIdWithLock(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", assetId));

        // 2. Concurrency check: Verify asset availability
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new IllegalStateException("Asset '" + asset.getAssetTag() + "' cannot be assigned because its status is " + asset.getStatus());
        }

        // 3. Double-assignment prevention check
        if (assignmentRepository.isAssetCurrentlyAssigned(assetId)) {
            throw new IllegalStateException("Asset '" + asset.getAssetTag() + "' is already actively assigned to an employee.");
        }

        // 4. Verify recipient employee is active
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot assign asset to employee with status: " + employee.getStatus());
        }

        // 5. Verify user performing the assignment
        User assignedByUser = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        // 6. Transition status & persist assignment
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employee(employee)
                .assignedBy(assignedByUser)
                .assignedAt(Instant.now())
                .conditionNotes(request.getConditionNotes())
                .build();

        AssetAssignment saved = assignmentRepository.save(assignment);
        log.info("Successfully assigned asset [{}] to employee [{}] by user [{}]",
                asset.getAssetTag(), employee.getEmployeeCode(), assignedByUser.getUsername());

        return mapToAssignmentResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "dashboardSummary", allEntries = true)
    @Auditable(action = AuditAction.RETURN, entityName = "AssetAssignment", description = "Accepted returned asset")
    public AssetAssignmentResponse returnAsset(Long assetId, AssetReturnRequest request) {
        log.info("Starting pessimistic lock acquisition for returning asset ID: {}", assetId);

        // 1. Acquire exclusive row-level write lock
        Asset asset = assetRepository.findByIdWithLock(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", assetId));

        // 2. Retrieve active assignment with write lock
        AssetAssignment activeAssignment = assignmentRepository.findActiveAssignmentByAssetIdWithLock(assetId)
                .orElseThrow(() -> new IllegalStateException("Asset '" + asset.getAssetTag() + "' does not have an active assignment to return."));

        // 3. Close assignment
        activeAssignment.setReturnedAt(Instant.now());
        if (request != null && StringUtils.hasText(request.getConditionNotes())) {
            String existingNotes = activeAssignment.getConditionNotes();
            String returnNotes = "Return Notes: " + request.getConditionNotes();
            activeAssignment.setConditionNotes(StringUtils.hasText(existingNotes) ? existingNotes + " | " + returnNotes : returnNotes);
        }

        // 4. Transition asset status
        AssetStatus targetStatus = (request != null && request.getNewStatus() != null)
                ? request.getNewStatus()
                : AssetStatus.AVAILABLE;
        asset.setStatus(targetStatus);

        assetRepository.save(asset);
        AssetAssignment updatedAssignment = assignmentRepository.save(activeAssignment);

        log.info("Successfully processed return for asset [{}] with new status: {}", asset.getAssetTag(), targetStatus);

        return mapToAssignmentResponse(updatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAssignmentResponse> getAssetAssignmentHistory(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }
        return assignmentRepository.findHistoryByAssetId(assetId).stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAssignmentResponse> getMyActiveAssets(UserPrincipal userPrincipal) {
        Employee employee = employeeRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user id: " + userPrincipal.getId()));

        return assignmentRepository.findActiveAssignmentsByEmployeeId(employee.getId()).stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Helper Mappers
    // =========================================================================

    private AssetResponse mapToAssetResponse(Asset asset, AssetAssignment activeAssignment) {
        Long assignedToEmpId = null;
        String assignedToEmpName = null;
        String assignedToEmpCode = null;
        Instant assignedAt = null;

        if (activeAssignment != null && activeAssignment.getEmployee() != null) {
            Employee emp = activeAssignment.getEmployee();
            assignedToEmpId = emp.getId();
            assignedToEmpName = emp.getFullName();
            assignedToEmpCode = emp.getEmployeeCode();
            assignedAt = activeAssignment.getAssignedAt();
        }

        return AssetResponse.builder()
                .id(asset.getId())
                .assetTag(asset.getAssetTag())
                .categoryId(asset.getCategory() != null ? asset.getCategory().getId() : null)
                .categoryName(asset.getCategory() != null ? asset.getCategory().getName() : null)
                .modelName(asset.getModelName())
                .serialNumber(asset.getSerialNumber())
                .purchaseDate(asset.getPurchaseDate())
                .status(asset.getStatus())
                .assignedToEmployeeId(assignedToEmpId)
                .assignedToEmployeeName(assignedToEmpName)
                .assignedToEmployeeCode(assignedToEmpCode)
                .assignedAt(assignedAt)
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .version(asset.getVersion())
                .build();
    }

    private AssetAssignmentResponse mapToAssignmentResponse(AssetAssignment assignment) {
        Asset asset = assignment.getAsset();
        Employee emp = assignment.getEmployee();
        User assignedBy = assignment.getAssignedBy();

        return AssetAssignmentResponse.builder()
                .id(assignment.getId())
                .assetId(asset != null ? asset.getId() : null)
                .assetTag(asset != null ? asset.getAssetTag() : null)
                .assetModelName(asset != null ? asset.getModelName() : null)
                .categoryName(asset != null && asset.getCategory() != null ? asset.getCategory().getName() : null)
                .employeeId(emp != null ? emp.getId() : null)
                .employeeName(emp != null ? emp.getFullName() : null)
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .assignedByUserId(assignedBy != null ? assignedBy.getId() : null)
                .assignedByUsername(assignedBy != null ? assignedBy.getUsername() : null)
                .assignedAt(assignment.getAssignedAt())
                .returnedAt(assignment.getReturnedAt())
                .conditionNotes(assignment.getConditionNotes())
                .isActive(assignment.getReturnedAt() == null)
                .build();
    }
}
