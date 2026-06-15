package com.atangle.shortcode.web;

import com.atangle.shortcode.common.Resp;
import com.atangle.shortcode.service.ShortUrlService;
import com.atangle.shortcode.web.dto.CreateShortUrlRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/short-urls")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    public Resp<ShortUrlService.CreateShortUrlResult> createShortUrl(@RequestBody CreateShortUrlRequest request) {
        log.info("Receive create short url request, originUrl={}", request.getOriginUrl());
        return Resp.success(shortUrlService.createShortUrl(
                request.getOriginUrl(),
                request.getExpireDays(),
                request.getCreator()
        ));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> accessShortUrl(@PathVariable String shortCode) {
        String originUrl = shortUrlService.getOriginUrlAndIncreaseAccessCount(shortCode);
        if (originUrl == null) {
            log.info("Short url not found for redirect, shortCode={}", shortCode);
            return ResponseEntity.notFound().build();
        }
        log.info("Redirect shortCode={} to originUrl={}", shortCode, originUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, URI.create(originUrl).toString())
                .build();
    }
}
