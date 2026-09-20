package com.company.eams.repository;

import com.company.eams.entity.AssetAssignment;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {

    @EntityGraph(attributePaths = {"asset", "asset.category", "employee", "assignedBy"})
    @Query("""
            SELECT aa
            FROM AssetAssignment aa
            WHERE aa.asset.id = :assetId
              AND aa.returnedAt IS NULL
            """)
    Optional<AssetAssignment> findActiveAssignmentByAssetId(
            @Param("assetId") Long assetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @EntityGraph(attributePaths = {"asset", "asset.category", "employee", "assignedBy"})
    @Query("""
            SELECT aa
            FROM AssetAssignment aa
            WHERE aa.asset.id = :assetId
              AND aa.returnedAt IS NULL
            """)
    Optional<AssetAssignment> findActiveAssignmentByAssetIdWithLock(
            @Param("assetId") Long assetId
    );

    // NEW: Batch fetch active assignments for multiple assets
    @EntityGraph(attributePaths = {"asset", "asset.category", "employee", "assignedBy"})
    @Query("""
            SELECT aa
            FROM AssetAssignment aa
            WHERE aa.asset.id IN :assetIds
              AND aa.returnedAt IS NULL
            """)
    List<AssetAssignment> findActiveAssignmentsByAssetIdsIn(
            @Param("assetIds") List<Long> assetIds
    );

    @EntityGraph(attributePaths = {"asset", "asset.category", "employee", "assignedBy"})
    @Query("""
            SELECT aa
            FROM AssetAssignment aa
            WHERE aa.employee.id = :employeeId
              AND aa.returnedAt IS NULL
            ORDER BY aa.assignedAt DESC
            """)
    List<AssetAssignment> findActiveAssignmentsByEmployeeId(
            @Param("employeeId") Long employeeId
    );

    @EntityGraph(attributePaths = {"asset", "asset.category", "employee", "assignedBy"})
    @Query("""
            SELECT aa
            FROM AssetAssignment aa
            WHERE aa.asset.id = :assetId
            ORDER BY aa.assignedAt DESC
            """)
    List<AssetAssignment> findHistoryByAssetId(
            @Param("assetId") Long assetId
    );

    @Query("""
            SELECT COUNT(aa) > 0
            FROM AssetAssignment aa
            WHERE aa.asset.id = :assetId
              AND aa.returnedAt IS NULL
            """)
    boolean isAssetCurrentlyAssigned(
            @Param("assetId") Long assetId
    );

    @EntityGraph(attributePaths = {"asset", "asset.category", "employee", "assignedBy"})
    @Query("SELECT aa FROM AssetAssignment aa ORDER BY aa.assignedAt DESC")
    List<AssetAssignment> findAllWithDetails();
}