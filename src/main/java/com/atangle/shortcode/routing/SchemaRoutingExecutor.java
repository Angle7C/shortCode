package com.atangle.shortcode.routing;

import org.springframework.jdbc.core.ConnectionCallback;

public interface SchemaRoutingExecutor {

    <T> T executeOn(RouteTarget routeTarget, ConnectionCallback<T> action);
}
