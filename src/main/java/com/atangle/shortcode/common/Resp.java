package com.atangle.shortcode.common;

import lombok.Getter;

@Getter
public class Resp<T> {

    private static final int SUCCESS_CODE = 0;

    private final int code;

    private final String message;

    private final T data;

    private Resp(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Resp<T> success(T data) {
        return new Resp<>(SUCCESS_CODE, "success", data);
    }

    public static <T> Resp<T> success(String message, T data) {
        return new Resp<>(SUCCESS_CODE, message, data);
    }

    public static <T> Resp<T> fail(int code, String message) {
        return new Resp<>(code, message, null);
    }

}
