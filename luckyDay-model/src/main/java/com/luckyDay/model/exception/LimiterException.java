package com.luckyDay.model.exception;

public class LimiterException extends RuntimeException {
    public LimiterException(String message) {
        super(message);
    }
}
