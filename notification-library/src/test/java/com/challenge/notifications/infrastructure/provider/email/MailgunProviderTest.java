package com.challenge.notifications.infrastructure.provider.email;

import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.domain.valueobject.DeviceToken;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailgunProviderTest {

    @Test
    void shouldSimulateEmailDelivery() {
        MailgunProvider provider = new MailgunProvider(mailgunConfiguration());

        ProviderResponse response = provider.send(emailNotification());

        assertThat(provider.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(provider.providerName()).isEqualTo("Mailgun");
        assertThat(response.accepted()).isTrue();
        assertThat(response.provider()).isEqualTo("Mailgun");
    }

    @Test
    void shouldRejectNullConfiguration() {
        assertThatThrownBy(() -> new MailgunProvider(null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Mailgun configuration cannot be null");
    }

    @Test
    void shouldRejectSendGridConfiguration() {
        EmailConfiguration configuration = EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
                .apiKey("key")
                .sender(new EmailAddress("sender@example.com"))
                .build();

        assertThatThrownBy(() -> new MailgunProvider(configuration))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("MAILGUN");
    }

    @Test
    void shouldRejectNullNotification() {
        MailgunProvider provider = new MailgunProvider(mailgunConfiguration());

        assertThatThrownBy(() -> provider.send(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectNonEmailNotification() {
        MailgunProvider provider = new MailgunProvider(mailgunConfiguration());
        PushNotification push = PushNotification.create(
                new DeviceToken("device-token-123456789"),
                new MessageBody("Message")
        );

        assertThatThrownBy(() -> provider.send(push))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("only supports email");
    }

    private static EmailConfiguration mailgunConfiguration() {
        return EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.MAILGUN)
                .apiKey("key")
                .sender(new EmailAddress("sender@example.com"))
                .build();
    }

    private static EmailNotification emailNotification() {
        return EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );
    }
}
