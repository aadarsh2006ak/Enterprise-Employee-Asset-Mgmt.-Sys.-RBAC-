package com.company.eams.scheduler;

import com.company.eams.entity.ExportJob;
import com.company.eams.entity.enums.ExportJobStatus;
import com.company.eams.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportJobCleanupScheduler {

    private final ExportJobRepository exportJobRepository;

    /**
     * Periodically purges expired export files from local disk and updates records.
     * ShedLock guarantees only a single cluster node executes this task concurrently.
     */
    @Scheduled(cron = "${eams.scheduler.export-cleanup-cron:0 0 * * * *}")
    @SchedulerLock(name = "ExportJobCleanupScheduler_purgeExpiredFiles", lockAtLeastFor = "1m", lockAtMostFor = "15m")
    @Transactional
    public void purgeExpiredExportFiles() {
        log.info("Starting distributed scheduled purge for expired export files...");

        Instant now = Instant.now();
        List<ExportJob> expiredJobs = exportJobRepository.findByStatusAndExpiresAtBefore(ExportJobStatus.COMPLETED, now);

        if (expiredJobs.isEmpty()) {
            log.info("No expired export files found to purge.");
            return;
        }

        int deletedCount = 0;
        long freedBytes = 0;

        for (ExportJob job : expiredJobs) {
            try {
                if (job.getFilePath() != null) {
                    Path path = Paths.get(job.getFilePath());
                    if (Files.exists(path)) {
                        long size = Files.size(path);
                        Files.deleteIfExists(path);
                        freedBytes += size;
                        deletedCount++;
                        log.debug("Deleted expired file: {} for job {}", path, job.getJobUuid());
                    }
                    job.setFilePath(null);
                }
                job.setErrorMessage("Export artifact expired and purged from storage.");
                exportJobRepository.save(job);
            } catch (Exception ex) {
                log.error("Failed to delete expired export file for job {}: {}", job.getJobUuid(), ex.getMessage());
            }
        }

        log.info("Completed export cleanup: purged {} files, freed {} bytes ({} KB)",
                deletedCount, freedBytes, freedBytes / 1024);
    }
}
