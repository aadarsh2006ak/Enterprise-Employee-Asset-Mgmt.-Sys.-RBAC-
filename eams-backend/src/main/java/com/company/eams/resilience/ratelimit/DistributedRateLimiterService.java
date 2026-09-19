package com.company.eams.resilience.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedRateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_PREFIX = "eams:ratelimit:";

    /**
     * Check and consume rate limit quota using an atomic Redis counter.
     *
     * @param clientIdentifier IP address or username
     * @param actionScope      Category (e.g., "auth", "export", "general")
     * @param limitPerMinute   Maximum requests permitted per 60-second window
     * @return RateLimitResult containing quota state and reset timing
     */
    public RateLimitResult checkLimit(String clientIdentifier, String actionScope, long limitPerMinute) {
        long currentEpochSecond = Instant.now().getEpochSecond();
        long currentMinuteWindow = currentEpochSecond / 60;
        long resetSeconds = 60 - (currentEpochSecond % 60);

        String redisKey = RATE_LIMIT_PREFIX + actionScope + ":" + clientIdentifier + ":" + currentMinuteWindow;

        try {
            Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);

            if (currentCount != null && currentCount == 1L) {
                // Ensure key expires after window passes (+ 10s buffer)
                stringRedisTemplate.expire(redisKey, Duration.ofSeconds(70));
            }

            long current = currentCount != null ? currentCount : 1L;
            boolean allowed = current <= limitPerMinute;
            long remaining = Math.max(0, limitPerMinute - current);

            return RateLimitResult.builder()
                    .allowed(allowed)
                    .limit(limitPerMinute)
                    .remaining(remaining)
                    .resetSeconds(resetSeconds)
                    .build();

        } catch (Exception ex) {
            // Graceful fallback if Redis is unavailable: allow request and log warning
            log.warn("Distributed rate limiter failed for client='{}', scope='{}'. Allowing request as fallback. Error: {}",
                    clientIdentifier, actionScope, ex.getMessage());

            return RateLimitResult.builder()
                    .allowed(true)
                    .limit(limitPerMinute)
                    .remaining(limitPerMinute)
                    .resetSeconds(resetSeconds)
                    .build();
        }
    }
}
