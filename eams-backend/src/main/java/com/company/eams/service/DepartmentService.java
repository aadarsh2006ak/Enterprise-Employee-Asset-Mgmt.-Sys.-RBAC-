package com.company.eams.service;

import com.company.eams.dto.request.DepartmentCreateRequest;
import com.company.eams.dto.request.DepartmentUpdateRequest;
import com.company.eams.dto.response.DepartmentResponse;
import com.company.eams.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentCreateRequest request);

    PageResponse<DepartmentResponse> getAllDepartments(Pageable pageable, String search);

    List<DepartmentResponse> getAllDepartmentsList();

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request);

    void deleteDepartment(Long id);
}
