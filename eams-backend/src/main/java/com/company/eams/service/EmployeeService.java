package com.company.eams.service;

import com.company.eams.dto.request.EmployeeCreateRequest;
import com.company.eams.dto.request.EmployeeUpdateRequest;
import com.company.eams.dto.response.EmployeeResponse;
import com.company.eams.dto.response.EmployeeSummaryResponse;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.entity.enums.EmployeeStatus;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    PageResponse<EmployeeSummaryResponse> getAllEmployees(Pageable pageable, Long departmentId, EmployeeStatus status, String search);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByUserId(Long userId);

    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);

    EmployeeResponse updateEmployeeStatus(Long id, EmployeeStatus status);

    void deleteEmployee(Long id);
}
