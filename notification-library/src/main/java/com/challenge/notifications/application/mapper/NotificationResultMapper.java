package com.challenge.notifications.application.mapper;


import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;

public final class NotificationResultMapper {

    public NotificationResult toDomain(
            Notification notification,
            ProviderResponse response
    ) {
        if (notification == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION,
                    "Notification cannot be null"
            );
        }
        if (response == null) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Provider response cannot be null"
            );
        }

        if (response.accepted()) {
            return NotificationResult.sent(
                    notification,
                    response.provider(),
                    response.detail()
            );
        }

        return NotificationResult.failed(
                notification,
                response.provider(),
                response.detail(),
                response.errorCode()
        );
    }
}
