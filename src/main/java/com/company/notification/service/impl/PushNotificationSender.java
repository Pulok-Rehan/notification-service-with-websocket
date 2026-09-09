package com.company.notification.service.impl;

import com.company.notification.entity.FcmToken;
import com.company.notification.entity.FailedNotification;
import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationChannel;
import com.company.notification.firebase.FirebasePushService;
import com.company.notification.repository.FailedNotificationRepository;
import com.company.notification.repository.FcmTokenRepository;
import com.company.notification.service.NotificationSender;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationSender implements NotificationSender {

    private final FirebasePushService firebasePushService;
    private final FcmTokenRepository fcmTokenRepository;
    private final FailedNotificationRepository failedNotificationRepository;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void send(Notification notification) {
        try {
            if (notification.isBroadcast() || notification.getTopic() != null) {
                String topic = notification.getTopic() != null ? notification.getTopic() : "global";
                firebasePushService.sendToTopic(topic, notification);
                return;
            }
            if (notification.isMulticast() && notification.getReceiverMobiles() != null) {
                List<String> tokens = notification.getReceiverMobiles().stream()
                        .flatMap(m -> fcmTokenRepository.findByMobileAndActiveTrue(m).stream())
                        .map(FcmToken::getFcmToken)
                        .toList();
                if (!tokens.isEmpty()) {
                    firebasePushService.sendMulticast(tokens, notification);
                }
                return;
            }
            if (notification.getReceiverMobile() != null) {
                List<FcmToken> tokens = fcmTokenRepository.findByMobileAndActiveTrue(notification.getReceiverMobile());
                for (FcmToken t : tokens) {
                    firebasePushService.sendToToken(t.getFcmToken(), notification);
                    log.info("FCM message sent to device {} for notification {}", t.getFcmToken(), notification.getId());
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Firebase send failed for notification {}: {}", notification.getId(), e.getMessage());
            failedNotificationRepository.save(FailedNotification.builder()
                    .notificationId(notification.getId())
                    .reason(e.getMessage())
                    .retryCount(0)
                    .lastAttemptAt(Instant.now())
                    .exhausted(false)
                    .build());
        }
    }
}
