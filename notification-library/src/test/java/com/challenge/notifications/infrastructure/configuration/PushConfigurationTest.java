package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.exception.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PushConfigurationTest {

    @Test
    void shouldBuildFirebaseConfiguration() {
        PushConfiguration configuration = PushConfiguration.builder()
                .provider(PushConfiguration.Provider.FIREBASE)
                .projectId("  project-1  ")
                .credentials("  secret-json  ")
                .build();

        assertThat(configuration.provider()).isEqualTo(PushConfiguration.Provider.FIREBASE);
        assertThat(configuration.projectId()).isEqualTo("project-1");
        assertThat(configuration.credentials()).isEqualTo("secret-json");
        assertThat(configuration.endpoint())
                .isEqualTo(URI.create("https://fcm.googleapis.com/v1/projects"));
        assertThat(configuration.toString()).doesNotContain("secret-json");
    }

    @Test
    void shouldUseCustomEndpoint() {
        URI endpoint = URI.create("https://push.example.test/v1");
        PushConfiguration configuration = validBuilder().endpoint(endpoint).build();

        assertThat(configuration.endpoint()).isEqualTo(endpoint);
    }

    @Test
    void shouldRejectMissingProvider() {
        assertThatThrownBy(() -> PushConfiguration.builder()
                .projectId("project")
                .credentials("credentials")
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("provider cannot be null");
    }

    @Test
    void shouldRejectBlankProjectId() {
        assertThatThrownBy(() -> validBuilder().projectId(" ").build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Push project id cannot be null or blank");
    }

    @Test
    void shouldRejectBlankCredentials() {
        assertThatThrownBy(() -> validBuilder().credentials("").build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Push credentials cannot be null or blank");
    }

    @Test
    void shouldRejectInvalidEndpoint() {
        assertThatThrownBy(() -> validBuilder()
                .endpoint(URI.create("fcm/projects"))
                .build())
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("absolute HTTP or HTTPS URI");
    }

    private static PushConfiguration.Builder validBuilder() {
        return PushConfiguration.builder()
                .provider(PushConfiguration.Provider.FIREBASE)
                .projectId("project")
                .credentials("credentials");
    }
}
