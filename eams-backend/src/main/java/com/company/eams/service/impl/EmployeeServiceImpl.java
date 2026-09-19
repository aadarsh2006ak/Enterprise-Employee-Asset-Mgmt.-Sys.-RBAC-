package com.company.eams.service.impl;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.dto.request.EmployeeCreateRequest;
import com.company.eams.dto.request.EmployeeUpdateRequest;
import com.company.eams.dto.response.EmployeeResponse;
import com.company.eams.dto.response.EmployeeSummaryResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.Department;
import com.company.eams.entity.Employee;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.DepartmentRepository;
import com.company.eams.repository.EmployeeRepository;
import com.company.eams.repository.RoleRepository;
import com.company.eams.repository.UserRepository;
import com.company.eams.repository.specification.EmployeeSpecification;
import com.company.eams.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityName = "Employee", description = "Created employee profile and provisioned user")
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        String trimmedCode = request.getEmployeeCode().trim();
        String trimmedUsername = request.getUsername().trim();
        String trimmedEmail = request.getEmail().trim().toLowerCase();

        log.info("Creating employee: code={}, username={}", trimmedCode, trimmedUsername);

        // 1. Uniqueness Checks
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(trimmedCode)) {
            throw new IllegalArgumentException("Employee code '" + trimmedCode + "' is already in use");
        }
        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
        }
        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new IllegalArgumentException("Email '" + trimmedEmail + "' is already registered");
        }

        // 2. Resolve Role
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRole()));

        // 3. Resolve Password
        String rawPassword = StringUtils.hasText(request.getPassword()) ? request.getPassword() : "Company@123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        boolean isUserActive = request.getStatus() == EmployeeStatus.ACTIVE;

        // 4. Create and Persist Linked User Account
        User user = User.builder()
                .username(trimmedUsername)
                .email(trimmedEmail)
                .passwordHash(encodedPassword)
                .role(role)
                .isActive(isUserActive)
                .build();

        User savedUser = userRepository.save(user);

        // 5. Resolve Department & Manager
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        }

        Employee reportingTo = null;
        if (request.getReportingToId() != null) {
            reportingTo = employeeRepository.findById(request.getReportingToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getReportingToId()));
        }

        // 6. Create and Persist Employee Profile
        Employee employee = Employee.builder()
                .user(savedUser)
                .employeeCode(trimmedCode)
                .fullName(request.getFullName().trim())
                .department(department)
                .designation(request.getDesignation())
                .dateOfJoining(request.getDateOfJoining())
                .reportingTo(reportingTo)
                .status(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Created employee profile ID: {} for user: {}", savedEmployee.getId(), savedUser.getUsername());

        return mapToEmployeeResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeSummaryResponse> getAllEmployees(
            Pageable pageable, Long departmentId, EmployeeStatus status, String search) {

        log.debug("Fetching paginated employees: dept={}, status={}, search={}", departmentId, status, search);

        Specification<Employee> spec = EmployeeSpecification.filter(departmentId, status, search);
        Page<Employee> page = employeeRepository.findAll(spec, pageable);

        Page<EmployeeSummaryResponse> summaryPage = page.map(this::mapToSummaryResponse);
        return PageResponse.from(summaryPage);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return mapToEmployeeResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByUserId(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "userId", userId));
        return mapToEmployeeResponse(employee);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityName = "Employee", description = "Updated employee details")
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Self-reporting prevention
        if (request.getReportingToId() != null && request.getReportingToId().equals(id)) {
            throw new IllegalArgumentException("An employee cannot report to themselves");
        }

        employee.setFullName(request.getFullName().trim());
        employee.setDesignation(request.getDesignation());
        employee.setDateOfJoining(request.getDateOfJoining());

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(department);
        } else {
            employee.setDepartment(null);
        }

        if (request.getReportingToId() != null) {
            Employee reportingTo = employeeRepository.findById(request.getReportingToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getReportingToId()));
            employee.setReportingTo(reportingTo);
        } else {
            employee.setReportingTo(null);
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Updated employee details for ID: {}", id);

        return mapToEmployeeResponse(updated);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.STATUS_CHANGE, entityName = "Employee", description = "Updated employee employment status")
    public EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setStatus(status);

        // Synchronize linked User active status (Deactivate credentials upon resignation or termination)
        User user = employee.getUser();
        if (user != null) {
            boolean active = (status == EmployeeStatus.ACTIVE);
            user.setIsActive(active);
            userRepository.save(user);
            log.info("Synchronized user account active status={}: userId={}", active, user.getId());
        }

        Employee updated = employeeRepository.save(employee);
        log.info("Updated employee status to {} for ID: {}", status, id);

        return mapToEmployeeResponse(updated);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, entityName = "Employee", description = "Deleted employee profile and credentials")
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // 1. Unassign as department manager if applicable
        List<Department> managedDepartments = departmentRepository.findAll().stream()
                .filter(d -> d.getManager() != null && d.getManager().getId().equals(id))
                .toList();
        for (Department dept : managedDepartments) {
            dept.setManager(null);
            departmentRepository.save(dept);
        }

        // 2. Unlink subordinates reporting to this employee
        List<Employee> subordinates = employeeRepository.findByReportingToId(id);
        for (Employee sub : subordinates) {
            sub.setReportingTo(null);
            employeeRepository.save(sub);
        }

        // 3. Delete employee record (Cascade deletes user)
        employeeRepository.delete(employee);
        log.info("Deleted employee profile and linked user for ID: {}", id);
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        User user = employee.getUser();
        Department dept = employee.getDepartment();
        Employee manager = employee.getReportingTo();

        return EmployeeResponse.builder()
                .id(employee.getId())
                .userId(user != null ? user.getId() : null)
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .role(user != null && user.getRole() != null ? user.getRole().getName() : null)
                .isUserActive(user != null ? user.getIsActive() : false)
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .departmentId(dept != null ? dept.getId() : null)
                .departmentName(dept != null ? dept.getName() : null)
                .designation(employee.getDesignation())
                .dateOfJoining(employee.getDateOfJoining())
                .reportingToId(manager != null ? manager.getId() : null)
                .reportingToName(manager != null ? manager.getFullName() : null)
                .reportingToCode(manager != null ? manager.getEmployeeCode() : null)
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .version(employee.getVersion())
                .build();
    }

    private EmployeeSummaryResponse mapToSummaryResponse(Employee employee) {
        User user = employee.getUser();
        Department dept = employee.getDepartment();

        return EmployeeSummaryResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .email(user != null ? user.getEmail() : null)
                .departmentName(dept != null ? dept.getName() : null)
                .designation(employee.getDesignation())
                .role(user != null && user.getRole() != null ? user.getRole().getName() : null)
                .status(employee.getStatus())
                .dateOfJoining(employee.getDateOfJoining())
                .build();
    }
}
