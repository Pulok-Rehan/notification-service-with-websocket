package com.company.notification.redis;

public final class RedisKeys {
    private RedisKeys() {}

    public static final String ONLINE_USERS_SET = "online_users";
    public static final String TODAY_ACTIVE_USERS_SET = "today_active_users";

    public static String session(String platformId) {
        return "platform:session:" + platformId;
    }

    public static String presence(String platformId) {
        return "platform:presence:" + platformId;
    }

    public static String unreadCount(String platformId) {
        return "unread_count:" + platformId;
    }

    public static String topicMembers(String topic) {
        return "topic_members:" + topic;
    }
}
