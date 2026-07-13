package com.challenge.notifications.infrastructure.strategy;

import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.model.SmsNotification;

/** SMS-channel strategy. */
public final class SmsStrategy extends NotificationStrategy<SmsNotification> {

    public SmsStrategy(NotificationProviderPort delegate) {
        super(NotificationChannel.SMS, SmsNotification.class, delegate);
    }
}
