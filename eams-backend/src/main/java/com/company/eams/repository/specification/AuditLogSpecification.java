package com.company.eams.repository.specification;

import com.company.eams.entity.AuditLog;
import com.company.eams.entity.enums.AuditAction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLog> filter(
            String entityName,
            Long entityId,
            AuditAction action,
            String username,
            Long userId,
            Instant startDate,
            Instant endDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(entityName)) {
                predicates.add(cb.equal(cb.lower(root.get("entityName")), entityName.trim().toLowerCase()));
            }

            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }

            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(cb.lower(root.get("usernameSnapshot")), "%" + username.trim().toLowerCase() + "%"));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.join("user").get("id"), userId));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
