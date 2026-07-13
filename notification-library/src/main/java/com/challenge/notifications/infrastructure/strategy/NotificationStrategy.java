package com.challenge.notifications.infrastructure.strategy;

import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.Notification;

import java.util.Objects;

/**
 * Base strategy that guarantees channel and notification subtype compatibility
 * before delegating to a provider-specific adapter.
 */
public abstract class NotificationStrategy<T extends Notification>
        implements NotificationProviderPort {

    private final NotificationChannel channel;
    private final Class<T> notificationType;
    private final NotificationProviderPort delegate;

    protected NotificationStrategy(
            NotificationChannel channel,
            Class<T> notificationType,
            NotificationProviderPort delegate
    ) {
        this.channel = Objects.requireNonNull(channel, "channel cannot be null");
        this.notificationType = Objects.requireNonNull(
                notificationType,
                "notificationType cannot be null"
        );
        this.delegate = validateDelegate(delegate, channel);
    }

    @Override
    public final NotificationChannel channel() {
        return channel;
    }

    @Override
    public final String providerName() {
        return delegate.providerName().trim();
    }

    @Override
    public final ProviderResponse send(Notification notification) {
        if (notification == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION,
                    "Notification cannot be null"
            );
        }
        if (notification.channel() != channel
                || !notificationType.isInstance(notification)) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Strategy for channel " + channel
                            + " cannot process notification type "
                            + notification.getClass().getSimpleName()
            );
        }
        return delegate.send(notificationType.cast(notification));
    }

    private static NotificationProviderPort validateDelegate(
            NotificationProviderPort delegate,
            NotificationChannel expectedChannel
    ) {
        if (delegate == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Strategy provider delegate cannot be null"
            );
        }
        if (delegate.channel() != expectedChannel) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Strategy channel does not match provider channel"
            );
        }
        if (delegate.providerName() == null || delegate.providerName().isBlank()) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Strategy provider name cannot be null or blank"
            );
        }
        return delegate;
    }
}
