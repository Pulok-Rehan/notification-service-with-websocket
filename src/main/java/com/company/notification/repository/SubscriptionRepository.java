package com.company.notification.repository;

import com.company.notification.entity.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    List<Subscription> findByMobile(String mobile);
    List<Subscription> findByTopic(String topic);
    boolean existsByMobileAndTopic(String mobile, String topic);
    void deleteByMobileAndTopic(String mobile, String topic);
}
