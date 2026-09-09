package com.company.notification.service.impl;

import com.company.notification.dto.*;
import com.company.notification.entity.Notification;
import com.company.notification.enums.*;
import com.company.notification.exception.ResourceNotFoundException;
import com.company.notification.mapper.NotificationMapper;
import com.company.notification.redis.PresenceService;
import com.company.notification.redis.UnreadCountService;
import com.company.notification.repository.NotificationRepository;
import com.company.notification.service.NotificationSender;
import com.company.notification.service.NotificationService;
import com.company.notification.service.RoleDirectoryService;
import com.company.notification.websocket.WebSocketNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Central orchestrator for every send/read/manage operation on notifications.
 *
 * Delivery flow for every send* method:
 *   1. Build + persist the Notification entity (status=CREATED) - this satisfies
 *      "every notification must be stored" and IN_APP/history requirements.
 *   2. Fan out to every registered NotificationSender (PUSH, WEBSOCKET, IN_APP, ...).
 *      New channels (EMAIL/SMS/WHATSAPP/APNS/WEB_PUSH) plug in automatically once a
 *      bean implementing NotificationSender exists - this class never needs to change.
 *   3. Update status=SENT, increment the receiver's Redis+Mongo-backed unread count,
 *      and push the live unread-count update over WebSocket.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final List<NotificationSender> senders;
    private final UnreadCountService unreadCountService;
    private final WebSocketNotifier webSocketNotifier;
    private final PresenceService presenceService;
    private final RoleDirectoryService roleDirectoryService;
    private final MongoTemplate mongoTemplate;

    @Override
    public NotificationResponse sendUnicast(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        notification.setDeliveryMode(DeliveryMode.UNICAST);
        notification.setChannel(defaultChannel(request));
        notification.setStatus(NotificationStatus.CREATED);
        notification.setCreatedAt(Instant.now());
        notification = notificationRepository.save(notification);

        dispatch(notification);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        notification = notificationRepository.save(notification);

        long unread = unreadCountService.increment(notification.getReceiverMobile());
        webSocketNotifier.sendUnreadCount(notification.getReceiverMobile(), unread);

        return notificationMapper.toResponse(notification);
    }

    @Override
    public List<NotificationResponse> sendMulticast(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        notification.setDeliveryMode(DeliveryMode.MULTICAST);
        notification.setMulticast(true);
        notification.setChannel(defaultChannel(request));
        notification.setStatus(NotificationStatus.CREATED);
        notification.setCreatedAt(Instant.now());
        Notification saved = notificationRepository.save(notification);

        dispatch(saved);

        saved.setStatus(NotificationStatus.SENT);
        saved.setSentAt(Instant.now());
        saved = notificationRepository.save(saved);

        List<String> mobiles = saved.getReceiverMobiles() == null ? List.of() : saved.getReceiverMobiles();
        for (String mobile : mobiles) {
            long unread = unreadCountService.increment(mobile);
            webSocketNotifier.sendUnreadCount(mobile, unread);
        }
        Notification finalSaved = saved;
        return mobiles.stream().map(m -> notificationMapper.toResponse(finalSaved)).collect(Collectors.toList());
    }

    @Override
    public NotificationResponse sendBroadcast(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        notification.setDeliveryMode(DeliveryMode.BROADCAST);
        notification.setBroadcast(true);
        notification.setChannel(defaultChannel(request));
        notification.setStatus(NotificationStatus.CREATED);
        notification.setCreatedAt(Instant.now());
        notification = notificationRepository.save(notification);

        dispatch(notification);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse sendToTopic(NotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        notification.setDeliveryMode(DeliveryMode.TOPIC);
        notification.setChannel(defaultChannel(request));
        notification.setStatus(NotificationStatus.CREATED);
        notification.setCreatedAt(Instant.now());
        notification = notificationRepository.save(notification);

        dispatch(notification);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public List<NotificationResponse> sendToRole(NotificationRequest request) {
        List<String> mobiles = roleDirectoryService.getMobilesByRole(request.getRole());
        NotificationRequest multicastRequest = NotificationRequest.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .image(request.getImage())
                .icon(request.getIcon())
                .clickAction(request.getClickAction())
                .deepLink(request.getDeepLink())
                .notificationType(request.getNotificationType())
                .priority(request.getPriority())
                .channel(request.getChannel())
                .sender(request.getSender())
                .receiverMobiles(mobiles)
                .data(request.getData())
                .ttl(request.getTtl())
                .build();
        return sendMulticast(multicastRequest);
    }

    @Override
    public NotificationResponse schedule(NotificationRequest request) {
        // Persisted via ScheduledNotificationRepository by the caller-facing controller;
        // the per-minute NotificationScheduler picks these up and calls the right send*.
        Notification notification = notificationMapper.toEntity(request);
        notification.setStatus(NotificationStatus.QUEUED);
        notification.setScheduledAt(request.getScheduledAt());
        notification.setCreatedAt(Instant.now());
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public PageResponse<NotificationResponse> getHistory(String mobile, NotificationHistoryFilter filter) {
        int page = Math.max(filter.getPage(), 0);
        int size = filter.getSize() > 0 ? Math.min(filter.getSize(), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        Query query = new Query();
        query.addCriteria(Criteria.where("receiverMobile").is(mobile).and("deleted").ne(true));

        if (filter.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(filter.getStatus()));
        }
        if (filter.getType() != null) {
            query.addCriteria(Criteria.where("notificationType").is(filter.getType()));
        }
        if (filter.getRead() != null) {
            if (Boolean.TRUE.equals(filter.getRead())) {
                query.addCriteria(Criteria.where("readAt").ne(null));
            } else {
                query.addCriteria(Criteria.where("readAt").is(null));
            }
        }
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String escaped = Pattern.quote(filter.getSearch().trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(escaped, "i"),
                    Criteria.where("body").regex(escaped, "i")));
        }
        if (filter.getFromDate() != null || filter.getToDate() != null) {
            Criteria dateCriteria = Criteria.where("createdAt");
            if (filter.getFromDate() != null) {
                dateCriteria = dateCriteria.gte(filter.getFromDate());
            }
            if (filter.getToDate() != null) {
                dateCriteria = dateCriteria.lte(filter.getToDate());
            }
            query.addCriteria(dateCriteria);
        }

        long total = mongoTemplate.count(query, Notification.class);

        query.with(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("_id")));
        query.skip((long) page * size).limit(size);

        List<NotificationResponse> content = mongoTemplate.find(query, Notification.class).stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / size);

        return PageResponse.<NotificationResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public NotificationResponse getById(String id) {
        return notificationMapper.toResponse(findOrThrow(id));
    }

    @Override
    public void deleteNotification(String id) {
        Notification notification = findOrThrow(id);
        notification.setDeleted(true);
        notification.setStatus(NotificationStatus.DELETED);
        notificationRepository.save(notification);
    }

    @Override
    public void archiveNotification(String id) {
        Notification notification = findOrThrow(id);
        notification.setStatus(NotificationStatus.EXPIRED);
        notificationRepository.save(notification);
    }

    @Override
    public NotificationResponse markAsRead(String id) {
        Notification notification = findOrThrow(id);
        boolean wasUnread = notification.getReadAt() == null;
        notification.setReadAt(Instant.now());
        notification.setStatus(NotificationStatus.READ);
        notification = notificationRepository.save(notification);

        if (wasUnread && notification.getReceiverMobile() != null) {
            long unread = unreadCountService.decrement(notification.getReceiverMobile());
            webSocketNotifier.sendUnreadCount(notification.getReceiverMobile(), unread);
        }
        return notificationMapper.toResponse(notification);
    }

    @Override
    public void markAllAsRead(String mobile) {
        List<Notification> unread = notificationRepository
                .findByReceiverMobileAndCreatedAtAfterAndDeletedFalse(mobile, Instant.EPOCH)
                .stream().filter(n -> n.getReadAt() == null).collect(Collectors.toList());
        Instant now = Instant.now();
        unread.forEach(n -> {
            n.setReadAt(now);
            n.setStatus(NotificationStatus.READ);
        });
        notificationRepository.saveAll(unread);
        unreadCountService.reset(mobile);
        webSocketNotifier.sendUnreadCount(mobile, 0);
    }

    @Override
    public NotificationResponse markAsUnread(String id) {
        Notification notification = findOrThrow(id);
        boolean wasRead = notification.getReadAt() != null;
        notification.setReadAt(null);
        notification.setStatus(NotificationStatus.DELIVERED);
        notification = notificationRepository.save(notification);

        if (wasRead && notification.getReceiverMobile() != null) {
            long unread = unreadCountService.increment(notification.getReceiverMobile());
            webSocketNotifier.sendUnreadCount(notification.getReceiverMobile(), unread);
        }
        return notificationMapper.toResponse(notification);
    }

    @Override
    public UnreadCountResponse getUnreadCount(String mobile) {
        Query query = new Query();
        query.addCriteria(Criteria.where("receiverMobile").is(mobile)
                .and("deleted").ne(true)
                .and("readAt").is(null)
                .and("status").nin(List.of(NotificationStatus.EXPIRED, NotificationStatus.DELETED)));
        long fromDb = mongoTemplate.count(query, Notification.class);
        unreadCountService.set(mobile, fromDb);
        return UnreadCountResponse.builder().unreadCount(fromDb).build();
    }

    /**
     * Delivers any notifications created while the user was offline, called by the
     * websocket connect listener / a dedicated "sync" endpoint on reconnect.
     */
    public void deliverPendingOnReconnect(String mobile) {
        List<Notification> pending = notificationRepository
                .findByReceiverMobileAndCreatedAtAfterAndDeletedFalse(mobile, Instant.now().minusSeconds(86400))
                .stream().filter(n -> n.getReadAt() == null).collect(Collectors.toList());
        pending.forEach(n -> webSocketNotifier.sendToUser(mobile, notificationMapper.toResponse(n)));
    }

    private void dispatch(Notification notification) {
        for (NotificationSender sender : senders) {
            try {
                sender.send(notification);
            } catch (Exception e) {
                log.error("Sender {} failed for notification {}: {}",
                        sender.getChannel(), notification.getId(), e.getMessage());
            }
        }
    }

    private NotificationChannel defaultChannel(NotificationRequest request) {
        return request.getChannel() != null ? request.getChannel() : NotificationChannel.PUSH;
    }

    private Notification findOrThrow(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }
}
