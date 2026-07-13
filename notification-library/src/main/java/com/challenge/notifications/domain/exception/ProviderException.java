package com.challenge.notifications.domain.exception;

import com.challenge.notifications.domain.enums.ErrorCode;

public final class ProviderException extends NotificationException {

    public ProviderException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ProviderException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
