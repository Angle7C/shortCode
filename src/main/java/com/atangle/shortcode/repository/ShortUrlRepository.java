package com.atangle.shortcode.repository;

import com.atangle.shortcode.entity.ShortUrlMapping;
import com.atangle.shortcode.routing.RouteTarget;

import java.util.Optional;

public interface ShortUrlRepository {

    void insert(RouteTarget routeTarget, ShortUrlMapping shortUrlMapping);

    Optional<ShortUrlMapping> findByShortCode(RouteTarget routeTarget, String shortCode);

    void increaseAccessCount(RouteTarget routeTarget, String shortCode);
}
