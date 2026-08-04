package com.company.notification.controller;

import com.company.notification.dto.*;
import com.company.notification.response.ApiResponse;
import com.company.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/unicast")
    public ApiResponse<NotificationResponse> unicast(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendUnicast(request));
    }

    @PostMapping("/multicast")
    public ApiResponse<List<NotificationResponse>> multicast(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendMulticast(request));
    }

    @PostMapping("/broadcast")
    public ApiResponse<NotificationResponse> broadcast(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendBroadcast(request));
    }

    @PostMapping("/topic")
    public ApiResponse<NotificationResponse> topic(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendToTopic(request));
    }

    @PostMapping("/role")
    public ApiResponse<List<NotificationResponse>> role(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.sendToRole(request));
    }

    @PostMapping("/schedule")
    public ApiResponse<NotificationResponse> schedule(@Valid @RequestBody NotificationRequest request) {
        return ApiResponse.success(notificationService.schedule(request));
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<NotificationResponse>> history(
            @RequestParam String mobile,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) String search) {
        NotificationHistoryFilter filter = NotificationHistoryFilter.builder()
                .page(page).size(size).read(read).search(search).build();
        return ApiResponse.success(notificationService.getHistory(mobile, filter));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationResponse> getById(@PathVariable String id) {
        return ApiResponse.success(notificationService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/read/{id}")
    public ApiResponse<NotificationResponse> markRead(@PathVariable String id) {
        return ApiResponse.success(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestParam String mobile) {
        notificationService.markAllAsRead(mobile);
        return ApiResponse.success(null);
    }

    @PutMapping("/unread/{id}")
    public ApiResponse<NotificationResponse> markUnread(@PathVariable String id) {
        return ApiResponse.success(notificationService.markAsUnread(id));
    }

    @PutMapping("/archive/{id}")
    public ApiResponse<Void> archive(@PathVariable String id) {
        notificationService.archiveNotification(id);
        return ApiResponse.success(null);
    }
}
