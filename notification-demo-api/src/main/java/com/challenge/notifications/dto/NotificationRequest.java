package com.challenge.notifications.dto;

import com.challenge.notifications.domain.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Unified request for Email, SMS and Push notifications")
public record NotificationRequest(
        @NotNull
        @Schema(description = "Notification channel", example = "EMAIL")
        NotificationChannel channel,

        @NotBlank
        @Size(max = 4096)
        @Schema(
                description = "Email address, E.164 phone number or device token, depending on channel",
                example = "customer@example.com"
        )
        String recipient,

        @Size(max = 150)
        @Schema(
                description = "Required for EMAIL and not allowed for SMS or PUSH",
                example = "Welcome"
        )
        String subject,

        @NotBlank
        @Size(max = 5000)
        @Schema(description = "Notification message", example = "Your account has been created")
        String message
) {
}
