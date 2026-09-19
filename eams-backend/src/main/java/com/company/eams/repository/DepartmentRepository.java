package com.company.eams.repository;

import com.company.eams.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @EntityGraph(attributePaths = {"manager"})
    Optional<Department> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @EntityGraph(attributePaths = {"manager"})
    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"manager"})
    @Query("SELECT d FROM Department d")
    List<Department> findAllWithManager();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    long countEmployeesByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT e.department.id, COUNT(e) FROM Employee e WHERE e.department.id IS NOT NULL GROUP BY e.department.id")
    List<Object[]> countEmployeesGroupedByDepartment();
}
