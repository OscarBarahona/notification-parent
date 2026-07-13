package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageBodyTest {

    @Test
    void shouldPreserveMessageFormatting() {
        MessageBody message = new MessageBody("  Hello\nWorld  ");

        assertThat(message.value()).isEqualTo("  Hello\nWorld  ");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldRejectMissingMessage(String value) {
        assertThatThrownBy(() -> new MessageBody(value))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MESSAGE_BODY);
    }

    @Test
    void shouldRejectMessageLongerThanMaximum() {
        assertThatThrownBy(() -> new MessageBody("a".repeat(10_001)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("10000");
    }
}
