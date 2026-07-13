package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;

import java.util.Objects;

public record DeviceToken(String value) {

    private static final int MIN_LENGTH = 16;
    private static final int MAX_LENGTH = 4096;

    public DeviceToken {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    ErrorCode.INVALID_DEVICE_TOKEN,
                    "Device token cannot be null or blank"
            );
        }

        value = value.trim();

        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new ValidationException(
                    ErrorCode.INVALID_DEVICE_TOKEN,
                    "Device token length must be between 16 and 4096 characters"
            );
        }
    }
}
