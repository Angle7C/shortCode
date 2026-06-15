package com.atangle.shortcode.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class BloomFilterService {

    private static final String BLOOM_PREFIX = "shortLink:bloom:";
    private static final long DEFAULT_BITMAP_SIZE = 1L << 24;
    private static final int DEFAULT_HASH_COUNT = 6;

    private final RedisTemplate<String, String> redisTemplate;

    public BloomFilterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(String filterName, String value) {
        Assert.hasText(filterName, "filterName must not be blank");
        Assert.hasText(value, "value must not be blank");

        String redisKey = buildRedisKey(filterName);
        for (long index : hashIndexes(value)) {
            redisTemplate.opsForValue().setBit(redisKey, index, true);
        }
    }

    public boolean mightContain(String filterName, String value) {
        Assert.hasText(filterName, "filterName must not be blank");
        Assert.hasText(value, "value must not be blank");

        String redisKey = buildRedisKey(filterName);
        for (long index : hashIndexes(value)) {
            Boolean bit = redisTemplate.opsForValue().getBit(redisKey, index);
            if (!Boolean.TRUE.equals(bit)) {
                return false;
            }
        }
        return true;
    }

    private long[] hashIndexes(String value) {
        byte[] digest = md5(value.trim());
        long hash1 = toPositiveLong(digest, 0);
        long hash2 = toPositiveLong(digest, 8);

        long[] indexes = new long[DEFAULT_HASH_COUNT];
        for (int i = 0; i < DEFAULT_HASH_COUNT; i++) {
            long combinedHash = hash1 + (long) i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            indexes[i] = combinedHash % DEFAULT_BITMAP_SIZE;
        }
        return indexes;
    }

    private byte[] md5(String value) {
        try {
            return MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    private long toPositiveLong(byte[] bytes, int offset) {
        long result = 0L;
        for (int i = 0; i < Long.BYTES; i++) {
            result = (result << 8) | (bytes[offset + i] & 0xffL);
        }
        return result & Long.MAX_VALUE;
    }

    private String buildRedisKey(String filterName) {
        return BLOOM_PREFIX + filterName.trim();
    }
}
