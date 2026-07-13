package com.challenge.notifications.dto;

import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.enums.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Unified notification delivery result")
public record NotificationResponse(
        UUID notificationId,
        NotificationChannel channel,
        NotificationStatus status,
        String provider,
        Instant occurredAt,
        String detail,
        String errorCode
) {
}
