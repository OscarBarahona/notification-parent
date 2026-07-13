package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceTokenTest {

    @Test
    void shouldAcceptAndTrimToken() {
        DeviceToken token = new DeviceToken("  abcdefghijklmnop  ");

        assertThat(token.value()).isEqualTo("abcdefghijklmnop");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "short-token"})
    void shouldRejectMissingOrShortToken(String value) {
        assertThatThrownBy(() -> new DeviceToken(value))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_DEVICE_TOKEN);
    }

    @Test
    void shouldRejectTokenLongerThanMaximum() {
        assertThatThrownBy(() -> new DeviceToken("a".repeat(4097)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("between 16 and 4096");
    }
}
