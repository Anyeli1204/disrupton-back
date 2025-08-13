package com.disrupton.user.controller;

import com.disrupton.user.dto.*;
import com.disrupton.service.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics-dashboard")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsDashboardController {

    private final DashboardAnalyticsService dashboardAnalyticsService;

    /**
     * Get comprehensive dashboard metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getDashboardMetrics(
            @RequestParam(defaultValue = "week") String timeRange) {
        
        log.info("Dashboard metrics requested for time range: {}", timeRange);
        
        try {
            DashboardMetricsDto metrics = dashboardAnalyticsService.getDashboardMetrics(timeRange);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting dashboard metrics: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener métricas del dashboard");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get most viewed cultural objects
     */
    @GetMapping("/objects/most-viewed")
    public ResponseEntity<?> getMostViewedObjects(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Most viewed objects requested - timeRange: {}, limit: {}", timeRange, limit);
        
        try {
            if (limit > 50) {
                limit = 50; // Limit to prevent performance issues
            }
            
            var objects = dashboardAnalyticsService.getMostViewedObjects(timeRange, limit);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("Error getting most viewed objects: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener objetos más vistos");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get campus zone activity
     */
    @GetMapping("/zones/activity")
    public ResponseEntity<?> getCampusZoneActivity(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Campus zone activity requested - timeRange: {}, limit: {}", timeRange, limit);
        
        try {
            if (limit > 20) {
                limit = 20; // Limit to prevent performance issues
            }
            
            var zones = dashboardAnalyticsService.getCampusZoneActivity(timeRange, limit);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            log.error("Error getting campus zone activity: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener actividad de zonas del campus");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get theme interactions
     */
    @GetMapping("/themes/interactions")
    public ResponseEntity<?> getThemeInteractions(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Theme interactions requested - timeRange: {}, limit: {}", timeRange, limit);
        
        try {
            if (limit > 20) {
                limit = 20; // Limit to prevent performance issues
            }
            
            var themes = dashboardAnalyticsService.getThemeInteractions(timeRange, limit);
            return ResponseEntity.ok(themes);
        } catch (Exception e) {
            log.error("Error getting theme interactions: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener interacciones por temas");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get overall statistics
     */
    @GetMapping("/stats/overall")
    public ResponseEntity<?> getOverallStats(
            @RequestParam(defaultValue = "week") String timeRange) {
        
        log.info("Overall stats requested for time range: {}", timeRange);
        
        try {
            var stats = dashboardAnalyticsService.getOverallStats(timeRange);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting overall stats: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas generales");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get user activity
     */
    @GetMapping("/users/activity")
    public ResponseEntity<?> getUserActivity(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("User activity requested - timeRange: {}, limit: {}", timeRange, limit);
        
        try {
            if (limit > 50) {
                limit = 50; // Limit to prevent performance issues
            }
            
            var users = dashboardAnalyticsService.getUserActivity(timeRange, limit);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting user activity: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener actividad de usuarios");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get time range analytics
     */
    @GetMapping("/analytics/time-range")
    public ResponseEntity<?> getTimeRangeAnalytics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        log.info("Time range analytics requested from {} to {}", startDate, endDate);
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            if (start.isAfter(end)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Fecha de inicio debe ser anterior a fecha de fin");
                return ResponseEntity.badRequest().body(error);
            }
            
            var analytics = dashboardAnalyticsService.getTimeRangeAnalytics(start, end);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting time range analytics: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener analytics por rango de tiempo");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get available time ranges
     */
    @GetMapping("/time-ranges")
    public ResponseEntity<?> getAvailableTimeRanges() {
        Map<String, String> timeRanges = new HashMap<>();
        timeRanges.put("today", "Hoy");
        timeRanges.put("week", "Última semana");
        timeRanges.put("month", "Último mes");
        timeRanges.put("year", "Último año");
        timeRanges.put("all", "Todo el tiempo");
        
        return ResponseEntity.ok(timeRanges);
    }

    /**
     * Get dashboard summary (lightweight version)
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary() {
        log.info("Dashboard summary requested");
        
        try {
            Map<String, Object> summary = new HashMap<>();
            
            // Get key metrics for today
            var todayStats = dashboardAnalyticsService.getOverallStats("today");
            summary.put("activeUsersToday", todayStats.getActiveUsersToday());
            summary.put("totalViewsToday", todayStats.getTotalViews());
            
            // Get top 3 most viewed objects
            var topObjects = dashboardAnalyticsService.getMostViewedObjects("today", 3);
            summary.put("topObjectsToday", topObjects);
            
            // Get top 3 active zones
            var topZones = dashboardAnalyticsService.getCampusZoneActivity("today", 3);
            summary.put("topZonesToday", topZones);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting dashboard summary: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener resumen del dashboard");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Health check for dashboard service
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("service", "Dashboard Analytics");
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(health);
    }
}
