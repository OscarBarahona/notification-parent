package com.challenge.notifications.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigurationTest {

    @Test
    void shouldConfigureApiMetadataAndBasicAuthentication() {
        OpenAPI openAPI = new OpenApiConfiguration().notificationOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Notification Demo API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey(OpenApiConfiguration.BASIC_AUTH_SCHEME);
        assertThat(openAPI.getSecurity()).hasSize(1);
    }
}
