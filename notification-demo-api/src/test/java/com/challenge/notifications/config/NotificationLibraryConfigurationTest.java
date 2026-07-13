package com.challenge.notifications.config;

import com.challenge.notifications.application.facade.NotificationFacade;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import com.challenge.notifications.infrastructure.configuration.NotificationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationLibraryConfigurationTest {

    private final NotificationLibraryConfiguration configuration =
            new NotificationLibraryConfiguration();

    @Test
    void shouldBuildAllChannelsWithDefaults() {
        NotificationConfiguration result = configuration.notificationConfiguration(
                new MockEnvironment()
        );

        assertThat(result.email()).isPresent();
        assertThat(result.sms()).isPresent();
        assertThat(result.push()).isPresent();
        assertThat(result.email().orElseThrow().provider())
                .isEqualTo(EmailConfiguration.Provider.SENDGRID);
    }

    @Test
    void shouldSelectMailgunFromEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("NOTIFICATION_EMAIL_PROVIDER", "mailgun")
                .withProperty("NOTIFICATION_EMAIL_API_KEY", "mailgun-key")
                .withProperty("NOTIFICATION_EMAIL_SENDER", "mail@example.com");

        NotificationConfiguration result = configuration.notificationConfiguration(environment);

        assertThat(result.email().orElseThrow().provider())
                .isEqualTo(EmailConfiguration.Provider.MAILGUN);
        assertThat(result.email().orElseThrow().apiKey()).isEqualTo("mailgun-key");
    }

    @Test
    void shouldRejectUnsupportedEmailProvider() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("NOTIFICATION_EMAIL_PROVIDER", "unsupported");

        assertThatThrownBy(() -> configuration.notificationConfiguration(environment))
                .isInstanceOf(ConfigurationException.class)
                .extracting(exception -> ((ConfigurationException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CONFIGURATION_ERROR);
    }

    @Test
    void shouldRejectBlankConfiguredProperty() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("NOTIFICATION_EMAIL_API_KEY", " ");

        assertThatThrownBy(() -> configuration.notificationConfiguration(environment))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("NOTIFICATION_EMAIL_API_KEY cannot be null or blank");
    }

    @Test
    void shouldUseVirtualThreadsForAsyncNotifications() throws Exception {
        try (ExecutorService executor = configuration.notificationExecutor()) {
            Future<Boolean> virtualThread = executor.submit(() -> Thread.currentThread().isVirtual());

            assertThat(virtualThread.get()).isTrue();
        }
    }

    @Test
    void shouldCreateWorkingFacade() {
        NotificationConfiguration libraryConfiguration =
                configuration.notificationConfiguration(new MockEnvironment());

        try (ExecutorService executor = configuration.notificationExecutor()) {
            NotificationFacade facade = configuration.notificationFacade(
                    libraryConfiguration,
                    executor
            );

            NotificationResult result = facade.send(EmailNotification.create(
                    new EmailAddress("customer@example.com"),
                    new Subject("Welcome"),
                    new MessageBody("Account created")
            ));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.provider()).isEqualTo("SendGrid");
        }
    }
}
