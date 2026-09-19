package com.company.eams.service.impl;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.dto.request.DepartmentCreateRequest;
import com.company.eams.dto.request.DepartmentUpdateRequest;
import com.company.eams.dto.response.DepartmentResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.Department;
import com.company.eams.entity.Employee;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.DepartmentRepository;
import com.company.eams.repository.EmployeeRepository;
import com.company.eams.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    @Auditable(action = AuditAction.CREATE, entityName = "Department", description = "Created organizational department")
    public DepartmentResponse createDepartment(DepartmentCreateRequest request) {
        String trimmedName = request.getName().trim();
        log.info("Creating department with name: {}", trimmedName);

        if (departmentRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Department with name '" + trimmedName + "' already exists");
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getManagerId()));
        }

        Department department = Department.builder()
                .name(trimmedName)
                .manager(manager)
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Created department with ID: {}", saved.getId());

        return mapToDepartmentResponse(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> getAllDepartments(Pageable pageable, String search) {
        log.debug("Fetching paginated departments with search: {}", search);

        Page<Department> page;
        if (StringUtils.hasText(search)) {
            page = departmentRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            page = departmentRepository.findAll(pageable);
        }

        Map<Long, Long> countsMap = fetchEmployeeCountsMap();

        Page<DepartmentResponse> responsePage = page.map(dept ->
                mapToDepartmentResponse(dept, countsMap.getOrDefault(dept.getId(), 0L))
        );

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "'all-list'")
    public List<DepartmentResponse> getAllDepartmentsList() {
        List<Department> list = departmentRepository.findAllWithManager();
        Map<Long, Long> countsMap = fetchEmployeeCountsMap();

        return list.stream()
                .map(dept -> mapToDepartmentResponse(dept, countsMap.getOrDefault(dept.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "#id")
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        return mapToDepartmentResponse(department, employeeCount);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    @Auditable(action = AuditAction.UPDATE, entityName = "Department", description = "Updated department details")
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        String trimmedName = request.getName().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new IllegalArgumentException("Another department with name '" + trimmedName + "' already exists");
        }

        department.setName(trimmedName);

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getManagerId()));
            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        Department updated = departmentRepository.save(department);
        long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        log.info("Updated department ID: {}", id);

        return mapToDepartmentResponse(updated, employeeCount);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    @Auditable(action = AuditAction.DELETE, entityName = "Department", description = "Deleted organizational department")
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        long employeeCount = departmentRepository.countEmployeesByDepartmentId(id);
        if (employeeCount > 0) {
            throw new IllegalStateException("Cannot delete department with " + employeeCount + " assigned employees. Please reassign or remove employees first.");
        }

        departmentRepository.delete(department);
        log.info("Deleted department ID: {}", id);
    }

    private Map<Long, Long> fetchEmployeeCountsMap() {
        Map<Long, Long> map = new HashMap<>();
        List<Object[]> results = departmentRepository.countEmployeesGroupedByDepartment();
        for (Object[] row : results) {
            Long deptId = (Long) row[0];
            Long count = (Long) row[1];
            map.put(deptId, count);
        }
        return map;
    }

    private DepartmentResponse mapToDepartmentResponse(Department department, Long employeeCount) {
        Employee manager = department.getManager();
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .managerId(manager != null ? manager.getId() : null)
                .managerName(manager != null ? manager.getFullName() : null)
                .managerEmployeeCode(manager != null ? manager.getEmployeeCode() : null)
                .employeeCount(employeeCount)
                .build();
    }
}
