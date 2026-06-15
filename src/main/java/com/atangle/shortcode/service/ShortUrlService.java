package com.atangle.shortcode.service;

import com.atangle.shortcode.common.PiException;
import com.atangle.shortcode.entity.ShortUrlMapping;
import com.atangle.shortcode.repository.ShortUrlRepository;
import com.atangle.shortcode.routing.RouteTarget;
import com.atangle.shortcode.routing.ShortCodeRouter;
import com.atangle.shortcode.util.Base62Codec;
import com.atangle.shortcode.util.SnowflakeIdGenerator;
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
    private final CacheService cacheService;
    SnowflakeIdGenerator generator=new SnowflakeIdGenerator(1,1);
    public ShortUrlService(
            ShortCodeRouter shortCodeRouter,
            ShortUrlRepository shortUrlRepository,
            CacheService cacheService
    ) {
        this.shortCodeRouter = shortCodeRouter;
        this.shortUrlRepository = shortUrlRepository;
        this.cacheService = cacheService;
    }

    public CreateShortUrlResult createShortUrl(String originUrl, Integer expireDays, String creator) {

        String shortCode = generateShortCode();
        RouteTarget routeTarget = shortCodeRouter.routeByShortCode(shortCode);
        Optional<ShortUrlMapping> existingMapping = shortUrlRepository.findByShortCode(routeTarget, shortCode);

        if (existingMapping.isPresent()) {
            ShortUrlMapping mapping = existingMapping.get();
            cacheService.putLocalCache(mapping);
            cacheService.putRemoteCache(mapping);
            log.info("Short url already exists, warm cache only, shortCode={}, schema={}, table={}",
                    shortCode,
                    routeTarget.schemaName(),
                    routeTarget.tableName());
            return new CreateShortUrlResult(
                    mapping.getShortCode(),
                    mapping.getOriginUrl(),
                    routeTarget.schemaName(),
                    routeTarget.tableName()
            );
        }

        LocalDateTime now = LocalDateTime.now();

        ShortUrlMapping shortUrlMapping = new ShortUrlMapping();
        shortUrlMapping.setShortCode(shortCode);
        shortUrlMapping.setOriginUrl(originUrl);
        shortUrlMapping.setOriginUrlHash(md5(originUrl));
        shortUrlMapping.setCreateTime(now);
        shortUrlMapping.setUpdateTime(now);
        shortUrlMapping.setExpireDays(expireDays);
        shortUrlMapping.setAccessCount(0L);
        shortUrlMapping.setStatus(STATUS_ENABLED);
        shortUrlMapping.setCreator(StringUtils.hasText(creator) ? creator.trim() : null);

        shortUrlRepository.insert(routeTarget, shortUrlMapping);
        cacheService.putLocalCache(shortUrlMapping);
        cacheService.putRemoteCache(shortUrlMapping);
        log.info("Create short url, shortCode={}, schema={}, table={}",
                shortCode,
                routeTarget.schemaName(),
                routeTarget.tableName());
        return new CreateShortUrlResult(shortCode, originUrl, routeTarget.schemaName(), routeTarget.tableName());
    }

    public String getOriginUrlAndIncreaseAccessCount(String shortCode) {
        Assert.hasText(shortCode, "shortCode must not be blank");

        RouteTarget routeTarget = shortCodeRouter.routeByShortCode(shortCode);
        Optional<ShortUrlMapping> cachedMapping = cacheService.getLocalRemoteCache(shortCode);

        if (cachedMapping.isPresent()) {
            ShortUrlMapping mapping = cachedMapping.get();
            validateStatus(mapping.getStatus());
            shortUrlRepository.increaseAccessCount(routeTarget, shortCode);
            log.info("Access short url from cache, shortCode={}, schema={}, table={}",
                    shortCode,
                    routeTarget.schemaName(),
                    routeTarget.tableName());
            return mapping.getOriginUrl();
        }

        Optional<ShortUrlMapping> mappingOptional = shortUrlRepository.findByShortCode(routeTarget, shortCode);
        if (mappingOptional.isEmpty()) {
            log.info("Short url not found, shortCode={}, schema={}, table={}",
                    shortCode,
                    routeTarget.schemaName(),
                    routeTarget.tableName());
            return null;
        }

        ShortUrlMapping mapping = mappingOptional.get();
        validateStatus(mapping.getStatus());

        cacheService.putLocalCache(mapping);
        cacheService.putRemoteCache(mapping);
        shortUrlRepository.increaseAccessCount(routeTarget, shortCode);
        log.info("Access short url from database, shortCode={}, schema={}, table={}",
                shortCode,
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


        return Base62Codec.encode(generator.nextId());
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }

    public record CreateShortUrlResult(String shortCode, String originUrl, String schemaName, String tableName) {
    }

    public record ShortUrlCacheValue(String originUrl, Short status) {
    }
}
