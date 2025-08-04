package com.disrupton.cultural.controller;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/firebase")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FirebaseCulturalController {
    
    private final Firestore firestore;
    
    /**
     * Endpoint de prueba para verificar la conectividad con Firebase
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        try {
            // Intentar una operación simple para verificar la conexión
            firestore.collection("test").document("health").get();
            return ResponseEntity.ok("✅ Firebase está funcionando correctamente");
        } catch (Exception e) {
            log.error("❌ Error al conectar con Firebase: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Error de conexión con Firebase: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint para obtener información del proyecto
     */
    @GetMapping("/info")
    public ResponseEntity<String> getProjectInfo() {
        try {
            String projectId = firestore.getOptions().getProjectId();
            
            String info = String.format("""
                📁 Proyecto ID: %s
                ✅ Estado: Conectado
                """, projectId);
                
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("❌ Error al obtener información del proyecto: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("❌ Error: " + e.getMessage());
        }
    }
} 