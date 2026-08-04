package com.company.notification.dto;

import com.company.notification.enums.PresenceStatus;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceResponse {
    private String mobile;
    private PresenceStatus status;
    private Instant lastSeen;
    private String device;
}
