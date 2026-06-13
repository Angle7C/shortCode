package com.atangle.shortcode.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@EnableConfigurationProperties({ShortCodeRouteProperties.class, ShortUrlCacheProperties.class})
public class ShortCodeRoutingConfig {
}
