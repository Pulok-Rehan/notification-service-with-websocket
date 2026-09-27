package com.company.notification.repository;

import com.company.notification.entity.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    List<Subscription> findByPlatformId(String platformId);
    List<Subscription> findByTopic(String topic);
    boolean existsByPlatformIdAndTopic(String platformId, String topic);
    void deleteByPlatformIdAndTopic(String platformId, String topic);
}
