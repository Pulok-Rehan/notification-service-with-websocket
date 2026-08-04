package com.company.notification.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "failed_notifications")
public class FailedNotification {

    @Id
    private String id;

    private String notificationId;
    private String reason;
    private int retryCount;
    private Instant lastAttemptAt;
    private boolean exhausted;
}
