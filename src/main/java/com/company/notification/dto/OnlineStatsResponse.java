package com.company.notification.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineStatsResponse {
    private long onlineUsers;
    private long offlineUsers;
    private long todayActiveUsers;
}
