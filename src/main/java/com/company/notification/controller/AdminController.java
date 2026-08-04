package com.company.notification.controller;

import com.company.notification.dto.NotificationRequest;
import com.company.notification.dto.NotificationResponse;
import com.company.notification.response.ApiResponse;
import com.company.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    public ApiResponse<NotificationResponse> broadcast(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendBroadcast(request));
    }

    @PostMapping("/system-alert")
    public ApiResponse<NotificationResponse> systemAlert(@Valid @RequestBody NotificationRequest request) {
        request.setNotificationType(com.company.notification.enums.NotificationType.SYSTEM);
        request.setPriority(com.company.notification.enums.NotificationPriority.URGENT);
        return ApiResponse.success(notificationService.sendBroadcast(request));
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> statistics() {
        // Lightweight placeholder; wire to Actuator metrics or a dedicated StatisticsService
        // for production dashboards (delivery success rate, queue size, etc).
        return ApiResponse.success(Map.of("status", "ok"));
    }
}
