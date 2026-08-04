package com.company.notification.dto;

import lombok.*;

import java.util.Map;

/**
 * Message frame actually pushed to WebSocket clients for live data updates
 * (mirrors DatabaseChangeEvent but shaped for the frontend).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketUpdateMessage {
    private String eventType;
    private String module;
    private String entityId;
    private Map<String, Object> payload;
}
