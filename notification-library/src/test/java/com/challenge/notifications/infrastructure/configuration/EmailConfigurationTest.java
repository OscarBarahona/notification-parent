package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailConfigurationTest {

    private static final EmailAddress SENDER = new EmailAddress("sender@example.com");

    @Test
    void shouldBuildSendGridConfigurationWithDefaultEndpoint() {
        EmailConfiguration configuration = EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
                .apiKey("  secret-key  ")
                .sender(SENDER)
                .build();

        assertThat(configuration.provider()).isEqualTo(EmailConfiguration.Provider.SENDGRID);
        assertThat(configuration.apiKey()).isEqualTo("secret-key");
        assertThat(configuration.sender()).isEqualTo(SENDER);
        assertThat(configuration.endpoint())
                .isEqualTo(URI.create("https://api.sendgrid.com/v3/mail/send"));
        assertThat(configuration.toString()).doesNotContain("secret-key");
    }

    @Test
    void shouldBuildMailgunConfigurationWithCustomEndpoint() {
        URI endpoint = URI.create("https://mail.example.test/messages");

        EmailConfiguration configuration = EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.MAILGUN)
                .apiKey("key")
                .endpoint(endpoint)
                .sender(SENDER)
                .build();

        assertThat(configuration.endpoint()).isEqualTo(endpoint);
        assertThat(configuration.toString()).contains("MAILGUN", endpoint.toString());
    }

    @Test
    void shouldUseMailgunDefaultEndpoint() {
        EmailConfiguration configuration = validBuilder()
                .provider(EmailConfiguration.Provider.MAILGUN)
                .build();

        assertThat(configuration.endpoint())
                .isEqualTo(URI.create("https://api.mailgun.net/v3/messages"));
    }

    @Test
    void shouldRejectMissingProvider() {
        assertThatThrownBy(() -> EmailConfiguration.builder()
                .apiKey("key")
                .sender(SENDER)
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("provider cannot be null");
    }

    @Test
    void shouldRejectBlankApiKey() {
        assertThatThrownBy(() -> validBuilder().apiKey(" ").build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Email API key cannot be null or blank");
    }

    @Test
    void shouldRejectMissingSender() {
        assertThatThrownBy(() -> EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
                .apiKey("key")
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("sender cannot be null");
    }

    @Test
    void shouldRejectNonHttpEndpoint() {
        assertThatThrownBy(() -> validBuilder()
                .endpoint(URI.create("ftp://example.test/mail"))
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("absolute HTTP or HTTPS URI");
    }

    @Test
    void shouldRejectRelativeEndpoint() {
        assertThatThrownBy(() -> validBuilder()
                .endpoint(URI.create("/mail/send"))
                .build())
                .isInstanceOf(ConfigurationException.class);
    }

    private static EmailConfiguration.Builder validBuilder() {
        return EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
                .apiKey("key")
                .sender(SENDER);
    }
}
