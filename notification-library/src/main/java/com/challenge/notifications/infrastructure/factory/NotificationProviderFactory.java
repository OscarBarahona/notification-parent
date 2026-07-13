package com.challenge.notifications.infrastructure.factory;

import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import com.challenge.notifications.infrastructure.configuration.NotificationConfiguration;
import com.challenge.notifications.infrastructure.configuration.PushConfiguration;
import com.challenge.notifications.infrastructure.configuration.SmsConfiguration;
import com.challenge.notifications.infrastructure.provider.email.MailgunProvider;
import com.challenge.notifications.infrastructure.provider.email.SendGridProvider;
import com.challenge.notifications.infrastructure.provider.push.FirebaseProvider;
import com.challenge.notifications.infrastructure.provider.sms.TwilioProvider;
import com.challenge.notifications.infrastructure.strategy.EmailStrategy;
import com.challenge.notifications.infrastructure.strategy.PushStrategy;
import com.challenge.notifications.infrastructure.strategy.SmsStrategy;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
 public final class NotificationProviderFactory {

    private NotificationProviderFactory() {
    }

    public static List<NotificationProviderPort> create(
            NotificationConfiguration configuration
    ) {
        if (configuration == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Notification configuration cannot be null"
            );
        }

        return Stream.of(
                        configuration.email().map(NotificationProviderFactory::createEmail),
                        configuration.sms().map(NotificationProviderFactory::createSms),
                        configuration.push().map(NotificationProviderFactory::createPush)
                )
                .flatMap(Optional::stream)
                .map(NotificationProviderPort.class::cast)
                .toList();
    }

    private static EmailStrategy createEmail(EmailConfiguration configuration) {
        NotificationProviderPort provider = switch (configuration.provider()) {
            case SENDGRID -> new SendGridProvider(configuration);
            case MAILGUN -> new MailgunProvider(configuration);
        };
        return new EmailStrategy(provider);
    }

    private static SmsStrategy createSms(SmsConfiguration configuration) {
        NotificationProviderPort provider = switch (configuration.provider()) {
            case TWILIO -> new TwilioProvider(configuration);
        };
        return new SmsStrategy(provider);
    }

    private static PushStrategy createPush(PushConfiguration configuration) {
        NotificationProviderPort provider = switch (configuration.provider()) {
            case FIREBASE -> new FirebaseProvider(configuration);
        };
        return new PushStrategy(provider);
    }
}
