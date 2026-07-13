package com.challenge.notifications.domain.exception;

import com.challenge.notifications.domain.enums.ErrorCode;

public final class ValidationException extends NotificationException {

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}