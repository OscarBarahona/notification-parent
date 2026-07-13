package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;

import java.util.UUID;

public record NotificationId(UUID value) {

    public NotificationId {
        if (value == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION_ID,
                    "Notification id cannot be null"
            );
        }
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    public static NotificationId from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION_ID,
                    "Notification id cannot be null or blank"
            );
        }

        try {
            return new NotificationId(UUID.fromString(rawValue.trim()));
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION_ID,
                    "Notification id must be a valid UUID",
                    exception
            );
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
