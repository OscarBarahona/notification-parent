package com.challenge.notifications.domain.exception;

import com.challenge.notifications.domain.enums.ErrorCode;

public final class ConfigurationException extends NotificationException {

    public ConfigurationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ConfigurationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
