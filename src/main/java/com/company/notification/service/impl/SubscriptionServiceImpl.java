package com.company.notification.service.impl;

import com.company.notification.dto.SubscriptionRequest;
import com.company.notification.entity.Subscription;
import com.company.notification.repository.SubscriptionRepository;
import com.company.notification.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void subscribe(SubscriptionRequest request) {
        if (!subscriptionRepository.existsByMobileAndTopic(request.getMobile(), request.getTopic())) {
            subscriptionRepository.save(Subscription.builder()
                    .mobile(request.getMobile())
                    .topic(request.getTopic())
                    .subscribedAt(Instant.now())
                    .build());
        }
    }

    @Override
    public void unsubscribe(SubscriptionRequest request) {
        subscriptionRepository.deleteByMobileAndTopic(request.getMobile(), request.getTopic());
    }

    @Override
    public List<String> listSubscriptions(String mobile) {
        return subscriptionRepository.findByMobile(mobile).stream().map(Subscription::getTopic).toList();
    }
}
