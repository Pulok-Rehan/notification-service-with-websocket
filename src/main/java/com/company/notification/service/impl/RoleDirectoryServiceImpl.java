package com.company.notification.service.impl;

import com.company.notification.service.RoleDirectoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cache-backed role -> platformIds directory. In production replace getPlatformIdsByRole's
 * cache-miss path with a REST call to the User Service; the @Cacheable annotation means
 * callers (NotificationServiceImpl) never need to change.
 */
@Service
public class RoleDirectoryServiceImpl implements RoleDirectoryService {

    private final ConcurrentHashMap<String, List<String>> roleCache = new ConcurrentHashMap<>();

    @Override
    @Cacheable(value = "rolePlatformIds", key = "#role")
    public List<String> getPlatformIdsByRole(String role) {
        return roleCache.getOrDefault(role, List.of());
    }

    @Override
    @CacheEvict(value = "rolePlatformIds", key = "#role")
    public void cacheRoleMapping(String role, List<String> platformIds) {
        roleCache.put(role, new CopyOnWriteArrayList<>(platformIds));
    }
}
