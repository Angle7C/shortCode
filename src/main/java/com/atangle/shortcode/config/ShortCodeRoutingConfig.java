package com.atangle.shortcode.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ShortCodeRouteProperties.class)
public class ShortCodeRoutingConfig {
}
