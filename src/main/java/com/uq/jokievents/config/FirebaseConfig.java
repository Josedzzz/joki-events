package com.uq.jokievents.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Getter
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private final ApplicationConfig applicationConfig;

//    @Bean
//    public FirebaseApp initializeFirebase() throws IOException {
//        FileInputStream serviceAccount = new FileInputStream(
//                "src/main/resources/joki-events-img-repo-firebase-adminsdk-k2dzt-5632c3e6b6.json"
//        );
//
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .setStorageBucket("joki-events-img-repo.appspot.com")
//                .build();
//
//        if(FirebaseApp.getApps().isEmpty()) {
//            return FirebaseApp.initializeApp(options);
//        }
//        return null;
//    }

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        String firebaseConfigBase64 = applicationConfig.getFirebaseKey();
        if (firebaseConfigBase64 == null) {
            throw new IllegalArgumentException("FIREBASE_CONFIG environment variable is not set");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
        try (InputStream serviceAccount = new ByteArrayInputStream(decodedBytes)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("joki-events-img-repo.appspot.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            }
            return null;
        }
    }


}
