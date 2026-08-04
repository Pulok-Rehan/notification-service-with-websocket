package com.company.notification.listener;

import com.company.notification.dto.DatabaseChangeEvent;
import com.company.notification.dto.WebSocketUpdateMessage;
import com.company.notification.websocket.WebSocketNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * THE most important feature in the spec: consumes generic domain events published by
 * ANY upstream microservice (Attendance, Deposit, Withdrawal, IPO, KYC, Profile, ...) the
 * moment their own database changes, and fans them out over WebSocket so the frontend
 * updates live WITHOUT polling APIs.
 *
 * Producers just publish a DatabaseChangeEvent-shaped JSON to the "notification.events"
 * topic - no per-module code is required here or in NotificationServiceImpl; the routing
 * is purely by the "module" field, e.g. payload.module = "attendance" -> /topic/attendance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseChangeEventListener {

    private final WebSocketNotifier webSocketNotifier;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notification.events", groupId = "notification-service")
    public void onDatabaseChangeEvent(String message) {
        try {
            DatabaseChangeEvent event = objectMapper.readValue(message, DatabaseChangeEvent.class);
            log.info("Received DB change event: module={} type={} entityId={}",
                    event.getModule(), event.getEventType(), event.getEntityId());

            WebSocketUpdateMessage update = WebSocketUpdateMessage.builder()
                    .eventType(event.getEventType())
                    .module(event.getModule())
                    .entityId(event.getEntityId())
                    .payload(event.getPayload())
                    .build();

            // Module-wide live feed (e.g. dashboards/lists watching /topic/attendance)
            webSocketNotifier.sendModuleUpdate(event.getModule(), update);

            // Direct push to the specific user if this change targets one (mobileNumber)
            if (event.getMobileNumber() != null && !event.getMobileNumber().isBlank()) {
                webSocketNotifier.sendUserUpdate(event.getMobileNumber(), update);
            }
        } catch (Exception e) {
            log.error("Failed to process database change event: {}", e.getMessage(), e);
        }
    }
}
