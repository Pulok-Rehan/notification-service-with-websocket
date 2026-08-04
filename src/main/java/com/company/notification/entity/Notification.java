package com.company.notification.entity;

import com.company.notification.enums.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String title;
    private String body;
    private String image;
    private String icon;
    private String clickAction;
    private String deepLink;

    private NotificationType notificationType;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private DeliveryMode deliveryMode;

    private String sender;

    @Indexed
    private String receiverMobile;       // unicast target (mobileNumber)

    private List<String> receiverMobiles; // multicast targets

    @Indexed
    private String topic;

    private String role;

    private boolean broadcast;
    private boolean multicast;

    private Map<String, Object> data;

    @CreatedDate
    private Instant createdAt;

    private Instant scheduledAt;
    private Instant sentAt;
    private Instant readAt;

    @Indexed
    private NotificationStatus status;

    private boolean deleted;
    private boolean expired;

    private Long ttl; // seconds
}
