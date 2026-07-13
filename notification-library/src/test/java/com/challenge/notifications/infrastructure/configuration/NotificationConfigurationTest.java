package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationConfigurationTest {

    @Test
    void shouldBuildConfigurationWithAllChannels() {
        EmailConfiguration email = emailConfiguration();
        SmsConfiguration sms = smsConfiguration();
        PushConfiguration push = pushConfiguration();

        NotificationConfiguration configuration = NotificationConfiguration.builder()
                .email(email)
                .sms(sms)
                .push(push)
                .build();

        assertThat(configuration.email()).contains(email);
        assertThat(configuration.sms()).contains(sms);
        assertThat(configuration.push()).contains(push);
        assertThat(configuration.toString()).contains("Optional");
    }

    @Test
    void shouldAllowOnlyOneConfiguredChannel() {
        NotificationConfiguration configuration = NotificationConfiguration.builder()
                .email(emailConfiguration())
                .build();

        assertThat(configuration.email()).isPresent();
        assertThat(configuration.sms()).isEmpty();
        assertThat(configuration.push()).isEmpty();
    }

    @Test
    void shouldRejectConfigurationWithoutChannels() {
        assertThatThrownBy(() -> NotificationConfiguration.builder().build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("At least one notification channel must be configured");
    }

    private static EmailConfiguration emailConfiguration() {
        return EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
                .apiKey("key")
                .sender(new EmailAddress("sender@example.com"))
                .build();
    }

    private static SmsConfiguration smsConfiguration() {
        return SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid("AC1")
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
}
