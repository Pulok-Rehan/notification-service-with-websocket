package com.company.notification.service.impl;

import com.company.notification.dto.FcmTokenRequest;
import com.company.notification.entity.FcmToken;
import com.company.notification.repository.FcmTokenRepository;
import com.company.notification.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmTokenServiceImpl implements FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

//    @Override
//    public FcmToken register(FcmTokenRequest request) {
//        FcmToken token = fcmTokenRepository.findByMobileAndDeviceId(request.getMobile(), request.getDeviceId())
//                .orElse(FcmToken.builder().mobile(request.getMobile()).deviceId(request.getDeviceId())
//                        .createdAt(Instant.now()).build());
//        token.setDeviceType(request.getDeviceType());
//        token.setFcmToken(request.getFcmToken());
//        token.setAppVersion(request.getAppVersion());
//        token.setPlatform(request.getPlatform());
//        token.setActive(true);
//        token.setUpdatedAt(Instant.now());
//        return fcmTokenRepository.save(token);
//    }

    public FcmToken register(FcmTokenRequest request) {

        FcmToken token = fcmTokenRepository
                .findByMobile(request.getMobile())
                .orElseGet(() -> FcmToken.builder()
                        .mobile(request.getMobile())
                        .createdAt(Instant.now())
                        .build());

        token.setFcmToken(request.getFcmToken());
        token.setDeviceType(request.getDeviceType());
        token.setAppVersion(request.getAppVersion());
        token.setPlatform(request.getPlatform());
        token.setActive(true);
        token.setUpdatedAt(Instant.now());

        return fcmTokenRepository.save(token);
    }

    @Override
    public FcmToken update(FcmTokenRequest request) {
        return register(request); // upsert semantics cover update automatically
    }

    @Override
    public void deleteToken(String mobile, String deviceId) {
        fcmTokenRepository.deleteByMobileAndDeviceId(mobile, deviceId);
    }

    @Override
    public List<FcmToken> getTokens(String mobile) {
        return fcmTokenRepository.findByMobileAndActiveTrue(mobile);
    }
}
