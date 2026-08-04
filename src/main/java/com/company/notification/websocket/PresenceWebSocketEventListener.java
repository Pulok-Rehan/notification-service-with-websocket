package com.company.notification.websocket;

import com.company.notification.redis.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

/**
 * Hooks WebSocket connect/disconnect lifecycle into Redis presence tracking
 * (connected/disconnected/lastSeen requirement).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresenceWebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String mobile = accessor.getUser().getName();
            presenceService.markOnline(mobile, accessor.getSessionId(), "unknown", "unknown");
            log.info("User connected: {}", mobile);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String mobile = accessor.getUser().getName();
            presenceService.markOffline(mobile);
            log.info("User disconnected: {}", mobile);
        }
    }
}
