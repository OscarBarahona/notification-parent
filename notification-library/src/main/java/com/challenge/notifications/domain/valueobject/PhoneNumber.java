package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;

import java.util.Objects;
import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    ErrorCode.INVALID_PHONE_NUMBER,
                    "Phone number cannot be null or blank"
            );
        }

        value = value.trim();

        if (!E164_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                    ErrorCode.INVALID_PHONE_NUMBER,
                    "Phone number must use E.164 format, for example +50370000000"
            );
        }
    }
}
