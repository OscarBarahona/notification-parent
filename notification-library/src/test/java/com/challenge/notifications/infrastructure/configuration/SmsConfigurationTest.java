package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsConfigurationTest {

    private static final PhoneNumber FROM = new PhoneNumber("+50370000000");

    @Test
    void shouldBuildTwilioConfiguration() {
        SmsConfiguration configuration = SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid("  AC123  ")
                .authToken("  token  ")
                .fromNumber(FROM)
                .build();

        assertThat(configuration.provider()).isEqualTo(SmsConfiguration.Provider.TWILIO);
        assertThat(configuration.accountSid()).isEqualTo("AC123");
        assertThat(configuration.authToken()).isEqualTo("token");
        assertThat(configuration.fromNumber()).isEqualTo(FROM);
        assertThat(configuration.endpoint())
                .isEqualTo(URI.create("https://api.twilio.com/2010-04-01/Accounts"));
        assertThat(configuration.toString()).doesNotContain("token");
    }

    @Test
    void shouldUseCustomEndpoint() {
        URI endpoint = URI.create("http://localhost:8089/twilio");
        SmsConfiguration configuration = validBuilder().endpoint(endpoint).build();

        assertThat(configuration.endpoint()).isEqualTo(endpoint);
    }

    @Test
    void shouldRejectMissingProvider() {
        assertThatThrownBy(() -> SmsConfiguration.builder()
                .accountSid("AC1")
                .authToken("token")
                .fromNumber(FROM)
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("provider cannot be null");
    }

    @Test
    void shouldRejectBlankAccountSid() {
        assertThatThrownBy(() -> validBuilder().accountSid(" ").build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("SMS account SID cannot be null or blank");
    }

    @Test
    void shouldRejectBlankAuthToken() {
        assertThatThrownBy(() -> validBuilder().authToken(null).build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("SMS authentication token cannot be null or blank");
    }

    @Test
    void shouldRejectMissingFromNumber() {
        assertThatThrownBy(() -> SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid("AC1")
                .authToken("token")
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("fromNumber cannot be null");
    }

    @Test
    void shouldRejectInvalidEndpoint() {
        assertThatThrownBy(() -> validBuilder()
                .endpoint(URI.create("file:///tmp/twilio"))
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("absolute HTTP or HTTPS URI");
    }

    private static SmsConfiguration.Builder validBuilder() {
        return SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid("AC1")
                .authToken("token")
                .fromNumber(FROM);
    }
}
