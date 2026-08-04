package com.company.notification.repository;

import com.company.notification.entity.ScheduledNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ScheduledNotificationRepository extends MongoRepository<ScheduledNotification, String> {
    List<ScheduledNotification> findByProcessedFalseAndScheduledAtBefore(Instant before);
}
