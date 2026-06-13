package com.atangle.shortcode.service;

import com.atangle.shortcode.entity.ShortUrlMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Optional;

/**
 * 缓存服务，统一封装本地缓存与远程缓存访问。
 */
@Slf4j
@Service
@CacheConfig(cacheNames = CacheService.SHORT_URL_CACHE_NAME)
public class CacheService {

    public static final String SHORT_URL_CACHE_NAME = "shortUrlMappingCache";

    private static final String CACHE_PREFIX = "shortLink:";
    private static final String URL_CACHE_PREFIX = CACHE_PREFIX + "url:";
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取远程缓存中的值。
     *
     * @param key 缓存 key
     * @return 缓存值，不存在时返回空
     */
    public Optional<ShortUrlMapping> getRemoteCache(String key) {
        Assert.hasText(key, "key must not be blank");

        String cacheKey = buildUrlCacheKey(key);
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(json, ShortUrlMapping.class));
        } catch (Exception e) {
            log.error("Load remote cache failed, key={}", cacheKey, e);
            return Optional.empty();
        }
    }

    /**
     * 按照本地优先、远程兜底的方式获取缓存值。
     *
     * <p>当本地 Caffeine 未命中时，会自动回源 Redis，并将结果写回本地缓存。</p>
     *
     * @param key 缓存 key
     * @return 缓存值，不存在时返回空
     */
    @Cacheable(key = "#key")
    public Optional<ShortUrlMapping> getLocalRemoteCache(String key) {
        Assert.hasText(key, "key must not be blank");
        log.debug("Local cache miss, fallback to remote cache, key={}", key);
        return getRemoteCache(key);
    }

    /**
     * 写入本地缓存。
     */
    @CachePut(key = "#mapping.shortCode")
    public Optional<ShortUrlMapping> putLocalCache(ShortUrlMapping mapping) {
        Assert.notNull(mapping, "mapping must not be null");
        Assert.hasText(mapping.getShortCode(), "shortCode must not be blank");
        return Optional.of(mapping);
    }

    /**
     * 删除本地缓存。
     */
    @CacheEvict(key = "#key")
    public void evictLocalCache(String key) {
        Assert.hasText(key, "key must not be blank");
    }

    /**
     * 写入远程缓存。
     */
    public void putRemoteCache(ShortUrlMapping mapping) {
        Assert.notNull(mapping, "mapping must not be null");
        Assert.hasText(mapping.getShortCode(), "shortCode must not be blank");

        String cacheKey = buildUrlCacheKey(mapping.getShortCode());
        try {
            String json = objectMapper.writeValueAsString(mapping);
            redisTemplate.opsForValue().set(cacheKey, json, DEFAULT_EXPIRE_TIME);
        } catch (Exception e) {
            log.error("Save remote cache failed, key={}", cacheKey, e);
        }
    }

    private String buildUrlCacheKey(String shortCode) {
        return URL_CACHE_PREFIX + shortCode.trim();
    }
}
