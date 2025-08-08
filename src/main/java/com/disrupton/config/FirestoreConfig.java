package com.disrupton.config;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
@Order(2)
public class FirestoreConfig {

    @Bean
    @Primary
    @DependsOn("firebaseConfig") // Asegurar que FirebaseConfig se ejecute primero
    public Firestore firestore() {
        try {
            // Verificar que Firebase esté inicializado
            if (FirebaseApp.getApps().isEmpty()) {
                throw new IllegalStateException("Firebase no está inicializado. Verifica FirebaseConfig.");
            }

            log.info("🔥 Creando bean de Firestore...");
            Firestore firestore = FirestoreClient.getFirestore();
            log.info("✅ Bean de Firestore creado correctamente");

            return firestore;

        } catch (Exception e) {
            log.error("❌ Error al crear el bean de Firestore: {}", e.getMessage());
            throw new RuntimeException("Failed to create Firestore bean", e);
        }
    }
} 