package com.company.notification.websocket;

import com.company.notification.dto.NotificationResponse;
import com.company.notification.dto.WebSocketUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Single point of truth for pushing frames to STOMP destinations. Used both by the
 * notification send path and by the database-change-event listener (live data updates).
 */
@Component
@RequiredArgsConstructor
public class WebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String mobile, NotificationResponse notification) {
        messagingTemplate.convertAndSendToUser(mobile, "/notifications", notification);
    }

    public void sendUnreadCount(String mobile, long unreadCount) {
        messagingTemplate.convertAndSendToUser(mobile, "/updates", new java.util.HashMap<>() {{
            put("eventType", "UNREAD_COUNT");
            put("unreadCount", unreadCount);
        }});
    }

    public void broadcast(NotificationResponse notification) {
        messagingTemplate.convertAndSend("/topic/global", notification);
    }

    public void sendToTopic(String topic, NotificationResponse notification) {
        messagingTemplate.convertAndSend("/topic/" + topic, notification);
    }

    public void sendModuleUpdate(String module, WebSocketUpdateMessage message) {
        messagingTemplate.convertAndSend("/topic/" + module, message);
    }

    public void sendUserUpdate(String mobile, WebSocketUpdateMessage message) {
        messagingTemplate.convertAndSendToUser(mobile, "/updates", message);
    }
}
