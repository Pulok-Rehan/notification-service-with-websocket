package com.company.notification.dto;

import com.company.notification.enums.NotificationChannel;
import com.company.notification.enums.NotificationPriority;
import com.company.notification.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic inbound payload for all send endpoints (unicast/multicast/broadcast/topic/role).
 * Only the fields relevant to the chosen delivery mode need to be populated; unused
 * targeting fields are ignored by the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    private String image;
    private String icon;
    private String clickAction;
    private String deepLink;

    private NotificationType notificationType;
    private NotificationPriority priority;
    private NotificationChannel channel;

    private String sender;

    // targeting - populated depending on the endpoint used
    private String receiverPlatformId;        // unicast
    private List<String> receiverPlatformIds; // multicast
    private String topic;                 // topic
    private String role;                  // role

    private Map<String, Object> data;

    private Instant scheduledAt;
    private Long ttl;
}
