package com.challenge.notifications.infrastructure.provider.email;


import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/** Simulated SendGrid adapter. No HTTP request is performed. */
public final class SendGridProvider implements NotificationProviderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendGridProvider.class);
    private static final String PROVIDER_NAME = "SendGrid";

    private final EmailConfiguration configuration;

    public SendGridProvider(EmailConfiguration configuration) {
        this.configuration = requireConfiguration(configuration);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    @Override
    public ProviderResponse send(Notification notification) {
        EmailNotification email = requireEmail(notification);

        LOGGER.info(
                "Simulated SendGrid delivery. notificationId={}, recipient={}, sender={}, endpoint={}",
                email.id().value(),
                maskEmail(email.recipient().value()),
                configuration.sender().value(),
                configuration.endpoint()
        );

        return ProviderResponse.accepted(
                PROVIDER_NAME,
                "Simulated SendGrid request accepted"
        );
    }

    private static EmailConfiguration requireConfiguration(EmailConfiguration configuration) {
        if (configuration == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "SendGrid configuration cannot be null"
            );
        }
        if (configuration.provider() != EmailConfiguration.Provider.SENDGRID) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "SendGrid provider requires SENDGRID email configuration"
            );
        }
        return configuration;
    }

    private static EmailNotification requireEmail(Notification notification) {
        if (notification == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION,
                    "Notification cannot be null"
            );
        }
        if (!(notification instanceof EmailNotification email)) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "SendGrid only supports email notifications"
            );
        }
        return email;
    }

    private static String maskEmail(String email) {
        int separator = Objects.requireNonNull(email).indexOf('@');
        return email.charAt(0) + "***" + email.substring(separator);
    }
}
