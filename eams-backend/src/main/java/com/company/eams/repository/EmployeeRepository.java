package com.company.eams.repository;

import com.company.eams.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "user.role", "department", "reportingTo"})
    Optional<Employee> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"user", "user.role", "department", "reportingTo"})
    Optional<Employee> findByEmployeeCode(String employeeCode);

    @EntityGraph(attributePaths = {"user", "user.role", "department", "reportingTo"})
    Optional<Employee> findByUserId(Long userId);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    long countByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByReportingToId(Long reportingToId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "user.role", "department", "reportingTo"})
    Page<Employee> findAll(Specification<Employee> spec, @NonNull Pageable pageable);
}
