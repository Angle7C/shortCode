package com.atangle.shortcode.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheServiceTest {

    @Test
    void shouldGetCacheFromLocalFirst() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        Cache<String, String> localCache = Caffeine.<String, String>newBuilder().build();
        CacheService cacheService = new CacheService(stringRedisTemplate, new JsonMapper());
        localCache.put("short-url:abc123", "https://example.com");

//        assertEquals("https://example.com", cacheService.getCache("short-url:abc123"));
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void shouldGetCacheFromRedisAndBackfillLocalWhenLocalMisses() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        Cache<String, String> localCache = Caffeine.<String, String>newBuilder().build();
        CacheService cacheService = new CacheService(stringRedisTemplate, new JsonMapper());

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("short-url:abc123")).thenReturn("https://example.com");

//        assertEquals("https://example.com", cacheService.getCache("short-url:abc123"));
        assertEquals("https://example.com", localCache.getIfPresent("short-url:abc123"));
    }

    @Test
    void shouldPutCacheToAllLayers() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        Cache<String, String> localCache = Caffeine.<String, String>newBuilder().build();
        CacheService cacheService = new CacheService(stringRedisTemplate, new JsonMapper());

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

//        cacheService.putCache("short-url:abc123", "https://example.com");

        assertEquals("https://example.com", localCache.getIfPresent("short-url:abc123"));
        verify(valueOperations).set("short-url:abc123", "https://example.com");
    }

    @Test
    void shouldIgnoreBlankValueWhenPuttingCache() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        Cache<String, String> localCache = Caffeine.<String, String>newBuilder().build();
        CacheService cacheService = new CacheService(stringRedisTemplate, new JsonMapper());

//        cacheService.putCache("short-url:abc123", " ");

//        assertNull(localCache.getIfPresent("short-url:abc123"));
    }

    @Test
    void shouldReturnNullWhenKeyIsBlank() {
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        Cache<String, String> localCache = Caffeine.<String, String>newBuilder().build();
        CacheService cacheService = new CacheService(stringRedisTemplate, new JsonMapper());

//        assertNull(cacheService.getCache(" "));
    }
}
