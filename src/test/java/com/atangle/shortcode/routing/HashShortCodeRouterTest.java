package com.atangle.shortcode.routing;

import com.atangle.shortcode.config.ShortCodeRouteProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HashShortCodeRouterTest {

    private final HashShortCodeRouter router = new HashShortCodeRouter(new ShortCodeRouteProperties());

    @Test
    void shouldReturnStableRouteForSameShortCode() {
        RouteTarget first = router.routeByShortCode("abc12345");
        RouteTarget second = router.routeByShortCode("abc12345");

        assertEquals(first, second);
        assertNotNull(first.qualifiedTableName());
    }

    @Test
    void shouldUseConfiguredSchemaAndTableNameFormat() {
        RouteTarget routeTarget = router.routeByShortCode("xyz98765");

        assertEquals("code_%02d".formatted(routeTarget.schemaIndex()), routeTarget.schemaName());
        assertEquals("short_url_mapping_%02d".formatted(routeTarget.tableIndex()), routeTarget.tableName());
    }

    @Test
    void shouldUseShortCodeHashCodeForSchemaAndTableRouting() {
        String shortCode = "shortA01";
        int hash = positiveHash(shortCode.hashCode());

        RouteTarget routeTarget = router.routeByShortCode(shortCode);

        assertEquals(hash % 16, routeTarget.schemaIndex());
        assertEquals(hash % 64, routeTarget.tableIndex());
    }

    @Test
    void shouldHandleMinIntegerHashAsNonNegative() {
        assertEquals(0, positiveHash(Integer.MIN_VALUE));
    }

    private int positiveHash(int hash) {
        return hash == Integer.MIN_VALUE ? 0 : Math.abs(hash);
    }
}
