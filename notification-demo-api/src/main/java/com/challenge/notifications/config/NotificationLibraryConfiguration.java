package com.challenge.notifications.config;

import com.challenge.notifications.application.facade.NotificationFacade;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import com.challenge.notifications.infrastructure.configuration.NotificationConfiguration;
import com.challenge.notifications.infrastructure.configuration.PushConfiguration;
import com.challenge.notifications.infrastructure.configuration.SmsConfiguration;
import com.challenge.notifications.infrastructure.factory.NotificationProviderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration(proxyBeanMethods = false)
public class NotificationLibraryConfiguration {

    static final String DEFAULT_EMAIL_API_KEY = "demo-sendgrid-api-key";
    static final String DEFAULT_SMS_ACCOUNT_SID = "AC00000000000000000000000000000000";
    static final String DEFAULT_SMS_AUTH_TOKEN = "demo-twilio-auth-token";
    static final String DEFAULT_PUSH_CREDENTIALS = "demo-firebase-credentials";

    @Bean
    NotificationConfiguration notificationConfiguration(Environment environment) {
        EmailConfiguration email = EmailConfiguration.builder()
                .provider(emailProvider(environment))
                .apiKey(property(environment, "NOTIFICATION_EMAIL_API_KEY", DEFAULT_EMAIL_API_KEY))
                .sender(new EmailAddress(property(
                        environment,
                        "NOTIFICATION_EMAIL_SENDER",
                        "sender@example.com"
                )))
                .build();

        SmsConfiguration sms = SmsConfiguration.builder()
                .provider(SmsConfiguration.Provider.TWILIO)
                .accountSid(property(
                        environment,
                        "NOTIFICATION_SMS_ACCOUNT_SID",
                        DEFAULT_SMS_ACCOUNT_SID
                ))
                .authToken(property(
                        environment,
                        "NOTIFICATION_SMS_AUTH_TOKEN",
                        DEFAULT_SMS_AUTH_TOKEN
                ))
                .fromNumber(new PhoneNumber(property(
                        environment,
                        "NOTIFICATION_SMS_FROM_NUMBER",
                        "+50370000000"
                )))
                .build();

        PushConfiguration push = PushConfiguration.builder()
                .provider(PushConfiguration.Provider.FIREBASE)
                .projectId(property(
                        environment,
                        "NOTIFICATION_PUSH_PROJECT_ID",
                        "notification-demo"
                ))
                .credentials(property(
                        environment,
                        "NOTIFICATION_PUSH_CREDENTIALS",
                        DEFAULT_PUSH_CREDENTIALS
                ))
                .build();

        return NotificationConfiguration.builder()
                .email(email)
                .sms(sms)
                .push(push)
                .build();
    }

    @Bean(name = "notificationExecutor", destroyMethod = "close")
    ExecutorService notificationExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    NotificationFacade notificationFacade(
            NotificationConfiguration configuration,
            @Qualifier("notificationExecutor") Executor executor
    ) {
        return NotificationFacade.create(
                NotificationProviderFactory.create(configuration),
                executor
        );
    }

    private static EmailConfiguration.Provider emailProvider(Environment environment) {
        String rawProvider = property(environment, "NOTIFICATION_EMAIL_PROVIDER", "SENDGRID");
        try {
            return EmailConfiguration.Provider.valueOf(
                    rawProvider.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Unsupported email provider: " + rawProvider,
                    exception
            );
        }
    }

    private static String property(
            Environment environment,
            String name,
            String defaultValue
    ) {
        String value = environment.getProperty(name, defaultValue);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    name + " cannot be null or blank"
            );
        }
        return value.trim();
    }
}
