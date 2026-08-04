package com.company.notification.firebase;

import com.company.notification.entity.Notification;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin wrapper around FirebaseMessaging covering unicast/multicast/broadcast(topic)/condition
 * sends. This is the only class that talks to Firebase directly - everything above it
 * (NotificationDispatchService) works against the PUSH channel abstraction so future
 * providers (Huawei Push, APNS direct, Web Push) can be swapped in without touching callers.
 */
@Slf4j
@Service
public class FirebasePushService {

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String sendToToken(String fcmToken, Notification notification) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(buildNotification(notification))
                .putAllData(stringifyData(notification))
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public BatchResponse sendMulticast(List<String> fcmTokens, Notification notification) throws FirebaseMessagingException {
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(fcmTokens)
                .setNotification(buildNotification(notification))
                .putAllData(stringifyData(notification))
                .build();
        return FirebaseMessaging.getInstance().sendEachForMulticast(message);
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String sendToTopic(String topic, Notification notification) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(buildNotification(notification))
                .putAllData(stringifyData(notification))
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }

    public void subscribeToTopic(List<String> fcmTokens, String topic) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().subscribeToTopic(fcmTokens, topic);
    }

    public void unsubscribeFromTopic(List<String> fcmTokens, String topic) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(fcmTokens, topic);
    }

    private com.google.firebase.messaging.Notification buildNotification(Notification n) {
        return com.google.firebase.messaging.Notification.builder()
                .setTitle(n.getTitle())
                .setBody(n.getBody())
                .setImage(n.getImage())
                .build();
    }

    private Map<String, String> stringifyData(Notification n) {
        if (n.getData() == null) return Map.of();
        return n.getData().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }
}
