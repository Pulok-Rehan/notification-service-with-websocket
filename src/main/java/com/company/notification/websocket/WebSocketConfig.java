package com.company.notification.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * STOMP over WebSocket (with SockJS fallback) configuration.
 *
 * Topics:
 *   /topic/global, /topic/{module} (attendance, deposit, withdraw, ipo, dashboard, system...)
 *   /user/{platformId}/notifications, /user/{platformId}/updates  (user-specific queues)
 *
 * Clients connect to ws://host/ws (or /ws with SockJS) sending "platformId: <id>" as a
 * STOMP CONNECT header; PlatformAuthChannelInterceptor resolves it to the STOMP principal
 * that makes the /user/{platformId}/... destinations and presence tracking work.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

//    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    private final PlatformAuthChannelInterceptor platformAuthChannelInterceptor;
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

//    @Override
//    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {

    /// /        registration.interceptors(authChannelInterceptor);
//    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(platformAuthChannelInterceptor);
    }
}
