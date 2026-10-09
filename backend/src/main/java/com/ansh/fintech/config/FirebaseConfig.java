package com.ansh.fintech.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-path:firebase-key.json}")
    private String serviceAccountPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();
        
        String emulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST");
        File keyFile = new File(serviceAccountPath);

        if (emulatorHost != null && !emulatorHost.isEmpty()) {
            log.info("FIRESTORE_EMULATOR_HOST found ({}). Initializing Firebase with dummy credentials for local emulator.", emulatorHost);
            optionsBuilder.setCredentials(GoogleCredentials.newBuilder().build());
            optionsBuilder.setProjectId("demo-fintech-project");
        } else if (keyFile.exists()) {
            log.info("Loading Firebase credentials from key path: {}", serviceAccountPath);
            try (FileInputStream serviceAccount = new FileInputStream(keyFile)) {
                optionsBuilder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
            }
        } else {
            log.warn("Firebase key file not found at '{}' and FIRESTORE_EMULATOR_HOST not set. Falling back to Application Default Credentials.", serviceAccountPath);
            try {
                optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
            } catch (Exception e) {
                log.warn("Could not load Application Default Credentials. Using unauthenticated credentials for mock/test environment.");
                optionsBuilder.setCredentials(GoogleCredentials.newBuilder().build());
                optionsBuilder.setProjectId("demo-fintech-project");
            }
        }

        return FirebaseApp.initializeApp(optionsBuilder.build());
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
