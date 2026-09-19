package com.company.eams.service.impl;

import com.company.eams.dto.request.DepartmentCreateRequest;
import com.company.eams.dto.response.DepartmentResponse;
import com.company.eams.entity.Department;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.DepartmentRepository;
import com.company.eams.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .build();
    }

    @Test
    @DisplayName("Create department successfully when name does not exist")
    void testCreateDepartmentSuccess() {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setName("Engineering");

        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        DepartmentResponse response = departmentService.createDepartment(request);

        assertNotNull(response);
        assertEquals("Engineering", response.getName());
        assertEquals(1L, response.getId());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Create department throws IllegalArgumentException when duplicate name")
    void testCreateDepartmentDuplicateNameThrowsException() {
        DepartmentCreateRequest request = new DepartmentCreateRequest();
        request.setName("Engineering");

        when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> departmentService.createDepartment(request));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Get department by ID returns correct department response")
    void testGetDepartmentById() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(5L);

        DepartmentResponse response = departmentService.getDepartmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Engineering", response.getName());
        assertEquals(5L, response.getEmployeeCount());
    }

    @Test
    @DisplayName("Get department by non-existent ID throws ResourceNotFoundException")
    void testGetDepartmentByInvalidIdThrowsNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getDepartmentById(99L));
    }

    @Test
    @DisplayName("Delete department throws IllegalStateException when assigned employees exist")
    void testDeleteDepartmentWithAssignedEmployeesThrowsException() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(3L);

        assertThrows(IllegalStateException.class, () -> departmentService.deleteDepartment(1L));
        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    @DisplayName("Delete department succeeds when no assigned employees")
    void testDeleteDepartmentSuccess() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.countEmployeesByDepartmentId(1L)).thenReturn(0L);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(department);
    }
}
