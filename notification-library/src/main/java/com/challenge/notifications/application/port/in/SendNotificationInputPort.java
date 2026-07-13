package com.challenge.notifications.application.port.in;

import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;

@FunctionalInterface
public interface SendNotificationInputPort {

    NotificationResult send(Notification notification);
}