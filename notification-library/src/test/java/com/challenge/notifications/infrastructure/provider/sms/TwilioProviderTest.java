package com.challenge.notifications.infrastructure.provider.sms;

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
import com.challenge.notifications.infrastructure.configuration.SmsConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TwilioProviderTest {

    @Test
    void shouldSimulateSmsDelivery() {
        TwilioProvider provider = new TwilioProvider(twilioConfiguration());
        SmsNotification sms = SmsNotification.create(
                new PhoneNumber("+50371111111"),
                new MessageBody("Message")
        );

        ProviderResponse response = provider.send(sms);

        assertThat(provider.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(provider.providerName()).isEqualTo("Twilio");
        assertThat(response.accepted()).isTrue();
        assertThat(response.provider()).isEqualTo("Twilio");
    }

    @Test
    void shouldRejectNullConfiguration() {
        assertThatThrownBy(() -> new TwilioProvider(null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Twilio configuration cannot be null");
    }

    @Test
    void shouldRejectConfigurationForUnknownProvider() {
        SmsConfiguration configuration = mock(SmsConfiguration.class);
        when(configuration.provider()).thenReturn(null);

        assertThatThrownBy(() -> new TwilioProvider(configuration))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("TWILIO");
    }

    @Test
    void shouldRejectNullNotification() {
        TwilioProvider provider = new TwilioProvider(twilioConfiguration());

        assertThatThrownBy(() -> provider.send(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectNonSmsNotification() {
        TwilioProvider provider = new TwilioProvider(twilioConfiguration());
        EmailNotification email = EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );

        assertThatThrownBy(() -> provider.send(email))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("only supports SMS");
    }

    private static SmsConfiguration twilioConfiguration() {
        return SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid("AC123456")
                .authToken("token")
                .fromNumber(new PhoneNumber("+50370000000"))
                .build();
    }
}
