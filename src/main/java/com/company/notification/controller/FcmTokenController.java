package com.company.notification.controller;

import com.company.notification.dto.FcmTokenRequest;
import com.company.notification.entity.FcmToken;
import com.company.notification.response.ApiResponse;
import com.company.notification.service.FcmTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FCM Tokens")
@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/register")
    public ApiResponse<FcmToken> register(@Valid @RequestBody FcmTokenRequest request) {
        return ApiResponse.success(fcmTokenService.register(request));
    }

    @PutMapping("/update")
    public ApiResponse<FcmToken> update(@Valid @RequestBody FcmTokenRequest request) {
        return ApiResponse.success(fcmTokenService.update(request));
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam String platformId, @RequestParam String deviceId) {
        fcmTokenService.deleteToken(platformId, deviceId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{platformId}")
    public ApiResponse<List<FcmToken>> getTokens(@PathVariable String platformId) {
        return ApiResponse.success(fcmTokenService.getTokens(platformId));
    }
}
