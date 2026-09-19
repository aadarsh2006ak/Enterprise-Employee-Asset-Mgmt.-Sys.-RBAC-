package com.company.eams.repository.specification;

import com.company.eams.entity.Asset;
import com.company.eams.entity.enums.AssetStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AssetSpecification {

    public static Specification<Asset> filter(Long categoryId, AssetStatus status, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate tagPredicate = cb.like(cb.lower(root.get("assetTag")), pattern);
                Predicate modelPredicate = cb.like(cb.lower(root.get("modelName")), pattern);
                Predicate serialPredicate = cb.like(cb.lower(root.get("serialNumber")), pattern);
                Predicate categoryPredicate = cb.like(cb.lower(root.join("category").get("name")), pattern);

                predicates.add(cb.or(tagPredicate, modelPredicate, serialPredicate, categoryPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
