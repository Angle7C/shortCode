package com.atangle.shortcode.web.dto;

public class ShortUrlDetailResponse {

    private final String shortCode;
    private final String originUrl;

    public ShortUrlDetailResponse(String shortCode, String originUrl) {
        this.shortCode = shortCode;
        this.originUrl = originUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginUrl() {
        return originUrl;
    }
}
