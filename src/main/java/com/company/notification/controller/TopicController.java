package com.company.notification.controller;

import com.company.notification.dto.SubscriptionRequest;
import com.company.notification.response.ApiResponse;
import com.company.notification.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Topics")
@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ApiResponse<Void> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        subscriptionService.subscribe(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/unsubscribe")
    public ApiResponse<Void> unsubscribe(@Valid @RequestBody SubscriptionRequest request) {
        subscriptionService.unsubscribe(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{platformId}")
    public ApiResponse<List<String>> listSubscriptions(@PathVariable String platformId) {
        return ApiResponse.success(subscriptionService.listSubscriptions(platformId));
    }
}
