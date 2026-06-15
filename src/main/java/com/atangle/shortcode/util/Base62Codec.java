package com.atangle.shortcode.util;

import java.math.BigInteger;

/**
 * Base62 encode/decode utility.
 */
public final class Base62Codec {

    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length);

    private Base62Codec() {
    }

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
        if (value == 0) {
            return String.valueOf(ALPHABET[0]);
        }

        StringBuilder builder = new StringBuilder();
        long current = value;
        while (current > 0) {
            int remainder = (int) (current % ALPHABET.length);
            builder.append(ALPHABET[remainder]);
            current = current / ALPHABET.length;
        }
        return builder.reverse().toString();
    }

    public static long decodeToLong(String value) {
        BigInteger decoded = decode(value);
        if (decoded.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("decoded value exceeds long range");
        }
        return decoded.longValue();
    }

    public static BigInteger decode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < value.length(); i++) {
            int index = indexOf(value.charAt(i));
            if (index < 0) {
                throw new IllegalArgumentException("invalid base62 character: " + value.charAt(i));
            }
            result = result.multiply(BASE).add(BigInteger.valueOf(index));
        }
        return result;
    }

    private static int indexOf(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'A' && ch <= 'Z') {
            return ch - 'A' + 10;
        }
        if (ch >= 'a' && ch <= 'z') {
            return ch - 'a' + 36;
        }
        return -1;
    }
}
