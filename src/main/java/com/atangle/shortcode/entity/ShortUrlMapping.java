package com.atangle.shortcode.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ShortUrlMapping {

    /**
     * Fixed-length short code, primary key.
     */
    private String shortCode;

    /**
     * Original long URL.
     */
    private String originUrl;

    /**
     * MD5 hash of the original URL.
     */
    private String originUrlHash;

    /**
     * Record creation time.
     */
    private LocalDateTime createTime;

    /**
     * Record last update time.
     */
    private LocalDateTime updateTime;

    /**
     * Expiration period in days.
     */
    private Integer expireDays;

    /**
     * Total access count.
     */
    private Long accessCount;

    /**
     * Status: 1-enabled, 0-disabled, -1-expired.
     */
    private Short status;

    /**
     * Record creator.
     */
    private String creator;

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getOriginUrlHash() {
        return originUrlHash;
    }

    public void setOriginUrlHash(String originUrlHash) {
        this.originUrlHash = originUrlHash;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(Integer expireDays) {
        this.expireDays = expireDays;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
