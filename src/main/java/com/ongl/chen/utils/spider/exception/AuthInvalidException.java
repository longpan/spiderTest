package com.ongl.chen.utils.spider.exception;

/**
 * 认证失效异常
 * 当检测到认证失效时抛出此异常
 */
public class AuthInvalidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthInvalidException(String message) {
        super(message);
    }

    public AuthInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
