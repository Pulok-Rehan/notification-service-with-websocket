package com.company.notification.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${firebase.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.warn("Firebase push notifications are DISABLED via firebase.enabled=false");
            return;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = resolveCredentialsStream();
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase. Push notifications will not work until "
                    + "a valid service-account JSON is provided at '{}'. Error: {}", credentialsPath, e.getMessage());
        }
    }

    private InputStream resolveCredentialsStream() throws IOException {
        try {
            return new FileInputStream(credentialsPath);
        } catch (IOException e) {
            return new ClassPathResource(credentialsPath).getInputStream();
        }
    }
}
