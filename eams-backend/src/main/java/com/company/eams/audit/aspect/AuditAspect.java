package com.company.eams.audit.aspect;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.audit.dto.AuditLogEvent;
import com.company.eams.audit.service.AsyncAuditWriter;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.security.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class AuditAspect {

    private final AsyncAuditWriter asyncAuditWriter;
    private final ObjectMapper objectMapper;

    public AuditAspect(AsyncAuditWriter asyncAuditWriter) {
        this.asyncAuditWriter = asyncAuditWriter;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // 1. Capture request context & caller
        HttpServletRequest request = getCurrentHttpRequest();
        String ipAddress = extractClientIp(request);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        String username = "ANONYMOUS";

        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            userId = principal.getId();
            username = principal.getUsername();
        }

        // 2. Pre-execution argument capture
        Map<String, Object> oldValue = extractArgumentsMap(joinPoint);

        // 3. Execute target method
        Object result = joinPoint.proceed();

        // 4. Post-execution result capture & asynchronous dispatch
        try {
            Map<String, Object> newValue = extractResultMap(result);
            Long entityId = extractEntityId(result, joinPoint.getArgs());

            // Handle login event specifically to capture user info from response
            if (result instanceof AuthResponse authResponse && authResponse.getUser() != null) {
                userId = authResponse.getUser().getId();
                username = authResponse.getUser().getUsername();
                entityId = userId;
            }

            AuditLogEvent event = AuditLogEvent.builder()
                    .userId(userId)
                    .usernameSnapshot(username)
                    .entityName(auditable.entityName())
                    .entityId(entityId)
                    .action(auditable.action())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .createdAt(Instant.now())
                    .build();

            asyncAuditWriter.writeAuditLog(event);

        } catch (Exception ex) {
            log.warn("Error capturing audit log for method {}: {}", joinPoint.getSignature().getName(), ex.getMessage());
        }

        return result;
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // In multi-proxy setups, first IP is the client
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    private Map<String, Object> extractArgumentsMap(ProceedingJoinPoint joinPoint) {
        Map<String, Object> map = new HashMap<>();
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    String name = paramNames[i];
                    Object arg = args[i];
                    // Skip sensitive fields or framework objects
                    if (arg instanceof HttpServletRequest || name.toLowerCase().contains("password") || name.toLowerCase().contains("token")) {
                        continue;
                    }
                    if (arg != null) {
                        try {
                            map.put(name, objectMapper.convertValue(arg, new TypeReference<Object>() {}));
                        } catch (Exception e) {
                            map.put(name, arg.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Unable to parse method arguments: {}", e.getMessage());
        }
        return map;
    }

    private Map<String, Object> extractResultMap(Object result) {
        if (result == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(result, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("result", result.toString());
            return fallback;
        }
    }

    private Long extractEntityId(Object result, Object[] args) {
        // 1. Try finding 'id' from result object (e.g. Response DTO or Entity)
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object idVal = getIdMethod.invoke(result);
                if (idVal instanceof Long l) return l;
                if (idVal instanceof Number n) return n.longValue();
            } catch (Exception ignored) {}

            try {
                Field idField = result.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object idVal = idField.get(result);
                if (idVal instanceof Long l) return l;
                if (idVal instanceof Number n) return n.longValue();
            } catch (Exception ignored) {}
        }

        // 2. Try finding ID from first argument if it's a Long (e.g. deleteDepartment(Long id))
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long l) return l;
            }
        }

        return null;
    }
}
