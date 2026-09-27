package com.company.notification.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Fast Redis-backed unread counter (mirrors MongoDB truth, used for instant reads).
 * On any mismatch the MongoDB count (NotificationRepository) is the source of truth;
 * this is purely a performance cache invalidated/incremented on every mutation.
 */
@Service
@RequiredArgsConstructor
public class UnreadCountService {

    private final StringRedisTemplate redisTemplate;

    public long increment(String platformId) {
        Long val = redisTemplate.opsForValue().increment(RedisKeys.unreadCount(platformId));
        return val == null ? 0 : val;
    }

    public long decrement(String platformId) {
        Long val = redisTemplate.opsForValue().decrement(RedisKeys.unreadCount(platformId));
        if (val != null && val < 0) {
            redisTemplate.opsForValue().set(RedisKeys.unreadCount(platformId), "0");
            return 0;
        }
        return val == null ? 0 : val;
    }

    public void set(String platformId, long count) {
        redisTemplate.opsForValue().set(RedisKeys.unreadCount(platformId), String.valueOf(count));
    }

    public void reset(String platformId) {
        redisTemplate.opsForValue().set(RedisKeys.unreadCount(platformId), "0");
    }

    public long get(String platformId) {
        String val = redisTemplate.opsForValue().get(RedisKeys.unreadCount(platformId));
        return val == null ? 0 : Long.parseLong(val);
    }
}
