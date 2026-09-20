package com.company.eams.resilience.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private DistributedRateLimiterService rateLimiterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(rateLimiterService);
        ReflectionTestUtils.setField(filter, "rateLimitingEnabled", true);
        ReflectionTestUtils.setField(filter, "authLimitPerMinute", 10L);
        ReflectionTestUtils.setField(filter, "exportLimitPerMinute", 5L);
        ReflectionTestUtils.setField(filter, "generalLimitPerMinute", 120L);
    }

    @Test
    @DisplayName("Allows request when under rate limit quota")
    void testRequestAllowedWhenUnderQuota() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/assets");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        RateLimitResult allowedResult = RateLimitResult.builder()
                .allowed(true)
                .limit(120L)
                .remaining(119L)
                .resetSeconds(55L)
                .build();

        when(rateLimiterService.checkLimit(eq("192.168.1.100"), eq("general"), eq(120L)))
                .thenReturn(allowedResult);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-RateLimit-Limit", "120");
        verify(response).setHeader("X-RateLimit-Remaining", "119");
        verify(response).setHeader("X-RateLimit-Reset", "55");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Blocks request with 429 Too Many Requests when rate limit exceeded")
    void testRequestBlockedWhenLimitExceeded() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        when(response.getWriter()).thenReturn(printWriter);

        RateLimitResult blockedResult = RateLimitResult.builder()
                .allowed(false)
                .limit(10L)
                .remaining(0L)
                .resetSeconds(45L)
                .build();

        when(rateLimiterService.checkLimit(eq("192.168.1.100"), eq("auth"), eq(10L)))
                .thenReturn(blockedResult);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(429);
        verify(response).setHeader("Retry-After", "45");
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(out.toString().contains("Rate limit quota exceeded"));
    }

    @Test
    @DisplayName("Skips rate limiting when rate-limiting is disabled")
    void testSkipsWhenDisabled() throws ServletException, IOException {
        ReflectionTestUtils.setField(filter, "rateLimitingEnabled", false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(rateLimiterService, never()).checkLimit(anyString(), anyString(), anyLong());
    }
}
