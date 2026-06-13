package com.atangle.shortcode.config;

import com.atangle.shortcode.service.ShortUrlService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ShortUrlCacheConfig {

    @Bean
    public Cache<String, ShortUrlService.ShortUrlCacheValue> shortUrlLocalCache(
            ShortUrlCacheProperties properties
    ) {
        return buildCaffeine(properties).build();
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager(ShortUrlCacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(buildCaffeine(properties));
        return cacheManager;
    }

    private Caffeine<Object, Object> buildCaffeine(ShortUrlCacheProperties properties) {
        return Caffeine.newBuilder()
                .initialCapacity(1_024)
                .maximumSize(properties.getLocalMaximumSize())
                .expireAfterWrite(Duration.ofMinutes(properties.getLocalExpireMinutes()));
    }
}
