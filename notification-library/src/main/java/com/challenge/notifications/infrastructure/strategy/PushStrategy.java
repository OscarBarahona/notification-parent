package com.challenge.notifications.infrastructure.strategy;


import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.model.PushNotification;

/** Push-channel strategy. */
public final class PushStrategy extends NotificationStrategy<PushNotification> {

    public PushStrategy(NotificationProviderPort delegate) {
        super(NotificationChannel.PUSH, PushNotification.class, delegate);
    }
}
