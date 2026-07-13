package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailAddressTest {

    @Test
    void shouldNormalizeValidEmail() {
        EmailAddress email = new EmailAddress("  USER.Name+tag@Example.COM  ");

        assertThat(email.value()).isEqualTo("user.name+tag@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "plain-address", "user@", "@example.com", "user@example"})
    void shouldRejectInvalidEmail(String value) {
        assertThatThrownBy(() -> new EmailAddress(value))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);
    }

    @Test
    void shouldRejectEmailLongerThan254Characters() {
        String email = "a".repeat(245) + "@example.com";

        assertThatThrownBy(() -> new EmailAddress(email))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email address");
    }
}
