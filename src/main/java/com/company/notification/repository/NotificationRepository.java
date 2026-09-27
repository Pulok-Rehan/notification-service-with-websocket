package com.company.notification.repository;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByReceiverPlatformIdAndDeletedFalseOrderByCreatedAtDesc(String receiverPlatformId, Pageable pageable);

    Page<Notification> findByReceiverPlatformIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            String receiverPlatformId, NotificationStatus status, Pageable pageable);

    long countByReceiverPlatformIdAndStatusNotAndDeletedFalse(String receiverPlatformId, NotificationStatus status);

    long countByReceiverPlatformIdAndReadAtIsNullAndDeletedFalse(String receiverPlatformId);

    List<Notification> findByReceiverPlatformIdAndCreatedAtAfterAndDeletedFalse(String receiverPlatformId, Instant after);

    List<Notification> findByScheduledAtBeforeAndStatus(Instant before, NotificationStatus status);

    List<Notification> findByTtlIsNotNullAndStatusNot(NotificationStatus status);
}
