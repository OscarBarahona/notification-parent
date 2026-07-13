package com.challenge.notifications.application.usecase;
import com.challenge.notifications.application.mapper.NotificationResultMapper;
import com.challenge.notifications.application.port.in.SendNotificationInputPort;
import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.NotificationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


public final class SendNotificationUseCase implements SendNotificationInputPort {

    private final Map<NotificationChannel, NotificationProviderPort> providersByChannel;
    private final NotificationResultMapper resultMapper;

    public SendNotificationUseCase(
            Collection<? extends NotificationProviderPort> providers
    ) {
        this(providers, new NotificationResultMapper());
    }

    public SendNotificationUseCase(
            Collection<? extends NotificationProviderPort> providers,
            NotificationResultMapper resultMapper
    ) {
        this.providersByChannel = buildProviderMap(providers);
        this.resultMapper = Objects.requireNonNull(
                resultMapper,
                "resultMapper cannot be null"
        );
    }

    @Override
    public NotificationResult send(Notification notification) {
        if (notification == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION,
                    "Notification cannot be null"
            );
        }

        NotificationProviderPort provider = providersByChannel.get(notification.channel());
        if (provider == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "No provider configured for channel " + notification.channel()
            );
        }

        try {
            ProviderResponse response = provider.send(notification);
            validateResponse(provider, response);
            return resultMapper.toDomain(notification, response);
        } catch (NotificationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Unexpected error while sending notification through "
                            + provider.providerName().trim(),
                    exception
            );
        }
    }

    private static Map<NotificationChannel, NotificationProviderPort> buildProviderMap(
            Collection<? extends NotificationProviderPort> providers
    ) {
        if (providers == null || providers.isEmpty()) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "At least one notification provider must be configured"
            );
        }

        return providers.stream()
                .map(SendNotificationUseCase::validateProvider)
                .collect(Collectors.toUnmodifiableMap(
                        NotificationProviderPort::channel,
                        Function.identity(),
                        SendNotificationUseCase::rejectDuplicateProvider
                ));
    }

    private static NotificationProviderPort validateProvider(
            NotificationProviderPort provider
    ) {
        if (provider == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Configured provider cannot be null"
            );
        }
        if (provider.channel() == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Configured provider channel cannot be null"
            );
        }
        if (provider.providerName() == null || provider.providerName().isBlank()) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Configured provider name cannot be null or blank"
            );
        }
        return provider;
    }

    private static NotificationProviderPort rejectDuplicateProvider(
            NotificationProviderPort first,
            NotificationProviderPort second
    ) {
        throw new ConfigurationException(
                ErrorCode.CONFIGURATION_ERROR,
                "Only one active provider can be configured for channel " + first.channel()
        );
    }

    private static void validateResponse(
            NotificationProviderPort selectedProvider,
            ProviderResponse response
    ) {
        if (response == null) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Provider response cannot be null"
            );
        }

        String configuredName = selectedProvider.providerName().trim();
        if (!configuredName.equals(response.provider())) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Provider response name does not match configured provider"
            );
        }
    }
}
