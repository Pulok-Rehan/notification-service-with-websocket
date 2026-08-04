package com.company.notification.redis;

public final class RedisKeys {
    private RedisKeys() {}

    public static final String ONLINE_USERS_SET = "online_users";
    public static final String TODAY_ACTIVE_USERS_SET = "today_active_users";

    public static String session(String mobile) {
        return "mobile:session:" + mobile;
    }

    public static String presence(String mobile) {
        return "mobile:presence:" + mobile;
    }

    public static String unreadCount(String mobile) {
        return "unread_count:" + mobile;
    }

    public static String topicMembers(String topic) {
        return "topic_members:" + topic;
    }
}
