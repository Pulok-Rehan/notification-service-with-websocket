package com.company.notification.service.impl;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationChannel;
import com.company.notification.mapper.NotificationMapper;
import com.company.notification.redis.PresenceService;
import com.company.notification.service.NotificationSender;
import com.company.notification.websocket.WebSocketNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

/**
 * Pushes the notification immediately if the user is online; if offline it is simply
 * left persisted in MongoDB (status=SENT/QUEUED) and delivered on next connect via the
 * pending-notifications flow (see NotificationServiceImpl#deliverPendingOnReconnect).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationSender implements NotificationSender {

    private final WebSocketNotifier webSocketNotifier;
    private final PresenceService presenceService;
    private final NotificationMapper notificationMapper;
    @Autowired
    private SimpUserRegistry simpUserRegistry;

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
        log.info("Connected users: {}", simpUserRegistry.getUsers());

        SimpUser user = simpUserRegistry.getUser(notification.getReceiverMobile());

        log.info("User found = {}", user);
        log.info("User {} is online: {}", notification.getReceiverMobile(), presenceService.isOnline(notification.getReceiverMobile()));
        if (notification.getReceiverMobile() != null && presenceService.isOnline(notification.getReceiverMobile())) {
            log.info("Sending notification to user {}", notification.getReceiverMobile());
            webSocketNotifier.sendToUser(notification.getReceiverMobile(), notificationMapper.toResponse(notification));
            log.info("Notification sent to user {}", notification.getReceiverMobile());
        }
    }
}
