package com.atangle.shortcode.routing;

import com.atangle.shortcode.config.ShortCodeRouteProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class HashShortCodeRouter implements ShortCodeRouter {

    private final ShortCodeRouteProperties properties;

    public HashShortCodeRouter(ShortCodeRouteProperties properties) {
        this.properties = properties;
    }

    @Override
    public RouteTarget routeByShortCode(String shortCode) {
        Assert.hasText(shortCode, "shortCode must not be blank");
        return route(shortCode.trim());
    }

    private RouteTarget route(String shortCode) {
        // Route by shortCode hash so the same shortCode always lands on the same schema/table.
        int hash = positiveHash(shortCode);
        int schemaCount = properties.getSchemaCount();
        int tableCountPerSchema = properties.getTableCountPerSchema();

        int schemaIndex = hash % schemaCount;
        int tableIndex = hash % tableCountPerSchema;

        return new RouteTarget(
                schemaIndex,
                tableIndex,
                formatName(properties.getSchemaPrefix(), schemaIndex),
                formatName(properties.getTablePrefix(), tableIndex)
        );
    }

    private String formatName(String prefix, int index) {
        return prefix + String.format("%02d", index);
    }

    private int positiveHash(String shortCode) {
        int hash = shortCode.hashCode();
        // Math.abs(Integer.MIN_VALUE) is still negative, so guard that edge case explicitly.
        return hash == Integer.MIN_VALUE ? 0 : Math.abs(hash);
    }
}
