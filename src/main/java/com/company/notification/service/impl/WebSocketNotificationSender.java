package com.company.notification.service.impl;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationChannel;
import com.company.notification.mapper.NotificationMapper;
import com.company.notification.redis.PresenceService;
import com.company.notification.service.NotificationSender;
import com.company.notification.websocket.WebSocketNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Pushes the notification immediately if the user is online; if offline it is simply
 * left persisted in MongoDB (status=SENT/QUEUED) and delivered on next connect via the
 * pending-notifications flow (see NotificationServiceImpl#deliverPendingOnReconnect).
 */
@Component
@RequiredArgsConstructor
public class WebSocketNotificationSender implements NotificationSender {

    private final WebSocketNotifier webSocketNotifier;
    private final PresenceService presenceService;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBSOCKET;
    }

    @Override
    public void send(Notification notification) {
        if (notification.isBroadcast()) {
            webSocketNotifier.broadcast(notificationMapper.toResponse(notification));
            return;
        }
        if (notification.getTopic() != null) {
            webSocketNotifier.sendToTopic(notification.getTopic(), notificationMapper.toResponse(notification));
            return;
        }
        if (notification.isMulticast() && notification.getReceiverMobiles() != null) {
            notification.getReceiverMobiles().forEach(mobile -> {
                if (presenceService.isOnline(mobile)) {
                    webSocketNotifier.sendToUser(mobile, notificationMapper.toResponse(notification));
                }
            });
            return;
        }
        if (notification.getReceiverMobile() != null && presenceService.isOnline(notification.getReceiverMobile())) {
            webSocketNotifier.sendToUser(notification.getReceiverMobile(), notificationMapper.toResponse(notification));
        }
    }
}
