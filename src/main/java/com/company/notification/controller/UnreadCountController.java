package com.company.notification.controller;

import com.company.notification.dto.UnreadCountResponse;
import com.company.notification.response.ApiResponse;
import com.company.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Unread Count")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UnreadCountController {

    private final NotificationService notificationService;

    @GetMapping("/{mobile}/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(@PathVariable String mobile) {
        return ApiResponse.success(notificationService.getUnreadCount(mobile));
    }
}
