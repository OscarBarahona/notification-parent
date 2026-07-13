package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final int MAX_LENGTH = 254;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE
    );

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    ErrorCode.INVALID_EMAIL,
                    "Email address cannot be null or blank"
            );
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.length() > MAX_LENGTH || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                    ErrorCode.INVALID_EMAIL,
                    "Invalid email address: " + value
            );
        }
    }
}
