package com.company.notification.controller;

import com.company.notification.dto.OnlineStatsResponse;
import com.company.notification.dto.PresenceResponse;
import com.company.notification.redis.PresenceService;
import com.company.notification.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Presence")
@RestController
@RequestMapping("/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping("/{platformId}")
    public ApiResponse<PresenceResponse> getPresence(@PathVariable String platformId) {
        return ApiResponse.success(presenceService.getPresence(platformId));
    }

    @GetMapping("/online")
    public ApiResponse<Set<String>> online() {
        return ApiResponse.success(presenceService.getOnlineUsers());
    }

    @GetMapping("/count")
    public ApiResponse<OnlineStatsResponse> count(@RequestParam(defaultValue = "0") long totalRegisteredUsers) {
        return ApiResponse.success(presenceService.getStats(totalRegisteredUsers));
    }
}
