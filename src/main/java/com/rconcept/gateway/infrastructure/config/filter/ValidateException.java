package com.rconcept.gateway.infrastructure.config.filter;

/**
 * @author LZx
 * @since 2020/12/31
 */
public class ValidateException extends RuntimeException {

    public ValidateException(String message) {
        super(message);
    }

}
