package com.company.notification.dto;

import com.company.notification.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {
    @NotBlank
    private String mobile;
    @NotBlank
    private String deviceId;
    private DeviceType deviceType;
    @NotBlank
    private String fcmToken;
    private String appVersion;
    private String platform;
}
