package com.challenge.notifications.documentation;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    public static final String BASIC_AUTH_SCHEME = "basicAuth";

    @Bean
    OpenAPI notificationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Demo API")
                        .version("v1")
                        .description(
                                "Spring Boot adapter demonstrating the framework-agnostic notification library"
                        )
                        .contact(new Contact().name("Backend Challenge Team")))
                .components(new Components().addSecuritySchemes(
                        BASIC_AUTH_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME));
    }
}
