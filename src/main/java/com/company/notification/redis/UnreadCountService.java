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

    public long increment(String mobile) {
        Long val = redisTemplate.opsForValue().increment(RedisKeys.unreadCount(mobile));
        return val == null ? 0 : val;
    }

    public long decrement(String mobile) {
        Long val = redisTemplate.opsForValue().decrement(RedisKeys.unreadCount(mobile));
        if (val != null && val < 0) {
            redisTemplate.opsForValue().set(RedisKeys.unreadCount(mobile), "0");
            return 0;
        }
        return val == null ? 0 : val;
    }

    public void set(String mobile, long count) {
        redisTemplate.opsForValue().set(RedisKeys.unreadCount(mobile), String.valueOf(count));
    }

    public void reset(String mobile) {
        redisTemplate.opsForValue().set(RedisKeys.unreadCount(mobile), "0");
    }

    public long get(String mobile) {
        String val = redisTemplate.opsForValue().get(RedisKeys.unreadCount(mobile));
        return val == null ? 0 : Long.parseLong(val);
    }
}
