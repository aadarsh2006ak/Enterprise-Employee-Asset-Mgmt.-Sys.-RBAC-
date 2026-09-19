package com.company.eams.repository;

import com.company.eams.entity.ExportJob;
import com.company.eams.entity.enums.ExportJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<ExportJob> findByJobUuid(String jobUuid);

    @EntityGraph(attributePaths = {"user"})
    Page<ExportJob> findByUserId(Long userId, Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user"})
    Page<ExportJob> findAll(@NonNull Pageable pageable);

    List<ExportJob> findByStatusAndExpiresAtBefore(ExportJobStatus status, Instant threshold);
}
