package com.challenge.notifications.domain.valueobject;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationIdTest {

    @Test
    void shouldGenerateNotificationId() {
        NotificationId id = NotificationId.generate();

        assertThat(id.value()).isNotNull();
        assertThat(id.toString()).isEqualTo(id.value().toString());
    }

    @Test
    void shouldCreateFromString() {
        UUID uuid = UUID.randomUUID();

        NotificationId id = NotificationId.from("  " + uuid + "  ");

        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void shouldRejectNullUuid() {
        assertThatThrownBy(() -> new NotificationId(null))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_NOTIFICATION_ID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-uuid"})
    void shouldRejectInvalidText(String value) {
        assertThatThrownBy(() -> NotificationId.from(value))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_NOTIFICATION_ID);
    }
}
