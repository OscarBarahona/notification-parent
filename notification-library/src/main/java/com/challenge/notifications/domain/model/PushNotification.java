package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.valueobject.DeviceToken;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.NotificationId;

public record PushNotification(
        NotificationId id,
        DeviceToken recipient,
        MessageBody message
) implements Notification {

    public PushNotification {
        if (id == null) {
            throw invalid("Push notification id cannot be null");
        }
        if (recipient == null) {
            throw invalid("Push recipient cannot be null");
        }
        if (message == null) {
            throw invalid("Push message cannot be null");
        }
    }

    public static PushNotification create(
            DeviceToken recipient,
            MessageBody message
    ) {
        return new PushNotification(NotificationId.generate(), recipient, message);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    private static ValidationException invalid(String message) {
        return new ValidationException(ErrorCode.INVALID_NOTIFICATION, message);
    }
}
