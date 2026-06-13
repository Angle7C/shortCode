package com.atangle.shortcode.web;

import com.atangle.shortcode.common.Resp;
import com.atangle.shortcode.service.RouteDebugService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/routing")
public class RoutingDebugController {

    private final RouteDebugService routeDebugService;

    public RoutingDebugController(RouteDebugService routeDebugService) {
        this.routeDebugService = routeDebugService;
    }

    @GetMapping("/short-code")
    public Resp<RouteDebugService.RouteDebugResult> routeByShortCode(@RequestParam String shortCode) {
        log.info("Receive route debug request, shortCode={}", shortCode);
        return Resp.success(routeDebugService.inspectByShortCode(shortCode));
    }
}
