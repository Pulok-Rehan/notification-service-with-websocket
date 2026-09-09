package com.company.notification.repository;

import com.company.notification.entity.FcmToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {
    List<FcmToken> findByMobileAndActiveTrue(String mobile);
    Optional<FcmToken> findByMobileAndDeviceId(String mobile, String deviceId);
    void deleteByMobileAndDeviceId(String mobile, String deviceId);
    List<FcmToken> findByMobileInAndActiveTrue(List<String> mobiles);
    Optional<FcmToken> findByMobile(String mobile);
}
