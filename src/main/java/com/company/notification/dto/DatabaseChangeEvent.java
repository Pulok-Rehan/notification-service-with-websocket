package com.company.notification.dto;

import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * Generic event published by ANY upstream microservice whenever its own database
 * changes (Attendance, Deposit, Withdrawal, IPO, KYC, Profile, etc). The Notification
 * Service consumes this single, generic shape and fans it out over WebSocket
 * (/topic/{module}) and optionally Firebase - no per-module code required.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseChangeEvent {
    private String eventType;    // e.g. CREATED, UPDATED, APPROVED, REJECTED
    private String module;       // e.g. attendance, deposit, withdraw, ipo, kyc, profile
    private String entityId;
    private String platformId;     // target user, nullable for broadcast-style module events
    private Map<String, Object> payload;
    private Instant timestamp;
}
