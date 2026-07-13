package com.challenge.notifications.domain.model;

import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.NotificationId;

public sealed interface Notification
        permits EmailNotification, SmsNotification, PushNotification {

    NotificationId id();

    NotificationChannel channel();

    MessageBody message();
}
