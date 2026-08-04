package com.company.notification.service;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationChannel;

/**
 * Strategy interface for a single delivery channel. PUSH / WEBSOCKET / IN_APP are
 * implemented today. Adding EMAIL, SMS, WHATSAPP, APNS, WEB_PUSH later is just a
 * new @Component implementing this interface - NotificationDispatchService picks them
 * up automatically via the Spring context, no existing code changes required.
 */
public interface NotificationSender {
    NotificationChannel getChannel();
    void send(Notification notification);
}
