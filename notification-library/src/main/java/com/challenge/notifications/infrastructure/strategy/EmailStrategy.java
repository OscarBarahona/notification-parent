package com.challenge.notifications.infrastructure.strategy;

import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.model.EmailNotification;

/** Email-channel strategy. */
public final class EmailStrategy extends NotificationStrategy<EmailNotification> {

    public EmailStrategy(NotificationProviderPort delegate) {
        super(NotificationChannel.EMAIL, EmailNotification.class, delegate);
    }
}
