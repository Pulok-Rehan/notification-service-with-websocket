package com.company.notification.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Reads the platformId from the STOMP CONNECT frame's native "platformId" header and sets
 * it as the Principal, so /user/{platformId}/queue/... destinations and presence tracking
 * key on platformId. Falls back to the legacy "mobile" header for older clients.
 */
@Component
public class PlatformAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            String platformId = accessor.getFirstNativeHeader("platformId");
            if (platformId == null || platformId.isBlank()) {
                platformId = accessor.getFirstNativeHeader("mobile"); // legacy clients
            }

            if (platformId != null && !platformId.isBlank()) {

                final String identity = platformId;
                accessor.setUser(new Principal() {
                    @Override
                    public String getName() {
                        return identity;
                    }
                });
            }
        }

        return message;
    }
}
