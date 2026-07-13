package com.challenge.notifications.infrastructure.strategy;

import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationStrategyTest {

    @Test
    void shouldDelegateEmailNotification() {
        EmailNotification notification = emailNotification();
        var delegate = new StubProvider(NotificationChannel.EMAIL, "  SendGrid  ");
        EmailStrategy strategy = new EmailStrategy(delegate);

        ProviderResponse response = strategy.send(notification);

        assertThat(strategy.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(strategy.providerName()).isEqualTo("SendGrid");
        assertThat(response.provider()).isEqualTo("SendGrid");
        assertThat(delegate.lastNotification).isSameAs(notification);
    }

    @Test
    void shouldDelegateSmsNotification() {
        SmsNotification notification = SmsNotification.create(
                new PhoneNumber("+50370000000"),
                new MessageBody("SMS")
        );
        SmsStrategy strategy = new SmsStrategy(
                new StubProvider(NotificationChannel.SMS, "Twilio")
        );

        assertThat(strategy.send(notification).provider()).isEqualTo("Twilio");
        assertThat(strategy.channel()).isEqualTo(NotificationChannel.SMS);
    }

    @Test
    void shouldDelegatePushNotification() {
        PushNotification notification = PushNotification.create(
                new DeviceToken("device-token-123456789"),
                new MessageBody("Push")
        );
        PushStrategy strategy = new PushStrategy(
                new StubProvider(NotificationChannel.PUSH, "Firebase")
        );

        assertThat(strategy.send(notification).provider()).isEqualTo("Firebase");
        assertThat(strategy.channel()).isEqualTo(NotificationChannel.PUSH);
    }

    @Test
    void shouldRejectNullNotification() {
        EmailStrategy strategy = new EmailStrategy(
                new StubProvider(NotificationChannel.EMAIL, "SendGrid")
        );

        assertThatThrownBy(() -> strategy.send(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Notification cannot be null");
    }

    @Test
    void shouldRejectNotificationForDifferentChannel() {
        EmailStrategy strategy = new EmailStrategy(
                new StubProvider(NotificationChannel.EMAIL, "SendGrid")
        );
        SmsNotification sms = SmsNotification.create(
                new PhoneNumber("+50370000000"),
                new MessageBody("SMS")
        );

        assertThatThrownBy(() -> strategy.send(sms))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("EMAIL")
                .hasMessageContaining("SmsNotification");
    }

    @Test
    void shouldRejectNullDelegate() {
        assertThatThrownBy(() -> new EmailStrategy(null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Strategy provider delegate cannot be null");
    }

    @Test
    void shouldRejectDelegateForDifferentChannel() {
        NotificationProviderPort delegate = new StubProvider(
                NotificationChannel.SMS,
                "Twilio"
        );

        assertThatThrownBy(() -> new EmailStrategy(delegate))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Strategy channel does not match provider channel");
    }

    @Test
    void shouldRejectBlankDelegateName() {
        NotificationProviderPort delegate = new StubProvider(
                NotificationChannel.EMAIL,
                " "
        );

        assertThatThrownBy(() -> new EmailStrategy(delegate))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Strategy provider name cannot be null or blank");
    }

    @Test
    void shouldRejectNullStrategyChannel() {
        assertThatThrownBy(() -> new TestStrategy(
                null,
                EmailNotification.class,
                new StubProvider(NotificationChannel.EMAIL, "SendGrid")
        )).isInstanceOf(NullPointerException.class)
                .hasMessage("channel cannot be null");
    }

    @Test
    void shouldRejectNullNotificationType() {
        assertThatThrownBy(() -> new TestStrategy(
                NotificationChannel.EMAIL,
                null,
                new StubProvider(NotificationChannel.EMAIL, "SendGrid")
        )).isInstanceOf(NullPointerException.class)
                .hasMessage("notificationType cannot be null");
    }

    private static EmailNotification emailNotification() {
        return EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );
    }

    private static final class TestStrategy
            extends NotificationStrategy<EmailNotification> {

        private TestStrategy(
                NotificationChannel channel,
                Class<EmailNotification> type,
                NotificationProviderPort delegate
        ) {
            super(channel, type, delegate);
        }
    }

    private static final class StubProvider implements NotificationProviderPort {

        private final NotificationChannel channel;
        private final String providerName;
        private Notification lastNotification;

        private StubProvider(NotificationChannel channel, String providerName) {
            this.channel = channel;
            this.providerName = providerName;
        }

        @Override
        public NotificationChannel channel() {
            return channel;
        }

        @Override
        public String providerName() {
            return providerName;
        }

        @Override
        public ProviderResponse send(Notification notification) {
            this.lastNotification = notification;
            return ProviderResponse.accepted(providerName.trim(), "Accepted");
        }
    }
}
