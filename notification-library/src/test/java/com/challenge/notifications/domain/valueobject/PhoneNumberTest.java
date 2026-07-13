package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTest {

    @Test
    void shouldAcceptE164PhoneNumber() {
        PhoneNumber phoneNumber = new PhoneNumber("  +50370000000  ");

        assertThat(phoneNumber.value()).isEqualTo("+50370000000");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "50370000000", "+012345678", "+123", "+1234567890123456"})
    void shouldRejectInvalidPhoneNumber(String value) {
        assertThatThrownBy(() -> new PhoneNumber(value))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PHONE_NUMBER);
    }
}
