package com.disrupton.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        log.info("🚀 Inicializando Firebase Admin SDK...");
        
        try {
            // Intentar cargar desde el archivo de recursos
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
            
            if (serviceAccount == null) {
                // Si no está en recursos, intentar desde el sistema de archivos
                serviceAccount = new FileInputStream("src/main/resources/firebase-service-account.json");
            }
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("disrupton2025.appspot.com") // Cambiar por tu bucket
                    .setProjectId("disrupton2025") // Cambiar por tu project ID
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK inicializado correctamente");
            } else {
                log.info("ℹ️ Firebase Admin SDK ya estaba inicializado");
            }
            
        } catch (IOException e) {
            log.error("❌ Error al inicializar Firebase Admin SDK: {}", e.getMessage());
            throw e;
        }
    }
} 