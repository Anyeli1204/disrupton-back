package com.disrupton.user.controller;

import com.disrupton.service.FirebaseAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final FirebaseAnalyticsService firebaseAnalyticsService;

    /**
     * Get analytics metrics
     */
    @GetMapping("/metrics/{metricName}")
    public ResponseEntity<?> getAnalyticsMetric(@PathVariable String metricName) {
        try {
            log.info("Getting analytics metric: {}", metricName);
            Object metric = firebaseAnalyticsService.getMetrics(metricName);
            return ResponseEntity.ok(metric);
        } catch (Exception e) {
            log.error("Error getting analytics metric: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener métrica de analytics");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get user engagement statistics
     */
    @GetMapping("/user/{userId}/engagement")
    public ResponseEntity<?> getUserEngagementStats(@PathVariable String userId) {
        try {
            log.info("Getting user engagement stats for user: {}", userId);
            Map<String, Object> stats = firebaseAnalyticsService.getUserEngagementStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting user engagement stats: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas de engagement del usuario");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get cultural object statistics
     */
    @GetMapping("/object/{objectId}/stats")
    public ResponseEntity<?> getCulturalObjectStats(@PathVariable String objectId) {
        try {
            log.info("Getting cultural object stats for object: {}", objectId);
            Map<String, Object> stats = firebaseAnalyticsService.getCulturalObjectStats(objectId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting cultural object stats: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas del objeto cultural");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Track cultural object view
     */
    @PostMapping("/track/view")
    public ResponseEntity<?> trackCulturalObjectView(
            @RequestParam String objectId,
            @RequestParam String userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        try {
            log.info("Tracking cultural object view - Object: {}, User: {}", objectId, userId);
            firebaseAnalyticsService.saveCulturalObjectView(objectId, userId, latitude, longitude);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cultural object view tracked successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error tracking cultural object view: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al registrar vista del objeto cultural");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Track AR session
     */
    @PostMapping("/track/ar-session")
    public ResponseEntity<?> trackARSession(
            @RequestParam String userId,
            @RequestParam String zoneId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Long duration) {
        
        try {
            log.info("Tracking AR session - User: {}, Zone: {}", userId, zoneId);
            firebaseAnalyticsService.saveARSession(userId, zoneId, latitude, longitude, duration);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "AR session tracked successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error tracking AR session: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al registrar sesión AR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get available metrics
     */
    @GetMapping("/available-metrics")
    public ResponseEntity<?> getAvailableMetrics() {
        Map<String, String> metrics = new HashMap<>();
        metrics.put("total_events", "Total de eventos");
        metrics.put("events_today", "Eventos de hoy");
        metrics.put("events_this_week", "Eventos de esta semana");
        metrics.put("events_by_type", "Eventos por tipo");
        metrics.put("user_engagement", "Engagement de usuarios");
        
        return ResponseEntity.ok(metrics);
    }
}
