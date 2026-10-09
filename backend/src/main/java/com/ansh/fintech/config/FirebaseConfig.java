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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        String envJson = System.getenv("FIREBASE_KEY_JSON");
        if (envJson == null || envJson.isBlank()) {
            envJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        }

        if (emulatorHost != null && !emulatorHost.isEmpty()) {
            log.info("FIRESTORE_EMULATOR_HOST found ({}). Initializing Firebase with dummy credentials for local emulator.", emulatorHost);
            optionsBuilder.setCredentials(GoogleCredentials.newBuilder().build());
            optionsBuilder.setProjectId("demo-fintech-project");
        } else if (envJson != null && !envJson.isBlank()) {
            log.info("Loading Firebase credentials from environment variable FIREBASE_KEY_JSON.");
            String rawJson = envJson.trim();
            if (!rawJson.startsWith("{") && rawJson.length() > 50) {
                // Handle Base64 encoded JSON
                rawJson = new String(Base64.getDecoder().decode(rawJson), StandardCharsets.UTF_8);
            }
            try (InputStream is = new ByteArrayInputStream(rawJson.getBytes(StandardCharsets.UTF_8))) {
                optionsBuilder.setCredentials(GoogleCredentials.fromStream(is));
            }
        } else {
            File primaryFile = new File(serviceAccountPath);
            File renderSecretFile1 = new File("/etc/secrets/firebase-key.json");
            File renderSecretFile2 = new File("/etc/secrets/firebase-key.json.json");

            File targetFile = null;
            if (primaryFile.exists()) {
                targetFile = primaryFile;
            } else if (renderSecretFile1.exists()) {
                targetFile = renderSecretFile1;
            } else if (renderSecretFile2.exists()) {
                targetFile = renderSecretFile2;
            }

            if (targetFile != null) {
                log.info("Loading Firebase credentials from file: {}", targetFile.getAbsolutePath());
                try (FileInputStream serviceAccount = new FileInputStream(targetFile)) {
                    optionsBuilder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
                }
            } else {
                log.warn("Firebase key file not found at '{}' or '/etc/secrets/'. Attempting Application Default Credentials.", serviceAccountPath);
                try {
                    optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
                } catch (Exception e) {
                    log.error("Could not load Google Credentials. Firebase Admin SDK will fail queries without a valid Service Account key file or FIREBASE_KEY_JSON environment variable!", e);
                    optionsBuilder.setCredentials(GoogleCredentials.newBuilder().build());
                    optionsBuilder.setProjectId("fintech-app-a8517");
                }
            }
        }

        return FirebaseApp.initializeApp(optionsBuilder.build());
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
