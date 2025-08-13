package com.disrupton.user.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@RequireRole(UserRole.USER)
public class UserBasicController {

    /**
     * Dashboard de usuario básico
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getUserDashboard() {
        log.info("👤 Accediendo al dashboard de usuario básico");
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard de Usuario");
        dashboard.put("timestamp", System.currentTimeMillis());
        dashboard.put("user", true);
        dashboard.put("functions", new String[]{
            "Explorar contenido cultural básico",
            "Ver tours públicos",
            "Comentar y calificar",
            "Guardar favoritos",
            "Acceso limitado a AR"
        });
        dashboard.put("upgradeMessage", "¡Actualiza a Premium para más funciones!");
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtener tours públicos
     */
    @GetMapping("/public-tours")
    public ResponseEntity<Map<String, Object>> getPublicTours() {
        log.info("👤 Usuario solicitando tours públicos");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tours públicos disponibles");
        response.put("totalTours", 5);
        response.put("tours", new Object[]{
            Map.of("id", "public_1", "title", "Tour Básico del Centro", "duration", "1 hora", "price", "Gratis"),
            Map.of("id", "public_2", "title", "Visita a Plaza Mayor", "duration", "30 min", "price", "Gratis"),
            Map.of("id", "public_3", "title", "Paseo por el Malecón", "duration", "45 min", "price", "Gratis"),
            Map.of("id", "public_4", "title", "Museo de la Ciudad", "duration", "1.5 horas", "price", "Gratis"),
            Map.of("id", "public_5", "title", "Iglesia San Francisco", "duration", "20 min", "price", "Gratis")
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener contenido cultural básico
     */
    @GetMapping("/cultural-content")
    public ResponseEntity<Map<String, Object>> getBasicCulturalContent() {
        log.info("👤 Usuario solicitando contenido cultural básico");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido cultural básico");
        response.put("totalContent", 10);
        response.put("content", new Object[]{
            Map.of("id", "basic_1", "title", "Historia de la Ciudad", "type", "text", "preview", true),
            Map.of("id", "basic_2", "title", "Fotos Principales", "type", "gallery", "photos", 20),
            Map.of("id", "basic_3", "title", "Audio Tour Básico", "type", "audio", "duration", "15 min"),
            Map.of("id", "basic_4", "title", "Video Introductorio", "type", "video", "duration", "5 min")
        });
        response.put("upgradeNote", "Contenido premium disponible con suscripción");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Comentar en contenido
     */
    @PostMapping("/comment")
    public ResponseEntity<Map<String, Object>> addComment(
            @RequestParam String contentId,
            @RequestParam String comment,
            @RequestParam(defaultValue = "5") int rating) {
        
        log.info("👤 Usuario agregando comentario en contenido: {}", contentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Comentario agregado exitosamente");
        response.put("commentId", "comment_" + System.currentTimeMillis());
        response.put("contentId", contentId);
        response.put("comment", comment);
        response.put("rating", rating);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Guardar favorito
     */
    @PostMapping("/favorites")
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @RequestParam String contentId,
            @RequestParam String contentType) {
        
        log.info("👤 Usuario guardando favorito: {} de tipo {}", contentId, contentType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Agregado a favoritos");
        response.put("contentId", contentId);
        response.put("contentType", contentType);
        response.put("favoriteId", "fav_" + System.currentTimeMillis());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener favoritos del usuario
     */
    @GetMapping("/favorites")
    public ResponseEntity<Map<String, Object>> getFavorites() {
        log.info("👤 Usuario solicitando sus favoritos");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Favoritos del usuario");
        response.put("totalFavorites", 3);
        response.put("favorites", new Object[]{
            Map.of("id", "fav_1", "contentId", "basic_1", "title", "Historia de la Ciudad", "type", "text"),
            Map.of("id", "fav_2", "contentId", "public_1", "title", "Tour Básico del Centro", "type", "tour"),
            Map.of("id", "fav_3", "contentId", "basic_2", "title", "Fotos Principales", "type", "gallery")
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Acceso básico a AR
     */
    @GetMapping("/ar-basic")
    public ResponseEntity<Map<String, Object>> getBasicARExperience() {
        log.info("👤 Usuario solicitando experiencia AR básica");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Experiencia AR básica");
        response.put("availableFeatures", new String[]{
            "Información básica en AR",
            "Marcadores simples",
            "Fotos AR limitadas"
        });
        response.put("limitations", new String[]{
            "Sin reconstrucción 3D",
            "Sin guía virtual",
            "Sin experiencias inmersivas"
        });
        response.put("upgradeSuggestion", "Actualiza a Premium para experiencias AR completas");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Estadísticas básicas del usuario
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        log.info("👤 Usuario solicitando sus estadísticas básicas");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas del usuario");
        response.put("memberSince", "2024-11-15");
        response.put("totalTours", 8);
        response.put("totalComments", 12);
        response.put("totalFavorites", 5);
        response.put("arExperiences", 3);
        response.put("currentLevel", "Básico");
        response.put("nextUpgrade", "Premium");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Solicitar actualización a premium
     */
    @PostMapping("/upgrade-request")
    public ResponseEntity<Map<String, Object>> requestUpgrade() {
        log.info("👤 Usuario solicitando información de actualización");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Información de actualización a Premium");
        response.put("premiumFeatures", new String[]{
            "Tours exclusivos",
            "Contenido cultural premium",
            "Experiencias AR avanzadas",
            "Soporte VIP",
            "Descargas ilimitadas"
        });
        response.put("pricing", Map.of(
            "monthly", 29.99,
            "yearly", 299.99,
            "lifetime", 999.99
        ));
        response.put("contactEmail", "premium@disrupton.com");
        
        return ResponseEntity.ok(response);
    }
}
