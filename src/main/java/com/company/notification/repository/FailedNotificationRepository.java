package com.company.notification.repository;

import com.company.notification.entity.FailedNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FailedNotificationRepository extends MongoRepository<FailedNotification, String> {
}
