package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.valueobject.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationModelTest {

    private static final EmailAddress EMAIL = new EmailAddress("user@example.com");
    private static final PhoneNumber PHONE = new PhoneNumber("+50370000000");
    private static final DeviceToken TOKEN = new DeviceToken("abcdefghijklmnop");
    private static final Subject SUBJECT = new Subject("Welcome");
    private static final MessageBody MESSAGE = new MessageBody("Hello");

    @Nested
    class EmailNotificationTests {

        @Test
        void shouldCreateEmailNotification() {
            EmailNotification notification = EmailNotification.create(EMAIL, SUBJECT, MESSAGE);

            assertThat(notification.id()).isNotNull();
            assertThat(notification.recipient()).isEqualTo(EMAIL);
            assertThat(notification.subject()).isEqualTo(SUBJECT);
            assertThat(notification.message()).isEqualTo(MESSAGE);
            assertThat(notification.channel()).isEqualTo(NotificationChannel.EMAIL);
        }

        @Test
        void shouldRejectNullFields() {
            NotificationId id = NotificationId.generate();

            assertInvalid(() -> new EmailNotification(null, EMAIL, SUBJECT, MESSAGE));
            assertInvalid(() -> new EmailNotification(id, null, SUBJECT, MESSAGE));
            assertInvalid(() -> new EmailNotification(id, EMAIL, null, MESSAGE));
            assertInvalid(() -> new EmailNotification(id, EMAIL, SUBJECT, null));
        }
    }

    @Nested
    class SmsNotificationTests {

        @Test
        void shouldCreateSmsNotification() {
            SmsNotification notification = SmsNotification.create(PHONE, MESSAGE);

            assertThat(notification.id()).isNotNull();
            assertThat(notification.recipient()).isEqualTo(PHONE);
            assertThat(notification.message()).isEqualTo(MESSAGE);
            assertThat(notification.channel()).isEqualTo(NotificationChannel.SMS);
        }

        @Test
        void shouldRejectNullFields() {
            NotificationId id = NotificationId.generate();

            assertInvalid(() -> new SmsNotification(null, PHONE, MESSAGE));
            assertInvalid(() -> new SmsNotification(id, null, MESSAGE));
            assertInvalid(() -> new SmsNotification(id, PHONE, null));
        }
    }

    @Nested
    class PushNotificationTests {

        @Test
        void shouldCreatePushNotification() {
            PushNotification notification = PushNotification.create(TOKEN, MESSAGE);

            assertThat(notification.id()).isNotNull();
            assertThat(notification.recipient()).isEqualTo(TOKEN);
            assertThat(notification.message()).isEqualTo(MESSAGE);
            assertThat(notification.channel()).isEqualTo(NotificationChannel.PUSH);
        }

        @Test
        void shouldRejectNullFields() {
            NotificationId id = NotificationId.generate();

            assertInvalid(() -> new PushNotification(null, TOKEN, MESSAGE));
            assertInvalid(() -> new PushNotification(id, null, MESSAGE));
            assertInvalid(() -> new PushNotification(id, TOKEN, null));
        }
    }

    private static void assertInvalid(Runnable operation) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_NOTIFICATION);
    }
}
