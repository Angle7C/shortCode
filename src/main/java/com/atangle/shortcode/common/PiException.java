package com.atangle.shortcode.common;

public class PiException extends RuntimeException {

    private final int code;

    public PiException(String message) {
        this(500, message);
    }

    public PiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public PiException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
