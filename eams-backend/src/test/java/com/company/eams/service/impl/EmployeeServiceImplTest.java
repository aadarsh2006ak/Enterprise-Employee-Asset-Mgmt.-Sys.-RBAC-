package com.company.eams.service.impl;

import com.company.eams.dto.request.EmployeeCreateRequest;
import com.company.eams.dto.response.EmployeeResponse;
import com.company.eams.entity.Department;
import com.company.eams.entity.Employee;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.DepartmentRepository;
import com.company.eams.repository.EmployeeRepository;
import com.company.eams.repository.RoleRepository;
import com.company.eams.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Role employeeRole;
    private Department department;
    private Employee employee;
    private User user;

    @BeforeEach
    void setUp() {
        employeeRole = Role.builder()
                .id(1L)
                .name(RoleType.EMPLOYEE)
                .permissions(Collections.emptySet())
                .build();

        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .build();

        user = User.builder()
                .id(10L)
                .username("jane_doe")
                .email("jane@company.com")
                .passwordHash("encodedHash")
                .role(employeeRole)
                .isActive(true)
                .build();

        employee = Employee.builder()
                .id(100L)
                .employeeCode("EMP-001")
                .fullName("Jane Doe")
                .user(user)
                .department(department)
                .designation("Software Engineer")
                .status(EmployeeStatus.ACTIVE)
                .dateOfJoining(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Create employee successfully")
    void testCreateEmployeeSuccess() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmployeeCode("EMP-001");
        request.setUsername("jane_doe");
        request.setEmail("jane@company.com");
        request.setPassword("SecurePass1!");
        request.setFullName("Jane Doe");
        request.setRole(RoleType.EMPLOYEE);
        request.setDepartmentId(1L);
        request.setDesignation("Software Engineer");
        request.setStatus(EmployeeStatus.ACTIVE);
        request.setDateOfJoining(LocalDate.now());

        when(employeeRepository.existsByEmployeeCodeIgnoreCase("EMP-001")).thenReturn(false);
        when(userRepository.existsByUsername("jane_doe")).thenReturn(false);
        when(userRepository.existsByEmail("jane@company.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedHash");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals("EMP-001", response.getEmployeeCode());
        assertEquals("Jane Doe", response.getFullName());
        assertEquals("jane_doe", response.getUsername());
        assertEquals("Engineering", response.getDepartmentName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create employee fails when duplicate employee code exists")
    void testCreateEmployeeDuplicateCodeThrowsException() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setEmployeeCode("EMP-001");
        request.setUsername("jane_doe");
        request.setEmail("jane@company.com");

        when(employeeRepository.existsByEmployeeCodeIgnoreCase("EMP-001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Get employee by ID returns EmployeeResponse")
    void testGetEmployeeByIdSuccess() {
        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = employeeService.getEmployeeById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("EMP-001", response.getEmployeeCode());
        assertEquals(EmployeeStatus.ACTIVE, response.getStatus());
    }

    @Test
    @DisplayName("Get employee by non-existent ID throws ResourceNotFoundException")
    void testGetEmployeeByInvalidIdThrowsNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(999L));
    }
}
