package com.atangle.shortcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "short-code.cache")
public class ShortUrlCacheProperties {

    private long localMaximumSize = 10_000;

    private long localExpireMinutes = 30;

    public long getLocalMaximumSize() {
        return localMaximumSize;
    }

    public void setLocalMaximumSize(long localMaximumSize) {
        this.localMaximumSize = localMaximumSize;
    }

    public long getLocalExpireMinutes() {
        return localExpireMinutes;
    }

    public void setLocalExpireMinutes(long localExpireMinutes) {
        this.localExpireMinutes = localExpireMinutes;
    }
}
