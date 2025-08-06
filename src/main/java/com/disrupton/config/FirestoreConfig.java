package com.disrupton.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Slf4j
public class FirestoreConfig {

    @Bean
    @DependsOn("firebaseConfig")
    public Firestore firestore() {
        try {
            log.info("🔍 Inicializando Firestore...");
            Firestore firestore = FirestoreClient.getFirestore();
            log.info("✅ Firestore inicializado correctamente");
            return firestore;
        } catch (Exception e) {
            log.error("❌ Error al inicializar Firestore: {}", e.getMessage());
            throw new RuntimeException("Error al inicializar Firestore", e);
        }
    }
} 