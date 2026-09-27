package com.company.notification.service;

import com.company.notification.dto.NotificationRequest;
import com.company.notification.entity.Notification;
import com.company.notification.enums.NotificationStatus;
import com.company.notification.mapper.NotificationMapper;
import com.company.notification.redis.PresenceService;
import com.company.notification.redis.UnreadCountService;
import com.company.notification.repository.NotificationRepository;
import com.company.notification.service.impl.NotificationServiceImpl;
import com.company.notification.websocket.WebSocketNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private NotificationSender pushSender;
    @Mock private UnreadCountService unreadCountService;
    @Mock private WebSocketNotifier webSocketNotifier;
    @Mock private PresenceService presenceService;
    @Mock private RoleDirectoryService roleDirectoryService;
    @Mock private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository, notificationMapper, List.of(pushSender),
                unreadCountService, webSocketNotifier, presenceService, roleDirectoryService, mongoTemplate);
    }

    @Test
    void sendUnicast_persistsAndIncrementsUnreadCount() {
        NotificationRequest request = NotificationRequest.builder()
                .title("Hello").body("World").receiverPlatformId("8801711000000").build();

        Notification entity = Notification.builder().receiverPlatformId("8801711000000").build();
        when(notificationMapper.toEntity(request)).thenReturn(entity);
        when(notificationRepository.save(any())).thenReturn(entity);
        when(unreadCountService.increment("8801711000000")).thenReturn(1L);
        when(notificationMapper.toResponse(any())).thenReturn(
                com.company.notification.dto.NotificationResponse.builder().receiverPlatformId("8801711000000").build());

        notificationService.sendUnicast(request);

        verify(pushSender, times(1)).send(any());
        verify(unreadCountService, times(1)).increment("8801711000000");
        verify(webSocketNotifier, times(1)).sendUnreadCount("8801711000000", 1L);
        assertEquals(NotificationStatus.SENT, entity.getStatus());
    }
}
