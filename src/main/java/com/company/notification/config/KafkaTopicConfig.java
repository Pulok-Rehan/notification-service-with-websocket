package com.company.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Auto-creates the inbound topic that upstream services publish DatabaseChangeEvent /
 * domain events to. Producers (Attendance Service, etc.) just need to publish to
 * "notification.events" with the DatabaseChangeEvent JSON shape - no per-module topic
 * needed, fan-out to /topic/{module} happens inside this service.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("notification.events").partitions(6).replicas(1).build();
    }
}
