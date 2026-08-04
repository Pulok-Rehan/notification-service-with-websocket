package com.company.notification.publisher;

import com.company.notification.dto.DatabaseChangeEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Convenience publisher for this service's own admin/internal flows that want to emit
 * a DatabaseChangeEvent (e.g. POST /admin/broadcast triggering a dashboard refresh).
 * Upstream microservices (Attendance, Deposit, etc.) publish to the same
 * "notification.events" topic directly from their own codebase using this exact JSON shape.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(DatabaseChangeEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("notification.events", event.getModule(), json);
        } catch (Exception e) {
            log.error("Failed to publish database change event: {}", e.getMessage(), e);
        }
    }
}
