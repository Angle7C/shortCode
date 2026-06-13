package com.atangle.shortcode.service;

import com.atangle.shortcode.common.PiException;
import com.atangle.shortcode.entity.ShortUrlMapping;
import com.atangle.shortcode.repository.ShortUrlRepository;
import com.atangle.shortcode.routing.RouteTarget;
import com.atangle.shortcode.routing.ShortCodeRouter;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ShortUrlService {

    private static final String SHORT_CODE_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SHORT_CODE_LENGTH = 8;
    private static final short STATUS_ENABLED = 1;

    private final ShortCodeRouter shortCodeRouter;
    private final ShortUrlRepository shortUrlRepository;
    private final Cache<String, ShortUrlCacheValue> shortUrlLocalCache;

    public ShortUrlService(
            ShortCodeRouter shortCodeRouter,
            ShortUrlRepository shortUrlRepository,
            Cache<String, ShortUrlCacheValue> shortUrlLocalCache
    ) {
        this.shortCodeRouter = shortCodeRouter;
        this.shortUrlRepository = shortUrlRepository;
        this.shortUrlLocalCache = shortUrlLocalCache;
    }

    public CreateShortUrlResult createShortUrl(String originUrl, Integer expireDays, String creator) {
        Assert.hasText(originUrl, "originUrl must not be blank");

        String normalizedOriginUrl = originUrl.trim();
        String shortCode = generateShortCode();
        RouteTarget routeTarget = shortCodeRouter.routeByShortCode(shortCode);
        LocalDateTime now = LocalDateTime.now();

        ShortUrlMapping shortUrlMapping = new ShortUrlMapping();
        shortUrlMapping.setShortCode(shortCode);
        shortUrlMapping.setOriginUrl(normalizedOriginUrl);
        shortUrlMapping.setOriginUrlHash(md5(normalizedOriginUrl));
        shortUrlMapping.setCreateTime(now);
        shortUrlMapping.setUpdateTime(now);
        shortUrlMapping.setExpireDays(expireDays);
        shortUrlMapping.setAccessCount(0L);
        shortUrlMapping.setStatus(STATUS_ENABLED);
        shortUrlMapping.setCreator(StringUtils.hasText(creator) ? creator.trim() : null);

        shortUrlRepository.insert(routeTarget, shortUrlMapping);
        shortUrlLocalCache.put(shortCode, new ShortUrlCacheValue(normalizedOriginUrl, STATUS_ENABLED));
        log.info("Create short url, shortCode={}, schema={}, table={}",
                shortCode,
                routeTarget.schemaName(),
                routeTarget.tableName());
        return new CreateShortUrlResult(shortCode, normalizedOriginUrl, routeTarget.schemaName(), routeTarget.tableName());
    }

    public String getOriginUrlAndIncreaseAccessCount(String shortCode) {
        Assert.hasText(shortCode, "shortCode must not be blank");

        String normalizedShortCode = shortCode.trim();
        RouteTarget routeTarget = shortCodeRouter.routeByShortCode(normalizedShortCode);
        ShortUrlCacheValue cacheValue = shortUrlLocalCache.getIfPresent(normalizedShortCode);

        if (cacheValue != null) {
            validateStatus(cacheValue.status());
            shortUrlRepository.increaseAccessCount(routeTarget, normalizedShortCode);
            log.info("Access short url from local cache, shortCode={}, schema={}, table={}",
                    normalizedShortCode,
                    routeTarget.schemaName(),
                    routeTarget.tableName());
            return cacheValue.originUrl();
        }

        Optional<ShortUrlMapping> mappingOptional = shortUrlRepository.findByShortCode(routeTarget, normalizedShortCode);
        ShortUrlMapping mapping = mappingOptional.orElseThrow(() -> new PiException(4004, "短链不存在"));
        validateStatus(mapping.getStatus());

        shortUrlLocalCache.put(normalizedShortCode, new ShortUrlCacheValue(mapping.getOriginUrl(), mapping.getStatus()));
        shortUrlRepository.increaseAccessCount(routeTarget, normalizedShortCode);
        log.info("Access short url from database, shortCode={}, schema={}, table={}",
                normalizedShortCode,
                routeTarget.schemaName(),
                routeTarget.tableName());
        return mapping.getOriginUrl();
    }

    private void validateStatus(Short status) {
        if (status == null || status != STATUS_ENABLED) {
            throw new PiException(4005, "短链不可访问");
        }
    }

    private String generateShortCode() {
        StringBuilder builder = new StringBuilder(SHORT_CODE_LENGTH);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            int index = random.nextInt(SHORT_CODE_ALPHABET.length());
            builder.append(SHORT_CODE_ALPHABET.charAt(index));
        }
        return builder.toString();
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }

    public record CreateShortUrlResult(String shortCode, String originUrl, String schemaName, String tableName) {
    }

    public record ShortUrlCacheValue(String originUrl, Short status) {
    }
}
