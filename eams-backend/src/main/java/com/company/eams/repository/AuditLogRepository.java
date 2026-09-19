package com.company.eams.repository;

import com.company.eams.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Optional<AuditLog> findById(@NonNull Long id);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Page<AuditLog> findAll(Specification<AuditLog> spec, @NonNull Pageable pageable);
}
