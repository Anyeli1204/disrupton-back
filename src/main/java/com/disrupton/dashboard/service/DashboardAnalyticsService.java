package com.disrupton.dashboard.service;

import com.disrupton.dashboard.dto.*;
import com.disrupton.shared.model.*;
import com.disrupton.user.dto.*;
import com.disrupton.cultural.dto.*;
import com.disrupton.comment.dto.*;
import com.disrupton.reaction.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsService {
    
    private final FirebaseAnalyticsService firebaseAnalyticsService;
    private final AnalyticsCalculationService analyticsCalculationService;
    
    /**
     * Get comprehensive dashboard metrics
     */
    public DashboardMetricsDto getDashboardMetrics(String timeRange) {
        log.info("Generating dashboard metrics for time range: {}", timeRange);
        
        try {
            DashboardMetricsDto metrics = new DashboardMetricsDto();
            
            // Get most viewed cultural objects
            metrics.setMostViewedObjects(getMostViewedObjects(timeRange, 10));
            
            // Get campus zone activity
            metrics.setCampusZoneActivity(getCampusZoneActivity(timeRange, 10));
            
            // Get theme interactions
            metrics.setThemeInteractions(getThemeInteractions(timeRange, 10));
            
            // Get overall statistics
            metrics.setOverallStats(getOverallStats(timeRange));
            
            metrics.setGeneratedAt(LocalDateTime.now());
            
            log.info("Dashboard metrics generated successfully");
            return metrics;
            
        } catch (Exception e) {
            log.error("Error generating dashboard metrics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate dashboard metrics", e);
        }
    }
    
    /**
     * Get most viewed cultural objects
     */
    public List<CulturalObjectViewMetricDto> getMostViewedObjects(String timeRange, int limit) {
        log.info("Getting most viewed objects for time range: {}, limit: {}", timeRange, limit);
        return firebaseAnalyticsService.getMostViewedObjects(timeRange, limit);
    }
    
    /**
     * Get campus zone activity
     */
    public List<CampusZoneActivityDto> getCampusZoneActivity(String timeRange, int limit) {
        log.info("Getting campus zone activity for time range: {}, limit: {}", timeRange, limit);
        return firebaseAnalyticsService.getCampusZoneActivity(timeRange, limit);
    }
    
    /**
     * Get theme interactions
     */
    public List<ThemeInteractionDto> getThemeInteractions(String timeRange, int limit) {
        log.info("Getting theme interactions for time range: {}, limit: {}", timeRange, limit);
        return firebaseAnalyticsService.getThemeInteractions(timeRange, limit);
    }
    
    /**
     * Get overall statistics
     */
    public OverallStatsDto getOverallStats(String timeRange) {
        log.info("Getting overall stats for time range: {}", timeRange);
        return firebaseAnalyticsService.getOverallStats(timeRange);
    }
    
    /**
     * Get user activity analytics
     */
    public List<UserActivityDto> getUserActivity(String timeRange, int limit) {
        log.info("Getting user activity for time range: {}, limit: {}", timeRange, limit);
        return firebaseAnalyticsService.getUserActivity(timeRange, limit);
    }
    
    /**
     * Get time range analytics
     */
    public TimeRangeAnalyticsDto getTimeRangeAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting time range analytics from {} to {}", startDate, endDate);
        
        TimeRangeAnalyticsDto analytics = new TimeRangeAnalyticsDto();
        analytics.setStartDate(startDate);
        analytics.setEndDate(endDate);
        
        // Determine time range type
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        String timeRange;
        if (daysBetween <= 1) {
            timeRange = "daily";
        } else if (daysBetween <= 7) {
            timeRange = "weekly";
        } else {
            timeRange = "monthly";
        }
        analytics.setTimeRange(timeRange);
        
        // Get analytics data
        analytics.setTopViewedObjects(firebaseAnalyticsService.getMostViewedObjectsInRange(startDate, endDate, 10));
        analytics.setTopActiveZones(firebaseAnalyticsService.getCampusZoneActivityInRange(startDate, endDate, 10));
        analytics.setTopThemes(firebaseAnalyticsService.getThemeInteractionsInRange(startDate, endDate, 10));
        analytics.setTopActiveUsers(firebaseAnalyticsService.getUserActivityInRange(startDate, endDate, 10));
        analytics.setStats(firebaseAnalyticsService.getOverallStatsInRange(startDate, endDate));
        
        return analytics;
    }
    
    /**
     * Track user interaction event
     */
    public void trackInteractionEvent(CulturalObjectInteraction interaction) {
        log.info("Tracking interaction event: {} for object: {}", 
                interaction.getInteractionType(), interaction.getObjectId());
        firebaseAnalyticsService.saveInteractionEvent(interaction);
    }
    
    /**
     * Track user session
     */
    public void trackUserSession(UserSession session) {
        log.info("Tracking user session: {} for user: {}", session.getSessionId(), session.getUserId());
        firebaseAnalyticsService.saveUserSession(session);
    }
    
    /**
     * Track social interaction
     */
    public void trackSocialInteraction(SocialInteraction interaction) {
        log.info("Tracking social interaction: {} by user: {}", 
                interaction.getInteractionType(), interaction.getUserId());
        firebaseAnalyticsService.saveSocialInteraction(interaction);
    }
    
    /**
     * Track analytics event
     */
    public void trackAnalyticsEvent(AnalyticsEvent event) {
        log.info("Tracking analytics event: {} for user: {}", event.getEventType(), event.getUserId());
        firebaseAnalyticsService.saveAnalyticsEvent(event);
    }
}
