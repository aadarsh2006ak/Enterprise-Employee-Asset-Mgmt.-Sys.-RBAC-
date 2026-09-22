package com.company.eams.resilience.ratelimit;

import com.company.eams.dto.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final DistributedRateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${eams.resilience.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    @Value("${eams.resilience.rate-limiting.auth-limit-per-minute:10}")
    private long authLimitPerMinute;

    @Value("${eams.resilience.rate-limiting.export-limit-per-minute:5}")
    private long exportLimitPerMinute;

    @Value("${eams.resilience.rate-limiting.general-limit-per-minute:120}")
    private long generalLimitPerMinute;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip Swagger, OpenAPI, and Actuator health endpoints
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitingEnabled || response.isCommitted()) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        String clientIdentifier = resolveClientIdentifier(request);

        String scope;
        long limit;

        if (uri.startsWith("/api/v1/auth/login") || uri.startsWith("/api/v1/auth/refresh")) {
            scope = "auth";
            limit = authLimitPerMinute;
        } else if (uri.startsWith("/api/v1/exports")) {
            scope = "export";
            limit = exportLimitPerMinute;
        } else if (uri.startsWith("/api/v1")) {
            scope = "general";
            limit = generalLimitPerMinute;
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitResult result = rateLimiterService.checkLimit(clientIdentifier, scope, limit);

        // Inject standard RFC RateLimit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));

        if (!result.isAllowed()) {
            log.warn("Rate limit exceeded for client='{}', path='{}', scope='{}'", clientIdentifier, uri, scope);
            response.setHeader("Retry-After", String.valueOf(result.getResetSeconds()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                    .message("Too many requests. Rate limit quota exceeded. Please try again in " + result.getResetSeconds() + " seconds.")
                    .path(uri)
                    .timestamp(Instant.now())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}
