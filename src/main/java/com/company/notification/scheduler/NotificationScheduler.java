package com.company.notification.scheduler;

import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationStatus;
import com.company.notification.repository.NotificationRepository;
import com.company.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Runs every minute. Handles:
 *  1) Dispatching due scheduled notifications (status=QUEUED, scheduledAt <= now).
 *  2) Expiring notifications whose TTL has elapsed (status -> EXPIRED, not delivered).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> senders;

    @Scheduled(cron = "0 * * * * *")
    public void dispatchScheduledNotifications() {
        List<Notification> due = notificationRepository.findByScheduledAtBeforeAndStatus(Instant.now(), NotificationStatus.QUEUED);
        for (Notification notification : due) {
            for (NotificationSender sender : senders) {
                try {
                    sender.send(notification);
                } catch (Exception e) {
                    log.error("Scheduled dispatch failed via {} for {}: {}", sender.getChannel(), notification.getId(), e.getMessage());
                }
            }
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
        }
        if (!due.isEmpty()) {
            notificationRepository.saveAll(due);
            log.info("Dispatched {} scheduled notifications", due.size());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expireTtlNotifications() {
        List<Notification> candidates = notificationRepository.findByTtlIsNotNullAndStatusNot(NotificationStatus.EXPIRED);
        Instant now = Instant.now();
        List<Notification> toExpire = candidates.stream()
                .filter(n -> n.getCreatedAt() != null && n.getTtl() != null
                        && n.getCreatedAt().plusSeconds(n.getTtl()).isBefore(now))
                .peek(n -> {
                    n.setExpired(true);
                    n.setStatus(NotificationStatus.EXPIRED);
                })
                .toList();
        if (!toExpire.isEmpty()) {
            notificationRepository.saveAll(toExpire);
            log.info("Expired {} notifications past TTL", toExpire.size());
        }
    }
}
