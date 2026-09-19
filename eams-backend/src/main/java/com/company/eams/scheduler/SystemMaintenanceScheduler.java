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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMaintenanceScheduler {

    private final ExportJobRepository exportJobRepository;

    /**
     * Daily maintenance and stuck-job recovery job.
     * Recovers jobs that were left in PROCESSING state (e.g. if a worker node restarted).
     */
    @Scheduled(cron = "${eams.scheduler.maintenance-cron:0 0 2 * * *}")
    @SchedulerLock(name = "SystemMaintenanceScheduler_runDailyCheck", lockAtLeastFor = "30s", lockAtMostFor = "5m")
    @Transactional
    public void runDailyMaintenance() {
        log.info("Executing daily maintenance routine with ShedLock distributed lock...");

        // Find stuck processing jobs older than 6 hours
        Instant stuckThreshold = Instant.now().minus(6, ChronoUnit.HOURS);
        List<ExportJob> stuckJobs = exportJobRepository.findAll().stream()
                .filter(job -> job.getStatus() == ExportJobStatus.PROCESSING &&
                               job.getStartedAt() != null &&
                               job.getStartedAt().isBefore(stuckThreshold))
                .toList();

        for (ExportJob job : stuckJobs) {
            log.warn("Marking orphaned/stuck job {} as FAILED", job.getJobUuid());
            job.setStatus(ExportJobStatus.FAILED);
            job.setErrorMessage("Export job timed out or worker process terminated unexpectedly.");
            job.setCompletedAt(Instant.now());
            exportJobRepository.save(job);
        }

        log.info("Daily maintenance completed. Recovered {} stuck export jobs.", stuckJobs.size());
    }
}
