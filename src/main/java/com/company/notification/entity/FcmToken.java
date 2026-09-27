package com.company.notification.entity;

import com.company.notification.enums.DeviceType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fcm_tokens")
public class FcmToken {

    @Id
    private String id;

    @Indexed
    private String platformId;

    private String deviceId;
    private DeviceType deviceType;
    private String fcmToken;
    private String appVersion;
    private String platform;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
