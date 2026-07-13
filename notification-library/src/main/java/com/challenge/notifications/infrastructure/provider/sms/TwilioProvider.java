package com.challenge.notifications.infrastructure.provider.sms;


import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.infrastructure.configuration.SmsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simulated Twilio adapter. No HTTP request is performed. */
public final class TwilioProvider implements NotificationProviderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioProvider.class);
    private static final String PROVIDER_NAME = "Twilio";

    private final SmsConfiguration configuration;

    public TwilioProvider(SmsConfiguration configuration) {
        this.configuration = requireConfiguration(configuration);
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    @Override
    public ProviderResponse send(Notification notification) {
        SmsNotification sms = requireSms(notification);

        LOGGER.info(
                "Simulated Twilio delivery. notificationId={}, recipient={}, from={}, accountSid={}, endpoint={}",
                sms.id().value(),
                maskPhone(sms.recipient().value()),
                configuration.fromNumber().value(),
                maskAccountSid(configuration.accountSid()),
                configuration.endpoint()
        );

        return ProviderResponse.accepted(
                PROVIDER_NAME,
                "Simulated Twilio request accepted"
        );
    }

    private static SmsConfiguration requireConfiguration(SmsConfiguration configuration) {
        if (configuration == null) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Twilio configuration cannot be null"
            );
        }
        if (configuration.provider() != SmsConfiguration.Provider.TWILIO) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "Twilio provider requires TWILIO SMS configuration"
            );
        }
        return configuration;
    }

    private static SmsNotification requireSms(Notification notification) {
        if (notification == null) {
            throw new ValidationException(
                    ErrorCode.INVALID_NOTIFICATION,
                    "Notification cannot be null"
            );
        }
        if (!(notification instanceof SmsNotification sms)) {
            throw new ProviderException(
                    ErrorCode.PROVIDER_ERROR,
                    "Twilio only supports SMS notifications"
            );
        }
        return sms;
    }

    private static String maskPhone(String phone) {
        int visible = Math.min(4, phone.length());
        return "*".repeat(phone.length() - visible) + phone.substring(phone.length() - visible);
    }

    private static String maskAccountSid(String accountSid) {
        int visible = Math.min(4, accountSid.length());
        return "*".repeat(accountSid.length() - visible)
                + accountSid.substring(accountSid.length() - visible);
    }
}
