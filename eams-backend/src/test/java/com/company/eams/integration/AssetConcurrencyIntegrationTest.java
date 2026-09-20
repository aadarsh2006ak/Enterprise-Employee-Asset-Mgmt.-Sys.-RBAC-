package com.company.eams.integration;

import com.company.eams.dto.request.AssetAssignRequest;
import com.company.eams.entity.Asset;
import com.company.eams.entity.AssetCategory;
import com.company.eams.entity.Employee;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.AssetStatus;
import com.company.eams.entity.enums.EmployeeStatus;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.repository.*;
import com.company.eams.security.UserPrincipal;
import com.company.eams.service.AssetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssetConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    @Test
    @DisplayName("Testcontainers Concurrency: 20 simultaneous threads attempt assigning the same asset; exactly 1 succeeds")
    void testConcurrentAssignmentPessimisticLock() throws InterruptedException {
        // 1. Prepare Category & Asset
        AssetCategory category = categoryRepository.save(AssetCategory.builder().name("HighConcurrencyLaptops").build());
        Asset asset = assetRepository.save(Asset.builder()
                .assetTag("CONC-LAP-001")
                .category(category)
                .modelName("ThinkPad X1 Carbon")
                .status(AssetStatus.AVAILABLE)
                .purchaseDate(LocalDate.now())
                .build());

        Role employeeRole = roleRepository.findByName(RoleType.EMPLOYEE)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.EMPLOYEE).description("Employee").build()));

        Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleType.ADMIN).description("Admin").build()));

        User adminUser = userRepository.save(User.builder()
                .username("concurrency_admin")
                .email("conc_admin@company.com")
                .passwordHash("hashed")
                .role(adminRole)
                .isActive(true)
                .build());

        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        // 2. Prepare 20 recipient employees
        int threadCount = 20;
        List<Employee> recipients = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            User user = userRepository.save(User.builder()
                    .username("conc_user_" + i)
                    .email("conc_user_" + i + "@company.com")
                    .passwordHash("hashed")
                    .role(employeeRole)
                    .isActive(true)
                    .build());

            Employee emp = employeeRepository.save(Employee.builder()
                    .user(user)
                    .employeeCode("CONC-EMP-" + i)
                    .fullName("Concurrent Emp " + i)
                    .status(EmployeeStatus.ACTIVE)
                    .dateOfJoining(LocalDate.now())
                    .build());

            recipients.add(emp);
        }

        // 3. Launch 20 concurrent threads trying to checkout CONC-LAP-001 at the exact same moment
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Employee targetEmp = recipients.get(i);
            executor.submit(() -> {
                try {
                    latch.await(); // Synchronize all threads to fire simultaneously
                    AssetAssignRequest req = new AssetAssignRequest();
                    req.setEmployeeId(targetEmp.getId());
                    req.setConditionNotes("Issued in high load test");

                    assetService.assignAsset(asset.getId(), req, adminPrincipal);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        latch.countDown();
        doneLatch.await();
        executor.shutdown();

        // 4. Assert concurrency guarantees
        assertEquals(1, successCount.get(), "Exactly one assignment must succeed under concurrent race conditions");
        assertEquals(threadCount - 1, failureCount.get(), "All other concurrent attempts must be rejected");

        // 5. Verify database state in PostgreSQL Testcontainer
        Asset updatedAsset = assetRepository.findById(asset.getId()).orElseThrow();
        assertEquals(AssetStatus.ASSIGNED, updatedAsset.getStatus());

        List<com.company.eams.entity.AssetAssignment> assignments = assignmentRepository.findHistoryByAssetId(asset.getId());
        assertEquals(1, assignments.size(), "Only 1 active assignment row should exist in PostgreSQL");
    }
}
