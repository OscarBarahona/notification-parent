package com.challenge.notifications.mapper;

import com.challenge.notifications.dto.NotificationRequest;
import com.challenge.notifications.dto.NotificationResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.domain.model.SmsNotification;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationRestMapperTest {

    private final NotificationRestMapper mapper =
            Mappers.getMapper(NotificationRestMapper.class);

    @Test
    void shouldMapEmailRequest() {
        NotificationRequest request = new NotificationRequest(
                NotificationChannel.EMAIL,
                "customer@example.com",
                "Welcome",
                "Account created"
        );

        Notification notification = mapper.toDomain(request);

        assertThat(notification).isInstanceOfSatisfying(
                EmailNotification.class,
                email -> {
                    assertThat(email.recipient().value()).isEqualTo("customer@example.com");
                    assertThat(email.subject().value()).isEqualTo("Welcome");
                    assertThat(email.message().value()).isEqualTo("Account created");
                }
        );
    }

    @Test
    void shouldMapSmsRequest() {
        NotificationRequest request = new NotificationRequest(
                NotificationChannel.SMS,
                "+50370000000",
                null,
                "Security code: 1234"
        );

        Notification notification = mapper.toDomain(request);

        assertThat(notification).isInstanceOfSatisfying(
                SmsNotification.class,
                sms -> {
                    assertThat(sms.recipient().value()).isEqualTo("+50370000000");
                    assertThat(sms.message().value()).isEqualTo("Security code: 1234");
                }
        );
    }

    @Test
    void shouldMapPushRequest() {
        NotificationRequest request = new NotificationRequest(
                NotificationChannel.PUSH,
                "device-token-1234567890",
                " ",
                "You have a new message"
        );

        Notification notification = mapper.toDomain(request);

        assertThat(notification).isInstanceOfSatisfying(
                PushNotification.class,
                push -> {
                    assertThat(push.recipient().value()).isEqualTo("device-token-1234567890");
                    assertThat(push.message().value()).isEqualTo("You have a new message");
                }
        );
    }

    @Test
    void shouldRejectSubjectForSms() {
        NotificationRequest request = new NotificationRequest(
                NotificationChannel.SMS,
                "+50370000000",
                "Not supported",
                "Message"
        );

        assertThatThrownBy(() -> mapper.toDomain(request))
                .isInstanceOf(ValidationException.class)
                .extracting(exception -> ((ValidationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SUBJECT);
    }

    @Test
    void shouldRejectNullRequest() {
        assertThatThrownBy(() -> mapper.toDomain(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Notification request cannot be null");
    }

    @Test
    void shouldRejectNullChannel() {
        NotificationRequest request = new NotificationRequest(
                null,
                "customer@example.com",
                "Welcome",
                "Message"
        );

        assertThatThrownBy(() -> mapper.toDomain(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Notification channel cannot be null");
    }

    @Test
    void shouldMapSuccessfulResult() {
        EmailNotification notification = EmailNotification.create(
                new com.challenge.notifications.domain.valueobject.EmailAddress("customer@example.com"),
                new com.challenge.notifications.domain.valueobject.Subject("Welcome"),
                new com.challenge.notifications.domain.valueobject.MessageBody("Message")
        );
        NotificationResult result = NotificationResult.sent(
                notification,
                "SendGrid",
                "Accepted"
        );

        NotificationResponse response = mapper.toResponse(result);

        assertThat(response.notificationId()).isEqualTo(notification.id().value());
        assertThat(response.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(response.status()).isEqualTo(result.status());
        assertThat(response.provider()).isEqualTo("SendGrid");
        assertThat(response.errorCode()).isNull();
    }

    @Test
    void shouldMapFailedResultErrorCode() {
        SmsNotification notification = SmsNotification.create(
                new com.challenge.notifications.domain.valueobject.PhoneNumber("+50370000000"),
                new com.challenge.notifications.domain.valueobject.MessageBody("Message")
        );
        NotificationResult result = NotificationResult.failed(
                notification,
                "Twilio",
                "Rejected",
                ErrorCode.PROVIDER_ERROR
        );

        NotificationResponse response = mapper.toResponse(result);

        assertThat(response.errorCode()).isEqualTo("PROVIDER_ERROR");
        assertThat(response.detail()).isEqualTo("Rejected");
    }
}
