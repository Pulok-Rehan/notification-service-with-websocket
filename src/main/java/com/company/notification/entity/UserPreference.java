package com.company.notification.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_preferences")
public class UserPreference {

    @Id
    private String id;

    @Indexed(unique = true)
    private String platformId;

    @Builder.Default private boolean push = true;
    @Builder.Default private boolean websocket = true;
    @Builder.Default private boolean email = false;
    @Builder.Default private boolean sms = false;
    @Builder.Default private boolean marketing = true;
    @Builder.Default private boolean transaction = true;
    @Builder.Default private boolean attendance = true;
}
