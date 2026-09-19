package com.company.eams.config;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@Slf4j
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerLockConfig {

    /**
     * Resilient ShedLock LockProvider:
     * Attempts Redis distributed lock first; if Redis is down/unreachable, gracefully falls back
     * to single-node standalone execution without throwing exceptions or blocking scheduler threads.
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        RedisLockProvider delegate = new RedisLockProvider(connectionFactory, "eams:scheduler-locks");
        return new LockProvider() {
            @Override
            public Optional<SimpleLock> lock(LockConfiguration lockConfiguration) {
                try {
                    return delegate.lock(lockConfiguration);
                } catch (Exception ex) {
                    log.warn("Redis LockProvider unavailable for lock '{}'. Running task in standalone fallback mode. Error: {}",
                            lockConfiguration.getName(), ex.getMessage());
                    return Optional.of(new SimpleLock() {
                        @Override
                        public void unlock() {
                            // no-op for standalone fallback
                        }
                    });
                }
            }
        };
    }
}
