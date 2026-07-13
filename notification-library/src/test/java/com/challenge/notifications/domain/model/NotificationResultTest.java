package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.enums.NotificationStatus;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.NotificationId;
import com.challenge.notifications.domain.valueobject.Subject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationResultTest {

    private final EmailNotification notification = EmailNotification.create(
            new EmailAddress("user@example.com"),
            new Subject("Welcome"),
            new MessageBody("Hello")
    );

    @Test
    void shouldCreateSuccessfulResult() {
        NotificationResult result = NotificationResult.sent(
                notification,
                "  SendGrid  ",
                "  Accepted by provider  "
        );

        assertThat(result.notificationId()).isEqualTo(notification.id());
        assertThat(result.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.provider()).isEqualTo("SendGrid");
        assertThat(result.detail()).isEqualTo("Accepted by provider");
        assertThat(result.errorCode()).isEmpty();
        assertThat(result.occurredAt()).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void shouldCreateFailedResult() {
        NotificationResult result = NotificationResult.failed(
                notification,
                "SendGrid",
                "Provider rejected request",
                ErrorCode.PROVIDER_ERROR
        );

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.errorCode()).contains(ErrorCode.PROVIDER_ERROR);
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void shouldRejectInvalidMandatoryFields() {
        Instant now = Instant.now();
        NotificationId id = notification.id();

        assertInvalid(() -> new NotificationResult(
                null, NotificationChannel.EMAIL, NotificationStatus.SENT,
                "SendGrid", now, "ok", Optional.empty()
        ));
        assertInvalid(() -> new NotificationResult(
                id, null, NotificationStatus.SENT,
                "SendGrid", now, "ok", Optional.empty()
        ));
        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, null,
                "SendGrid", now, "ok", Optional.empty()
        ));
        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, NotificationStatus.SENT,
                "SendGrid", null, "ok", Optional.empty()
        ));
    }

    @Test
    void shouldRejectInvalidTextAndErrorCombinations() {
        Instant now = Instant.now();
        NotificationId id = notification.id();

        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, NotificationStatus.SENT,
                " ", now, "ok", Optional.empty()
        ));
        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, NotificationStatus.SENT,
                "SendGrid", now, " ", Optional.empty()
        ));
        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, NotificationStatus.SENT,
                "SendGrid", now, "ok", null
        ));
        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, NotificationStatus.SENT,
                "SendGrid", now, "ok", Optional.of(ErrorCode.PROVIDER_ERROR)
        ));
        assertInvalid(() -> new NotificationResult(
                id, NotificationChannel.EMAIL, NotificationStatus.FAILED,
                "SendGrid", now, "failed", Optional.empty()
        ));
    }

    @Test
    void shouldRejectNullNotificationAndNullFailureCode() {
        assertInvalid(() -> NotificationResult.sent(null, "SendGrid", "ok"));
        assertInvalid(() -> NotificationResult.failed(null, "SendGrid", "failed", ErrorCode.PROVIDER_ERROR));
        assertInvalid(() -> NotificationResult.failed(notification, "SendGrid", "failed", null));
    }

    private static void assertInvalid(Runnable operation) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_NOTIFICATION_RESULT);
    }
}
