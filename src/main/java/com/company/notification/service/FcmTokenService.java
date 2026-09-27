package com.company.notification.service;

import com.company.notification.dto.FcmTokenRequest;
import com.company.notification.entity.FcmToken;

import java.util.List;

public interface FcmTokenService {
    FcmToken register(FcmTokenRequest request);
    FcmToken update(FcmTokenRequest request);
    void deleteToken(String platformId, String deviceId);
    List<FcmToken> getTokens(String platformId);
}
