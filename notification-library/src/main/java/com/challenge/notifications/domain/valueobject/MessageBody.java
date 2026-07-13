package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;

public record MessageBody(String value) {

    private static final int MAX_LENGTH = 10_000;

    public MessageBody {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    ErrorCode.INVALID_MESSAGE_BODY,
                    "Message body cannot be null or blank"
            );
        }

        if (value.length() > MAX_LENGTH) {
            throw new ValidationException(
                    ErrorCode.INVALID_MESSAGE_BODY,
                    "Message body cannot exceed 10000 characters"
            );
        }
    }
}
