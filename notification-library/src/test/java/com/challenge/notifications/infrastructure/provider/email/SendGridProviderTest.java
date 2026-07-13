package com.challenge.notifications.infrastructure.provider.email;

import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import com.challenge.notifications.domain.valueobject.Subject;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendGridProviderTest {

    @Test
    void shouldSimulateEmailDelivery() {
        SendGridProvider provider = new SendGridProvider(sendGridConfiguration());
        EmailNotification notification = emailNotification();

        ProviderResponse response = provider.send(notification);

        assertThat(provider.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(provider.providerName()).isEqualTo("SendGrid");
        assertThat(response.accepted()).isTrue();
        assertThat(response.provider()).isEqualTo("SendGrid");
        assertThat(response.detail()).contains("accepted");
    }

    @Test
    void shouldRejectNullConfiguration() {
        assertThatThrownBy(() -> new SendGridProvider(null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("SendGrid configuration cannot be null");
    }

    @Test
    void shouldRejectMailgunConfiguration() {
        EmailConfiguration configuration = EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.MAILGUN)
                .apiKey("key")
                .sender(new EmailAddress("sender@example.com"))
                .build();

        assertThatThrownBy(() -> new SendGridProvider(configuration))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("SENDGRID");
    }

    @Test
    void shouldRejectNullNotification() {
        SendGridProvider provider = new SendGridProvider(sendGridConfiguration());

        assertThatThrownBy(() -> provider.send(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectNonEmailNotification() {
        SendGridProvider provider = new SendGridProvider(sendGridConfiguration());
        SmsNotification sms = SmsNotification.create(
                new PhoneNumber("+50370000000"),
                new MessageBody("Message")
        );

        assertThatThrownBy(() -> provider.send(sms))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("only supports email");
    }

    private static EmailConfiguration sendGridConfiguration() {
        return EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
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
