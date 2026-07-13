package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.NotificationId;
import com.challenge.notifications.domain.valueobject.Subject;

public record EmailNotification(
        NotificationId id,
        EmailAddress recipient,
        Subject subject,
        MessageBody message
) implements Notification {

    public EmailNotification {
        if (id == null) {
            throw invalid("Email notification id cannot be null");
        }
        if (recipient == null) {
            throw invalid("Email recipient cannot be null");
        }
        if (subject == null) {
            throw invalid("Email subject cannot be null");
        }
        if (message == null) {
            throw invalid("Email message cannot be null");
        }
    }

    public static EmailNotification create(
            EmailAddress recipient,
            Subject subject,
            MessageBody message
    ) {
        return new EmailNotification(NotificationId.generate(), recipient, subject, message);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    private static ValidationException invalid(String message) {
        return new ValidationException(ErrorCode.INVALID_NOTIFICATION, message);
    }
}