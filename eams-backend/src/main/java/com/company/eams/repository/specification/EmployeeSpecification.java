package com.company.eams.repository.specification;

import com.company.eams.entity.Employee;
import com.company.eams.entity.enums.EmployeeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> filter(Long departmentId, EmployeeStatus status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate fullNamePredicate = cb.like(cb.lower(root.get("fullName")), searchPattern);
                Predicate codePredicate = cb.like(cb.lower(root.get("employeeCode")), searchPattern);
                Predicate designationPredicate = cb.like(cb.lower(root.get("designation")), searchPattern);
                Predicate emailPredicate = cb.like(cb.lower(root.join("user").get("email")), searchPattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.join("user").get("username")), searchPattern);

                predicates.add(cb.or(fullNamePredicate, codePredicate, designationPredicate, emailPredicate, usernamePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
