package com.atangle.shortcode.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PiException.class)
    public Resp<Void> handlePiException(PiException ex) {
        log.warn("Business exception, code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return Resp.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Resp<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return Resp.fail(500, ex.getMessage());
    }
}
