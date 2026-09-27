package com.company.notification.repository;

import com.company.notification.entity.FcmToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {
    List<FcmToken> findByPlatformIdAndActiveTrue(String platformId);
    Optional<FcmToken> findByPlatformIdAndDeviceId(String platformId, String deviceId);
    void deleteByPlatformIdAndDeviceId(String platformId, String deviceId);
    List<FcmToken> findByPlatformIdInAndActiveTrue(List<String> platformIds);
    Optional<FcmToken> findByPlatformId(String platformId);
}
