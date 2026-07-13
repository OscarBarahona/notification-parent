package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;

public record Subject(String value) {

    private static final int MAX_LENGTH = 200;

    public Subject {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    ErrorCode.INVALID_SUBJECT,
                    "Subject cannot be null or blank"
            );
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new ValidationException(
                    ErrorCode.INVALID_SUBJECT,
                    "Subject cannot exceed 200 characters"
            );
        }
    }
}
