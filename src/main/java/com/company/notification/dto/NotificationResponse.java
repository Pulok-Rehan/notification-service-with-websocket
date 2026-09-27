package com.company.notification.dto;

import com.company.notification.enums.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String body;
    private String image;
    private String clickAction;
    private String deepLink;
    private NotificationType notificationType;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private String sender;
    private String receiverPlatformId;
    private String topic;
    private String role;
    private NotificationStatus status;
    private Map<String, Object> data;
    private boolean read;
    private Instant createdAt;
    private Instant sentAt;
    private Instant readAt;
}
