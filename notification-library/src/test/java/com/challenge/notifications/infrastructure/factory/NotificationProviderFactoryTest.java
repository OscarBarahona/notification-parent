package com.challenge.notifications.infrastructure.factory;

import com.challenge.notifications.application.facade.NotificationFacade;
import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.enums.NotificationStatus;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.domain.valueobject.*;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import com.challenge.notifications.infrastructure.configuration.NotificationConfiguration;
import com.challenge.notifications.infrastructure.configuration.PushConfiguration;
import com.challenge.notifications.infrastructure.configuration.SmsConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationProviderFactoryTest {

    @Test
    void shouldCreateAllConfiguredProviders() {
        List<NotificationProviderPort> providers = NotificationProviderFactory.create(
                NotificationConfiguration.builder()
                        .email(emailConfiguration(EmailConfiguration.Provider.SENDGRID))
                        .sms(smsConfiguration())
                        .push(pushConfiguration())
                        .build()
        );

        Map<NotificationChannel, NotificationProviderPort> byChannel = providers.stream()
                .collect(Collectors.toMap(
                        NotificationProviderPort::channel,
                        Function.identity()
                ));

        assertThat(providers).hasSize(3);
        assertThat(byChannel.get(NotificationChannel.EMAIL).providerName())
                .isEqualTo("SendGrid");
        assertThat(byChannel.get(NotificationChannel.SMS).providerName())
                .isEqualTo("Twilio");
        assertThat(byChannel.get(NotificationChannel.PUSH).providerName())
                .isEqualTo("Firebase");
    }

    @Test
    void shouldCreateMailgunProvider() {
        List<NotificationProviderPort> providers = NotificationProviderFactory.create(
                NotificationConfiguration.builder()
                        .email(emailConfiguration(EmailConfiguration.Provider.MAILGUN))
                        .build()
        );

        assertThat(providers)
                .singleElement()
                .satisfies(provider -> {
                    assertThat(provider.channel()).isEqualTo(NotificationChannel.EMAIL);
                    assertThat(provider.providerName()).isEqualTo("Mailgun");
                });
    }

    @Test
    void shouldCreateWorkingFacadeFromGeneratedProviders() {
        NotificationConfiguration configuration = NotificationConfiguration.builder()
                .email(emailConfiguration(EmailConfiguration.Provider.SENDGRID))
                .sms(smsConfiguration())
                .push(pushConfiguration())
                .build();
        NotificationFacade facade = NotificationFacade.create(
                NotificationProviderFactory.create(configuration),
                Runnable::run
        );

        assertThat(facade.send(emailNotification()).status())
                .isEqualTo(NotificationStatus.SENT);
        assertThat(facade.send(smsNotification()).status())
                .isEqualTo(NotificationStatus.SENT);
        assertThat(facade.send(pushNotification()).status())
                .isEqualTo(NotificationStatus.SENT);
        assertThat(facade.sendAsync(emailNotification()).join().status())
                .isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void shouldReturnUnmodifiableProviderList() {
        List<NotificationProviderPort> providers = NotificationProviderFactory.create(
                NotificationConfiguration.builder()
                        .email(emailConfiguration(EmailConfiguration.Provider.SENDGRID))
                        .build()
        );

        assertThatThrownBy(() -> providers.add(providers.getFirst()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectNullConfiguration() {
        assertThatThrownBy(() -> NotificationProviderFactory.create(null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Notification configuration cannot be null");
    }

    private static EmailConfiguration emailConfiguration(
            EmailConfiguration.Provider provider
    ) {
        return EmailConfiguration.builder()
                .provider(provider)
                .apiKey("email-key")
                .sender(new EmailAddress("sender@example.com"))
                .build();
    }

    private static SmsConfiguration smsConfiguration() {
        return SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid("AC123456")
                .authToken("token")
                .fromNumber(new PhoneNumber("+50370000000"))
                .build();
    }

    private static PushConfiguration pushConfiguration() {
        return PushConfiguration.builder()
                .provider(PushConfiguration.Provider.FIREBASE)
                .projectId("project")
                .credentials("credentials")
                .build();
    }

    private static EmailNotification emailNotification() {
        return EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Email message")
        );
    }

    private static SmsNotification smsNotification() {
        return SmsNotification.create(
                new PhoneNumber("+50371111111"),
                new MessageBody("SMS message")
        );
    }

    private static PushNotification pushNotification() {
        return PushNotification.create(
                new DeviceToken("device-token-123456789"),
                new MessageBody("Push message")
        );
    }
}
