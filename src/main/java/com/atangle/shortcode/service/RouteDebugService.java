package com.atangle.shortcode.service;

import com.atangle.shortcode.routing.RouteTarget;
import com.atangle.shortcode.routing.SchemaRoutingExecutor;
import com.atangle.shortcode.routing.ShortCodeRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RouteDebugService {
    
    private final ShortCodeRouter shortCodeRouter;
    private final SchemaRoutingExecutor schemaRoutingExecutor;

    public RouteDebugService(ShortCodeRouter shortCodeRouter, SchemaRoutingExecutor schemaRoutingExecutor) {
        this.shortCodeRouter = shortCodeRouter;
        this.schemaRoutingExecutor = schemaRoutingExecutor;
    }

    public RouteDebugResult inspectByShortCode(String shortCode) {
        RouteTarget routeTarget = shortCodeRouter.routeByShortCode(shortCode);
        String currentSchema = schemaRoutingExecutor.executeOn(routeTarget, connection -> connection.getSchema());
        log.info("Route shortCode={}, schema={}, table={}",
                shortCode,
                routeTarget.schemaName(),
                routeTarget.tableName());
        return new RouteDebugResult(routeTarget, currentSchema);
    }

    public record RouteDebugResult(RouteTarget routeTarget, String currentSchema) {
    }
}
