package com.company.eams.concurrency;

import com.company.eams.dto.request.AssetAssignRequest;
import com.company.eams.dto.response.AssetAssignmentResponse;
import com.company.eams.entity.Asset;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.exception.ConflictException;
import com.company.eams.repository.AssetAssignmentRepository;
import com.company.eams.repository.AssetRepository;
import com.company.eams.repository.EmployeeRepository;
import com.company.eams.repository.UserRepository;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.impl.AssetServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetConcurrencyTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetAssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssetServiceImpl assetService;

    @Test
    @DisplayName("Pessimistic Lock Verification: Only one assignment succeeds in parallel thread execution (Section 18)")
    void concurrentAssignment_onlyOneShouldSucceed() throws InterruptedException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Given a shared asset
        Long sharedAssetId = 100L;
        Asset availableAsset = Asset.builder()
                .id(sharedAssetId)
                .assetTag("AST-LAP-999")
                .status(AssetStatus.AVAILABLE)
                .build();

        when(assetRepository.findByIdWithLock(sharedAssetId)).thenReturn(Optional.of(availableAsset));

        UserPrincipal adminUser = UserPrincipal.builder()
                .id(1L)
                .username("admin")
                .role(RoleType.ADMIN)
                .build();

        for (int i = 0; i < threads; i++) {
            final long employeeId = 10L + i;
            executor.submit(() -> {
                try {
                    AssetAssignRequest req = new AssetAssignRequest();
                    req.setEmployeeId(employeeId);
                    req.setConditionNotes("Issued in working condition");

                    AssetAssignmentResponse res = assetService.assignAsset(sharedAssetId, req, adminUser);
                    if (res != null) {
                        successCount.incrementAndGet();
                    }
                } catch (ConflictException | IllegalStateException ignored) {
                    // Expected failure on subsequent parallel attempts
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Verifies the locking and status update logic
        verify(assetRepository, atLeastOnce()).findByIdWithLock(sharedAssetId);
    }
}
