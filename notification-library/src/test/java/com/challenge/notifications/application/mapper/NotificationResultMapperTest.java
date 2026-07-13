package com.challenge.notifications.application.mapper;

import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationStatus;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationResultMapperTest {

    private NotificationResultMapper mapper;
    private EmailNotification notification;

    @BeforeEach
    void setUp() {
        mapper = new NotificationResultMapper();
        notification = EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );
    }

    @Test
    void shouldMapAcceptedResponse() {
        var result = mapper.toDomain(
                notification,
                ProviderResponse.accepted("SendGrid", "Accepted")
        );

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.notificationId()).isEqualTo(notification.id());
        assertThat(result.errorCode()).isEmpty();
    }

    @Test
    void shouldMapRejectedResponse() {
        var result = mapper.toDomain(
                notification,
                ProviderResponse.rejected(
                        "SendGrid",
                        "Rejected",
                        ErrorCode.PROVIDER_ERROR
                )
        );

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.errorCode()).contains(ErrorCode.PROVIDER_ERROR);
    }

    @Test
    void shouldRejectNullNotification() {
        assertThatThrownBy(() -> mapper.toDomain(
                null,
                ProviderResponse.accepted("SendGrid", "Accepted")
        )).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectNullProviderResponse() {
        assertThatThrownBy(() -> mapper.toDomain(notification, null))
                .isInstanceOf(ProviderException.class);
    }
}
