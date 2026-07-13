package com.challenge.notifications.infrastructure.provider.push;

import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.infrastructure.configuration.PushConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simulated Firebase Cloud Messaging adapter. No HTTP request is performed. */
public final class FirebaseProvider implements NotificationProviderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseProvider.class);
    private static final String PROVIDER_NAME = "Firebase";

    private final PushConfiguration configuration;

    public FirebaseProvider(PushConfiguration configuration) {
        this.configuration = requireConfiguration(configuration);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    @Override
    public ProviderResponse send(Notification notification) {
        PushNotification push = requirePush(notification);

        LOGGER.info(
                "Simulated Firebase delivery. notificationId={}, token={}, projectId={}, endpoint={}",
                push.id().value(),
                maskToken(push.recipient().value()),
                configuration.projectId(),
                configuration.endpoint()
        );

        return ProviderResponse.accepted(
                PROVIDER_NAME,
                "Simulated Firebase request accepted"
        );
    }

    private static PushConfiguration requireConfiguration(PushConfiguration configuration) {
        if (configuration == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Firebase configuration cannot be null"
            );
        }
        if (configuration.provider() != PushConfiguration.Provider.FIREBASE) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Firebase provider requires FIREBASE push configuration"
            );
        }
        return configuration;
    }

    private static PushNotification requirePush(Notification notification) {
        if (notification == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION,
                    "Notification cannot be null"
            );
        }
        if (!(notification instanceof PushNotification push)) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Firebase only supports push notifications"
            );
        }
        return push;
    }

    private static String maskToken(String token) {
        int visible = Math.min(6, token.length());
        return "*".repeat(token.length() - visible) + token.substring(token.length() - visible);
    }
}
