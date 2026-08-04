package com.company.notification.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * STOMP over WebSocket (with SockJS fallback) configuration.
 *
 * Topics:
 *   /topic/global, /topic/{module} (attendance, deposit, withdraw, ipo, dashboard, system...)
 *   /user/{mobile}/notifications, /user/{mobile}/updates  (user-specific queues)
 *
 * Clients connect to ws://host/ws (or /ws with SockJS) sending "Authorization: Bearer <jwt>"
 * either as a STOMP CONNECT header or query param; JwtHandshakeInterceptor + the channel
 * interceptor in WebSocketAuthChannelInterceptor extract mobileNumber from the token.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor authChannelInterceptor;
    private final PresenceWebSocketEventListener presenceEventListener; // ensures bean wiring/eager init

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
