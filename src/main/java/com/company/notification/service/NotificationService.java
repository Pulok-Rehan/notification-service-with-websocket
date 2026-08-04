package com.company.notification.service;

import com.company.notification.dto.*;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendUnicast(NotificationRequest request);

    List<NotificationResponse> sendMulticast(NotificationRequest request);

    NotificationResponse sendBroadcast(NotificationRequest request);

    NotificationResponse sendToTopic(NotificationRequest request);

    List<NotificationResponse> sendToRole(NotificationRequest request);

    NotificationResponse schedule(NotificationRequest request);

    PageResponse<NotificationResponse> getHistory(String mobile, NotificationHistoryFilter filter);

    NotificationResponse getById(String id);

    void deleteNotification(String id);

    void archiveNotification(String id);

    NotificationResponse markAsRead(String id);

    void markAllAsRead(String mobile);

    NotificationResponse markAsUnread(String id);

    UnreadCountResponse getUnreadCount(String mobile);
}
