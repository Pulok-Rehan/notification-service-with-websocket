package com.company.notification.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Programmatic index creation so a fresh deployment gets correct indexes without a
 * manual migration step. (Equivalent mongosh commands are also in
 * docs/mongo-indexes.js for reference / Atlas console use.)
 */
@Component
@RequiredArgsConstructor
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        mongoTemplate.indexOps("notifications")
                .ensureIndex(new Index().on("receiverPlatformId", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("createdAt", org.springframework.data.domain.Sort.Direction.DESC));
        mongoTemplate.indexOps("notifications")
                .ensureIndex(new Index().on("status", org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps("notifications")
                .ensureIndex(new Index().on("topic", org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps("fcm_tokens")
                .ensureIndex(new Index().on("platformId", org.springframework.data.domain.Sort.Direction.ASC));
        mongoTemplate.indexOps("subscriptions")
                .ensureIndex(new Index().on("platformId", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("topic", org.springframework.data.domain.Sort.Direction.ASC).unique());
    }
}
