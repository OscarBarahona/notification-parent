package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.enums.NotificationStatus;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.valueobject.NotificationId;

import java.time.Instant;
import java.util.Optional;

public record NotificationResult(
        NotificationId notificationId,
        NotificationChannel channel,
        NotificationStatus status,
        String provider,
        Instant occurredAt,
        String detail,
        Optional<ErrorCode> errorCode
) {

    public NotificationResult {
        if (notificationId == null || channel == null || status == null || occurredAt == null) {
            throw invalid("Notification result mandatory fields cannot be null");
        }

        provider = requireText(provider, "Provider cannot be null or blank");
        detail = requireText(detail, "Result detail cannot be null or blank");

        if (errorCode == null) {
            throw invalid("Error code container cannot be null");
        }
        if (status == NotificationStatus.SENT && errorCode.isPresent()) {
            throw invalid("A successful result cannot contain an error code");
        }
        if (status == NotificationStatus.FAILED && errorCode.isEmpty()) {
            throw invalid("A failed result must contain an error code");
        }
    }

    public static NotificationResult sent(
            Notification notification,
            String provider,
            String detail
    ) {
        requireNotification(notification);
        return new NotificationResult(
                notification.id(),
                notification.channel(),
                NotificationStatus.SENT,
                provider,
                Instant.now(),
                detail,
                Optional.empty()
        );
    }

    public static NotificationResult failed(
            Notification notification,
            String provider,
            String detail,
            ErrorCode errorCode
    ) {
        requireNotification(notification);
        if (errorCode == null) {
            throw invalid("Failed result error code cannot be null");
        }

        return new NotificationResult(
                notification.id(),
                notification.channel(),
                NotificationStatus.FAILED,
                provider,
                Instant.now(),
                detail,
                Optional.of(errorCode)
        );
    }

    public boolean isSuccess() {
        return status == NotificationStatus.SENT;
    }

    private static void requireNotification(Notification notification) {
        if (notification == null) {
            throw invalid("Notification cannot be null");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw invalid(message);
        }
        return value.trim();
    }

    private static ValidationException invalid(String message) {
        return new ValidationException(ErrorCode.INVALID_NOTIFICATION_RESULT, message);
    }
}