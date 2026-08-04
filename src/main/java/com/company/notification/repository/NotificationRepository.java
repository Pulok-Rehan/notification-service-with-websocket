package com.company.notification.repository;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByReceiverMobileAndDeletedFalseOrderByCreatedAtDesc(String receiverMobile, Pageable pageable);

    Page<Notification> findByReceiverMobileAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            String receiverMobile, NotificationStatus status, Pageable pageable);

    long countByReceiverMobileAndStatusNotAndDeletedFalse(String receiverMobile, NotificationStatus status);

    List<Notification> findByReceiverMobileAndCreatedAtAfterAndDeletedFalse(String receiverMobile, Instant after);

    List<Notification> findByScheduledAtBeforeAndStatus(Instant before, NotificationStatus status);

    List<Notification> findByTtlIsNotNullAndStatusNot(NotificationStatus status);
}
