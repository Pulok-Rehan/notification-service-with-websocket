package com.company.notification.entity;

import com.company.notification.dto.NotificationRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scheduled_notifications")
public class ScheduledNotification {

    @Id
    private String id;

    private NotificationRequest payload;

    @Indexed
    private Instant scheduledAt;

    @Builder.Default
    private boolean processed = false;
}
