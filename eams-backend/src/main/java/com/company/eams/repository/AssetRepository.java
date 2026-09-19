package com.company.eams.repository;

import com.company.eams.entity.Asset;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    /**
     * Pessimistic row-level write lock (SELECT ... FOR UPDATE) to prevent race conditions during asset assignment/returns.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM Asset a WHERE a.id = :id")
    Optional<Asset> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM Asset a WHERE a.id = :id")
    Optional<Asset> findByIdForUpdate(@Param("id") Long id);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"category"})
    Optional<Asset> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"category"})
    Optional<Asset> findByAssetTag(String assetTag);

    boolean existsByAssetTagIgnoreCase(String assetTag);

    boolean existsByAssetTagIgnoreCaseAndIdNot(String assetTag, Long id);

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);

    long countByCategoryId(Long categoryId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"category"})
    Page<Asset> findAll(Specification<Asset> spec, @NonNull Pageable pageable);
}
