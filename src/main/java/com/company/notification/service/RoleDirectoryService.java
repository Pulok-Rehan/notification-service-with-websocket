package com.company.notification.service;

import java.util.List;

/**
 * Resolves role -> platformIds. Backed by a cached call to the upstream User Service.
 * Swap the impl to call the real User Service REST API; for now it's cache-only so the
 * Notification Service can run standalone and roles can be seeded/synced via
 * POST /admin or a Kafka "role.updated" event in the future.
 */
public interface RoleDirectoryService {
    List<String> getPlatformIdsByRole(String role);
    void cacheRoleMapping(String role, List<String> platformIds);
}
