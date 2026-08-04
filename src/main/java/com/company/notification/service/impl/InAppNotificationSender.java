package com.company.notification.service.impl;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationChannel;
import com.company.notification.service.NotificationSender;
import org.springframework.stereotype.Component;

/**
 * IN_APP delivery is satisfied simply by persistence (the notification already lives in
 * MongoDB and is returned by GET /notifications/history) - no extra action needed here.
 * Kept as an explicit no-op sender so the channel is visible/extensible in the registry.
 */
@Component
public class InAppNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void send(Notification notification) {
        // No-op: persistence in NotificationServiceImpl already satisfies IN_APP delivery.
    }
}
