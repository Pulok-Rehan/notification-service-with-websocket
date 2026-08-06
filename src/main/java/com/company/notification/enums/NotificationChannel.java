package com.company.notification.enums;

/**
 * Delivery channel. EMAIL, SMS, WHATSAPP, APNS, WEB_PUSH are reserved for future
 * implementation - see com.company.notification.service.NotificationSender for the
 * extension point (Strategy pattern) that allows adding new channels without
 * touching existing notification logic.
 */
public enum NotificationChannel {
    PUSH, WEBSOCKET, IN_APP, EMAIL, SMS, WHATSAPP, APNS, WEB_PUSH, CLIENT_PORTAL
}
