package com.company.notification.service;

import com.company.notification.dto.SubscriptionRequest;

import java.util.List;

public interface SubscriptionService {
    void subscribe(SubscriptionRequest request);
    void unsubscribe(SubscriptionRequest request);
    List<String> listSubscriptions(String mobile);
}
