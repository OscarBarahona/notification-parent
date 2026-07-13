package com.challenge.notifications.application.port.out;


import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.model.Notification;


public interface NotificationProviderPort {

    NotificationChannel channel();
    String providerName();
    ProviderResponse send(Notification notification);

    record ProviderResponse(
            boolean accepted,
            String provider,
            String detail,
            ErrorCode errorCode
    ) {

        public ProviderResponse {
            provider = requireText(provider, "Provider name cannot be null or blank.");
            detail = requireText(detail, "Provider detail cannot be null or blank.");

            if (accepted && errorCode != null) {
                throw invalid("An accepted provider response cannot contain an error code.");
            }
            if (!accepted && errorCode == null) {
                throw invalid("A rejected provider response must contain an error code.");
            }
        }

        public static ProviderResponse accepted(String provider, String detail) {
            return new ProviderResponse(true, provider, detail, null);
        }

        public static ProviderResponse rejected(
                String provider,
                String detail,
                ErrorCode errorCode
        ) {
            return new ProviderResponse(false, provider, detail, errorCode);
        }

        private static String requireText(String value, String message) {
            if (value == null || value.isBlank()) {
                throw invalid(message);
            }
            return value.trim();
        }

        private static ProviderException invalid(String message) {
            return new ProviderException(ErrorCode.PROVIDER_ERROR, message);
        }
    }
}
