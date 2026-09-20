package com.company.eams.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class TestRedisConfig {

    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager("rolePermissions", "departments", "userProfile", "dashboardSummary", "categories");
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate mockTemplate = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> mockValueOps = Mockito.mock(ValueOperations.class);
        when(mockTemplate.opsForValue()).thenReturn(mockValueOps);
        when(mockTemplate.hasKey(anyString())).thenReturn(Boolean.FALSE);
        return mockTemplate;
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> mockTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> mockValueOps = Mockito.mock(ValueOperations.class);
        when(mockTemplate.opsForValue()).thenReturn(mockValueOps);
        return mockTemplate;
    }
}
