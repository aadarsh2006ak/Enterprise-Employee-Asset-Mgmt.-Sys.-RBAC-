package com.company.eams.resilience.ratelimit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RateLimitResult {
    private final boolean allowed;
    private final long limit;
    private final long remaining;
    private final long resetSeconds;
}
