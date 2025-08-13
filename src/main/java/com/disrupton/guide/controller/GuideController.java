package com.disrupton.guide.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/guide")
@RequiredArgsConstructor
@Slf4j
@RequireRole({UserRole.GUIDE, UserRole.ADMIN})
public class GuideController {

    /**
     * Dashboard de guía turístico
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getGuideDashboard() {
        log.info("🗺️ Accediendo al dashboard de guía turístico");
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard de Guía Turístico");
        dashboard.put("timestamp", System.currentTimeMillis());
        dashboard.put("guide", true);
        dashboard.put("functions", new String[]{
            "Crear tours virtuales",
            "Gestionar contenido cultural",
            "Interactuar con turistas",
            "Subir fotos y videos",
            "Crear rutas personalizadas"
        });
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Crear nuevo tour virtual
     */
    @PostMapping("/tours")
    public ResponseEntity<Map<String, Object>> createTour(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location) {
        
        log.info("🗺️ Guía creando tour: {} en {}", title, location);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tour creado exitosamente");
        response.put("tourId", "tour_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("description", description);
        response.put("location", location);
        response.put("createdBy", "guide");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener tours del guía
     */
    @GetMapping("/tours")
    public ResponseEntity<Map<String, Object>> getMyTours() {
        log.info("🗺️ Guía solicitando sus tours");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tours del guía");
        response.put("totalTours", 3);
        response.put("activeTours", 2);
        response.put("completedTours", 1);
        response.put("tours", new Object[]{
            Map.of("id", "tour_1", "title", "Tour Histórico del Centro", "status", "active"),
            Map.of("id", "tour_2", "title", "Ruta Gastronómica", "status", "active"),
            Map.of("id", "tour_3", "title", "Arquitectura Colonial", "status", "completed")
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Subir contenido cultural
     */
    @PostMapping("/cultural-content")
    public ResponseEntity<Map<String, Object>> uploadCulturalContent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam String location) {
        
        log.info("📸 Guía subiendo contenido cultural: {} de tipo {}", title, type);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido cultural subido exitosamente");
        response.put("contentId", "content_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("description", description);
        response.put("type", type);
        response.put("location", location);
        response.put("uploadedBy", "guide");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Crear ruta personalizada
     */
    @PostMapping("/routes")
    public ResponseEntity<Map<String, Object>> createCustomRoute(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String[] stops) {
        
        log.info("🛤️ Guía creando ruta personalizada: {} con {} paradas", name, stops.length);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ruta personalizada creada");
        response.put("routeId", "route_" + System.currentTimeMillis());
        response.put("name", name);
        response.put("description", description);
        response.put("stops", stops);
        response.put("totalStops", stops.length);
        response.put("createdBy", "guide");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener estadísticas del guía
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGuideStats() {
        log.info("📊 Guía solicitando sus estadísticas");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas del guía");
        response.put("totalTours", 15);
        response.put("totalVisitors", 234);
        response.put("averageRating", 4.8);
        response.put("totalContent", 45);
        response.put("activeRoutes", 5);
        response.put("earnings", 1250.50);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gestionar reservas de tours
     */
    @GetMapping("/bookings")
    public ResponseEntity<Map<String, Object>> getTourBookings() {
        log.info("📅 Guía solicitando reservas de tours");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reservas de tours");
        response.put("pendingBookings", 3);
        response.put("confirmedBookings", 8);
        response.put("completedBookings", 12);
        response.put("bookings", new Object[]{
            Map.of("id", "booking_1", "tour", "Tour Histórico", "visitors", 4, "date", "2025-01-15"),
            Map.of("id", "booking_2", "tour", "Ruta Gastronómica", "visitors", 2, "date", "2025-01-16")
        });
        
        return ResponseEntity.ok(response);
    }
}
