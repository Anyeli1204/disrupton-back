package com.disrupton.dashboard.controller;

import com.disrupton.dashboard.dto.*;
import com.disrupton.dashboard.service.DashboardAnalyticsService;
import com.disrupton.analytics.service.AnalyticsCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final AnalyticsCalculationService analyticsCalculationService;
    
    /**
     * Get comprehensive dashboard metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange) {
        
        try {
            log.info("Getting dashboard metrics for time range: {}", timeRange);
            
            DashboardMetricsDto metrics = dashboardAnalyticsService.getDashboardMetrics(timeRange);
            
            log.info("Dashboard metrics retrieved successfully");
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error getting dashboard metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get most viewed cultural objects
     */
    @GetMapping("/most-viewed")
    public ResponseEntity<List<CulturalObjectViewMetricDto>> getMostViewedObjects(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            log.info("Getting most viewed objects for time range: {}, limit: {}", timeRange, limit);
            
            List<CulturalObjectViewMetricDto> objects = dashboardAnalyticsService.getMostViewedObjects(timeRange, limit);
            
            return ResponseEntity.ok(objects);
            
        } catch (Exception e) {
            log.error("Error getting most viewed objects: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get campus zone activity
     */
    @GetMapping("/zone-activity")
    public ResponseEntity<List<CampusZoneActivityDto>> getCampusZoneActivity(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            log.info("Getting campus zone activity for time range: {}, limit: {}", timeRange, limit);
            
            List<CampusZoneActivityDto> activities = dashboardAnalyticsService.getCampusZoneActivity(timeRange, limit);
            
            return ResponseEntity.ok(activities);
            
        } catch (Exception e) {
            log.error("Error getting campus zone activity: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get theme interactions
     */
    @GetMapping("/theme-interactions")
    public ResponseEntity<List<ThemeInteractionDto>> getThemeInteractions(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            log.info("Getting theme interactions for time range: {}, limit: {}", timeRange, limit);
            
            List<ThemeInteractionDto> interactions = dashboardAnalyticsService.getThemeInteractions(timeRange, limit);
            
            return ResponseEntity.ok(interactions);
            
        } catch (Exception e) {
            log.error("Error getting theme interactions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get overall statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<OverallStatsDto> getOverallStats(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange) {
        
        try {
            log.info("Getting overall stats for time range: {}", timeRange);
            
            OverallStatsDto stats = dashboardAnalyticsService.getOverallStats(timeRange);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting overall stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get user activity analytics
     */
    @GetMapping("/user-activity")
    public ResponseEntity<List<UserActivityDto>> getUserActivity(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        
        try {
            log.info("Getting user activity for time range: {}, limit: {}", timeRange, limit);
            
            List<UserActivityDto> activities = dashboardAnalyticsService.getUserActivity(timeRange, limit);
            
            return ResponseEntity.ok(activities);
            
        } catch (Exception e) {
            log.error("Error getting user activity: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get analytics for custom time range
     */
    @GetMapping("/time-range-analytics")
    public ResponseEntity<TimeRangeAnalyticsDto> getTimeRangeAnalytics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            log.info("Getting time range analytics from {} to {}", startDate, endDate);
            
            TimeRangeAnalyticsDto analytics = dashboardAnalyticsService.getTimeRangeAnalytics(startDate, endDate);
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Error getting time range analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get content optimization recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<String>> getContentRecommendations(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange) {
        
        try {
            log.info("Generating content recommendations for time range: {}", timeRange);
            
            DashboardMetricsDto metrics = dashboardAnalyticsService.getDashboardMetrics(timeRange);
            List<String> recommendations = analyticsCalculationService.generateContentRecommendations(metrics);
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            log.error("Error generating content recommendations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get analytics insights
     */
    @GetMapping("/insights")
    public ResponseEntity<List<String>> getAnalyticsInsights(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange) {
        
        try {
            log.info("Generating analytics insights for time range: {}", timeRange);
            
            DashboardMetricsDto metrics = dashboardAnalyticsService.getDashboardMetrics(timeRange);
            List<String> insights = analyticsCalculationService.generateInsights(metrics);
            
            return ResponseEntity.ok(insights);
            
        } catch (Exception e) {
            log.error("Error generating analytics insights: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get trend predictions
     */
    @GetMapping("/predictions")
    public ResponseEntity<Map<String, Double>> getTrendPredictions(
            @RequestParam(value = "timeRange", defaultValue = "weekly") String timeRange) {
        
        try {
            log.info("Generating trend predictions for time range: {}", timeRange);
            
            DashboardMetricsDto metrics = dashboardAnalyticsService.getDashboardMetrics(timeRange);
            Map<String, Double> predictions = analyticsCalculationService.generateTrendPredictions(metrics);
            
            return ResponseEntity.ok(predictions);
            
        } catch (Exception e) {
            log.error("Error generating trend predictions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============ EVENT TRACKING ENDPOINTS ============
    
    /**
     * Track cultural object interaction
     */
    @PostMapping("/track/interaction")
    public ResponseEntity<?> trackInteraction(@RequestBody CulturalObjectInteractionRequest request) {
        try {
            log.info("Tracking interaction: {} for object: {}", request.getInteractionType(), request.getObjectId());
            
            CulturalObjectInteraction interaction = new CulturalObjectInteraction();
            interaction.setInteractionId(UUID.randomUUID().toString());
            interaction.setObjectId(request.getObjectId());
            interaction.setObjectName(request.getObjectName());
            interaction.setUserId(request.getUserId());
            interaction.setInteractionType(request.getInteractionType());
            interaction.setTimestamp(LocalDateTime.now());
            interaction.setDuration(request.getDuration());
            interaction.setZoneId(request.getZoneId());
            interaction.setCulturalType(request.getCulturalType());
            interaction.setTheme(request.getTheme());
            interaction.setCulture(request.getCulture());
            interaction.setPeriod(request.getPeriod());
            interaction.setRegion(request.getRegion());
            interaction.setAdditionalData(request.getAdditionalData());
            
            dashboardAnalyticsService.trackInteractionEvent(interaction);
            
            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Interaction tracked successfully\"}");
            
        } catch (Exception e) {
            log.error("Error tracking interaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"status\":\"error\",\"message\":\"Failed to track interaction\"}");
        }
    }
    
    /**
     * Track user session
     */
    @PostMapping("/track/session")
    public ResponseEntity<?> trackSession(@RequestBody UserSessionRequest request) {
        try {
            log.info("Tracking session: {} for user: {}", request.getSessionId(), request.getUserId());
            
            UserSession session = new UserSession();
            session.setSessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
            session.setUserId(request.getUserId());
            session.setStartTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now());
            session.setEndTime(request.getEndTime());
            session.setDuration(request.getDuration());
            session.setZoneId(request.getZoneId());
            session.setZoneName(request.getZoneName());
            session.setLatitude(request.getLatitude());
            session.setLongitude(request.getLongitude());
            session.setDeviceType(request.getDeviceType());
            session.setSessionType(request.getSessionType());
            
            dashboardAnalyticsService.trackUserSession(session);
            
            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Session tracked successfully\"}");
            
        } catch (Exception e) {
            log.error("Error tracking session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"status\":\"error\",\"message\":\"Failed to track session\"}");
        }
    }
    
    /**
     * Track social interaction
     */
    @PostMapping("/track/social")
    public ResponseEntity<?> trackSocialInteraction(@RequestBody SocialInteractionRequest request) {
        try {
            log.info("Tracking social interaction: {} by user: {}", request.getInteractionType(), request.getUserId());
            
            SocialInteraction interaction = new SocialInteraction();
            interaction.setInteractionId(UUID.randomUUID().toString());
            interaction.setUserId(request.getUserId());
            interaction.setTargetId(request.getTargetId());
            interaction.setTargetType(request.getTargetType());
            interaction.setInteractionType(request.getInteractionType());
            interaction.setContent(request.getContent());
            interaction.setTimestamp(LocalDateTime.now());
            interaction.setZoneId(request.getZoneId());
            interaction.setCulturalTheme(request.getCulturalTheme());
            interaction.setMetadata(request.getMetadata());
            
            dashboardAnalyticsService.trackSocialInteraction(interaction);
            
            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Social interaction tracked successfully\"}");
            
        } catch (Exception e) {
            log.error("Error tracking social interaction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"status\":\"error\",\"message\":\"Failed to track social interaction\"}");
        }
    }
    
    /**
     * Track general analytics event
     */
    @PostMapping("/track/event")
    public ResponseEntity<?> trackEvent(@RequestBody AnalyticsEventRequest request) {
        try {
            log.info("Tracking analytics event: {} for user: {}", request.getEventType(), request.getUserId());
            
            AnalyticsEvent event = new AnalyticsEvent();
            event.setEventId(UUID.randomUUID().toString());
            event.setUserId(request.getUserId());
            event.setSessionId(request.getSessionId());
            event.setEventType(request.getEventType());
            event.setObjectId(request.getObjectId());
            event.setZoneId(request.getZoneId());
            event.setTimestamp(LocalDateTime.now());
            event.setDuration(request.getDuration());
            event.setEventData(request.getEventData());
            event.setDeviceInfo(request.getDeviceInfo());
            event.setLatitude(request.getLatitude());
            event.setLongitude(request.getLongitude());
            
            dashboardAnalyticsService.trackAnalyticsEvent(event);
            
            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Event tracked successfully\"}");
            
        } catch (Exception e) {
            log.error("Error tracking event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"status\":\"error\",\"message\":\"Failed to track event\"}");
        }
    }
    
    // ============ REQUEST DTOs ============
    
    public static class CulturalObjectInteractionRequest {
        private String objectId;
        private String objectName;
        private String userId;
        private String interactionType;
        private Double duration;
        private String zoneId;
        private String culturalType;
        private String theme;
        private String culture;
        private String period;
        private String region;
        private String additionalData;
        
        // Getters and setters
        public String getObjectId() { return objectId; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        public String getObjectName() { return objectName; }
        public void setObjectName(String objectName) { this.objectName = objectName; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getInteractionType() { return interactionType; }
        public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }
        public String getZoneId() { return zoneId; }
        public void setZoneId(String zoneId) { this.zoneId = zoneId; }
        public String getCulturalType() { return culturalType; }
        public void setCulturalType(String culturalType) { this.culturalType = culturalType; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        public String getCulture() { return culture; }
        public void setCulture(String culture) { this.culture = culture; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getAdditionalData() { return additionalData; }
        public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }
    }
    
    public static class UserSessionRequest {
        private String sessionId;
        private String userId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Double duration;
        private String zoneId;
        private String zoneName;
        private Double latitude;
        private Double longitude;
        private String deviceType;
        private String sessionType;
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }
        public String getZoneId() { return zoneId; }
        public void setZoneId(String zoneId) { this.zoneId = zoneId; }
        public String getZoneName() { return zoneName; }
        public void setZoneName(String zoneName) { this.zoneName = zoneName; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    }
    
    public static class SocialInteractionRequest {
        private String userId;
        private String targetId;
        private String targetType;
        private String interactionType;
        private String content;
        private String zoneId;
        private String culturalTheme;
        private String metadata;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public String getInteractionType() { return interactionType; }
        public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getZoneId() { return zoneId; }
        public void setZoneId(String zoneId) { this.zoneId = zoneId; }
        public String getCulturalTheme() { return culturalTheme; }
        public void setCulturalTheme(String culturalTheme) { this.culturalTheme = culturalTheme; }
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
    }
    
    public static class AnalyticsEventRequest {
        private String userId;
        private String sessionId;
        private String eventType;
        private String objectId;
        private String zoneId;
        private Double duration;
        private String eventData;
        private String deviceInfo;
        private Double latitude;
        private Double longitude;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public String getObjectId() { return objectId; }
        public void setObjectId(String objectId) { this.objectId = objectId; }
        public String getZoneId() { return zoneId; }
        public void setZoneId(String zoneId) { this.zoneId = zoneId; }
        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }
        public String getEventData() { return eventData; }
        public void setEventData(String eventData) { this.eventData = eventData; }
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
}
