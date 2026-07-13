package com.challenge.notifications.domain.exception;

import com.challenge.notifications.domain.enums.ErrorCode;

import java.util.Objects;

public abstract class NotificationException extends RuntimeException {

    private final ErrorCode errorCode;

    protected NotificationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
    }

    protected NotificationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}