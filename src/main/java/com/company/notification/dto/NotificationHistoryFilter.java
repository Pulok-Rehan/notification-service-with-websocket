package com.company.notification.dto;

import com.company.notification.enums.NotificationStatus;
import com.company.notification.enums.NotificationType;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHistoryFilter {
    private Instant fromDate;
    private Instant toDate;
    private NotificationStatus status;
    private NotificationType type;
    private Boolean read;
    private String search;
    private int page;
    private int size;
}
