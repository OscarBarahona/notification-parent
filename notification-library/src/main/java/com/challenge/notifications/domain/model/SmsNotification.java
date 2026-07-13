package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.NotificationId;
import com.challenge.notifications.domain.valueobject.PhoneNumber;

public record SmsNotification(
        NotificationId id,
        PhoneNumber recipient,
        MessageBody message
) implements Notification {

    public SmsNotification {
        if (id == null) {
            throw invalid("SMS notification id cannot be null");
        }
        if (recipient == null) {
            throw invalid("SMS recipient cannot be null");
        }
        if (message == null) {
            throw invalid("SMS message cannot be null");
        }
    }

    public static SmsNotification create(
            PhoneNumber recipient,
            MessageBody message
    ) {
        return new SmsNotification(NotificationId.generate(), recipient, message);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    private static ValidationException invalid(String message) {
        return new ValidationException(ErrorCode.INVALID_NOTIFICATION, message);
    }
}