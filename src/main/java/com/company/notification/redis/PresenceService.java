package com.company.notification.redis;

import com.company.notification.dto.OnlineStatsResponse;
import com.company.notification.dto.PresenceResponse;
import com.company.notification.enums.PresenceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Tracks online/offline/away/busy presence and the live online-user set in Redis.
 * Backs GET /presence/* and the "online users count" requirement.
 */
@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redisTemplate;

    public void markOnline(String platformId, String sessionId, String device, String ip) {
        redisTemplate.opsForValue().set(RedisKeys.session(platformId), sessionId, 24, TimeUnit.HOURS);
        redisTemplate.opsForSet().add(RedisKeys.ONLINE_USERS_SET, platformId);
        redisTemplate.opsForSet().add(todayKey(), platformId);
        redisTemplate.expire(todayKey(), 1, TimeUnit.DAYS);
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "status", PresenceStatus.ONLINE.name());
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "lastSeen", Instant.now().toString());
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "device", device == null ? "" : device);
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "ip", ip == null ? "" : ip);
    }

    public void markOffline(String platformId) {
        redisTemplate.opsForSet().remove(RedisKeys.ONLINE_USERS_SET, platformId);
        redisTemplate.delete(RedisKeys.session(platformId));
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "status", PresenceStatus.OFFLINE.name());
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "lastSeen", Instant.now().toString());
    }

    public void updateStatus(String platformId, PresenceStatus status) {
        redisTemplate.opsForHash().put(RedisKeys.presence(platformId), "status", status.name());
    }

    public boolean isOnline(String platformId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisKeys.ONLINE_USERS_SET, platformId));
    }

    public PresenceResponse getPresence(String platformId) {
        Object status = redisTemplate.opsForHash().get(RedisKeys.presence(platformId), "status");
        Object lastSeen = redisTemplate.opsForHash().get(RedisKeys.presence(platformId), "lastSeen");
        Object device = redisTemplate.opsForHash().get(RedisKeys.presence(platformId), "device");
        return PresenceResponse.builder()
                .platformId(platformId)
                .status(status == null ? PresenceStatus.OFFLINE : PresenceStatus.valueOf(status.toString()))
                .lastSeen(lastSeen == null ? null : Instant.parse(lastSeen.toString()))
                .device(device == null ? null : device.toString())
                .build();
    }

    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(RedisKeys.ONLINE_USERS_SET);
    }

    public OnlineStatsResponse getStats(long totalRegisteredUsers) {
        long online = sizeOf(RedisKeys.ONLINE_USERS_SET);
        long todayActive = sizeOf(todayKey());
        return OnlineStatsResponse.builder()
                .onlineUsers(online)
                .offlineUsers(Math.max(totalRegisteredUsers - online, 0))
                .todayActiveUsers(todayActive)
                .build();
    }

    private long sizeOf(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0 : size;
    }

    private String todayKey() {
        String day = LocalDate.now(ZoneOffset.UTC).toString();
        return RedisKeys.TODAY_ACTIVE_USERS_SET + ":" + day;
    }
}
