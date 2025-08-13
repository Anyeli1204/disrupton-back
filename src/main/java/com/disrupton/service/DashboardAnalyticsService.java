package com.disrupton.service;

import com.disrupton.user.dto.*;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAnalyticsService {
    
    private final FirebaseAnalyticsService firebaseAnalyticsService;
    private final AnalyticsCalculationService analyticsCalculationService;
    private final Firestore firestore;
    
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
        
        try {
            Query query = firestore.collection("cultural_objects");
            
            // Apply time filter if specified
            if (!"all".equals(timeRange)) {
                Timestamp startTime = getStartTimeForRange(timeRange);
                query = query.whereGreaterThanOrEqualTo("createdAt", startTime);
            }
            
            QuerySnapshot snapshot = query
                .orderBy("totalViews", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get();
            
            List<CulturalObjectViewMetricDto> objects = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : snapshot) {
                CulturalObjectViewMetricDto dto = new CulturalObjectViewMetricDto();
                dto.setObjectId(document.getId());
                dto.setObjectName(document.getString("name"));
                dto.setCulturalType(document.getString("type"));
                dto.setTotalViews(document.getLong("totalViews") != null ? document.getLong("totalViews") : 0L);
                dto.setTotalComments(document.getLong("totalComments") != null ? document.getLong("totalComments") : 0L);
                dto.setTotalReactions(document.getLong("totalReactions") != null ? document.getLong("totalReactions") : 0L);
                dto.setTotalShares(document.getLong("totalShares") != null ? document.getLong("totalShares") : 0L);
                dto.setAverageExplorationTime(document.getDouble("averageExplorationTime"));
                // Engagement score calculated by AnalyticsCalculationService
                
                objects.add(dto);
            }
            
            return objects;
            
        } catch (Exception e) {
            log.error("Error getting most viewed objects: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get campus zone activity
     */
    public List<CampusZoneActivityDto> getCampusZoneActivity(String timeRange, int limit) {
        log.info("Getting campus zone activity for time range: {}, limit: {}", timeRange, limit);
        
        try {
            Query query = firestore.collection("campus_zones");
            
            // Apply time filter if specified
            if (!"all".equals(timeRange)) {
                Timestamp startTime = getStartTimeForRange(timeRange);
                query = query.whereGreaterThanOrEqualTo("lastActivity", startTime);
            }
            
            QuerySnapshot snapshot = query
                .orderBy("totalArSessions", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get();
            
            List<CampusZoneActivityDto> zones = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : snapshot) {
                CampusZoneActivityDto dto = new CampusZoneActivityDto();
                dto.setZoneId(document.getId());
                dto.setZoneName(document.getString("name"));
                dto.setTotalArSessions(document.getLong("totalArSessions") != null ? document.getLong("totalArSessions") : 0L);
                dto.setUniqueVisitors(document.getLong("uniqueVisitors") != null ? document.getLong("uniqueVisitors") : 0L);
                dto.setAverageSessionDuration(document.getDouble("averageSessionDuration"));
                dto.setDailyArSessions(document.getLong("dailyArSessions") != null ? document.getLong("dailyArSessions") : 0L);
                dto.setWeeklyArSessions(document.getLong("weeklyArSessions") != null ? document.getLong("weeklyArSessions") : 0L);
                // Popularity score calculated by AnalyticsCalculationService
                
                zones.add(dto);
            }
            
            return zones;
            
        } catch (Exception e) {
            log.error("Error getting campus zone activity: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get theme interactions
     */
    public List<ThemeInteractionDto> getThemeInteractions(String timeRange, int limit) {
        log.info("Getting theme interactions for time range: {}, limit: {}", timeRange, limit);
        
        try {
            // Get interactions and group by theme
            Query query = firestore.collection("cultural_object_interactions");
            
            if (!"all".equals(timeRange)) {
                Timestamp startTime = getStartTimeForRange(timeRange);
                query = query.whereGreaterThanOrEqualTo("createdAt", startTime);
            }
            
            QuerySnapshot snapshot = query.get().get();
            
            // Group interactions by theme
            Map<String, List<QueryDocumentSnapshot>> themeGroups = new HashMap<>();
            
            for (QueryDocumentSnapshot document : snapshot) {
                String theme = document.getString("theme");
                if (theme != null) {
                    themeGroups.computeIfAbsent(theme, k -> new ArrayList<>()).add(document);
                }
            }
            
            // Convert to DTOs and calculate metrics
            List<ThemeInteractionDto> themes = themeGroups.entrySet().stream()
                .map(entry -> {
                    ThemeInteractionDto dto = new ThemeInteractionDto();
                    dto.setTheme(entry.getKey());
                    dto.setCulture(getCultureFromTheme(entry.getKey()));
                    
                    List<QueryDocumentSnapshot> interactions = entry.getValue();
                    dto.setTotalInteractions((long) interactions.size());
                    dto.setCommentsCount(countByType(interactions, "COMMENT"));
                    dto.setReactionsCount(countByType(interactions, "REACTION"));
                    dto.setSharesCount(countByType(interactions, "SHARE"));
                    dto.setPhotosCount(countByType(interactions, "PHOTO"));
                    dto.setAverageEngagementTime(calculateAverageEngagementTime(interactions));
                    dto.setInteractionScore(analyticsCalculationService.calculateThemeInteractionScore(dto));
                    
                    return dto;
                })
                .sorted((t1, t2) -> Double.compare(t2.getInteractionScore(), t1.getInteractionScore()))
                .limit(limit)
                .collect(Collectors.toList());
            
            return themes;
            
        } catch (Exception e) {
            log.error("Error getting theme interactions: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Get overall statistics
     */
    public OverallStatsDto getOverallStats(String timeRange) {
        log.info("Getting overall stats for time range: {}", timeRange);
        
        try {
            OverallStatsDto stats = new OverallStatsDto();
            
            // Get total users
            QuerySnapshot usersSnapshot = firestore.collection("users").get().get();
            stats.setTotalUsers((long) usersSnapshot.size());
            
            // Get total interactions
            QuerySnapshot interactionsSnapshot = firestore.collection("cultural_object_interactions").get().get();
            stats.setTotalComments((long) interactionsSnapshot.size());
            
            // Get total views
            QuerySnapshot objectsSnapshot = firestore.collection("cultural_objects").get().get();
            long totalViews = objectsSnapshot.getDocuments().stream()
                .mapToLong(doc -> doc.getLong("totalViews") != null ? doc.getLong("totalViews") : 0L)
                .sum();
            stats.setTotalViews(totalViews);
            
            // Calculate active users
            stats.setActiveUsersToday(calculateActiveUsersToday());
            stats.setActiveUsersThisWeek(calculateActiveUsersThisWeek());
            stats.setActiveUsersThisMonth(calculateActiveUsersThisMonth());
            
            // Calculate session metrics
            stats.setAverageSessionDuration(calculateAverageSessionDuration());
            stats.setSocialFunctionUsage(calculateSocialFunctionUsage());
            stats.setUserRetentionRate(calculateUserRetentionRate());
            
            return stats;
            
        } catch (Exception e) {
            log.error("Error getting overall stats: {}", e.getMessage(), e);
            return new OverallStatsDto();
        }
    }
    
    /**
     * Get user activity analytics
     */
    public List<UserActivityDto> getUserActivity(String timeRange, int limit) {
        log.info("Getting user activity for time range: {}, limit: {}", timeRange, limit);
        
        try {
            Query query = firestore.collection("users");
            
            if (!"all".equals(timeRange)) {
                Timestamp startTime = getStartTimeForRange(timeRange);
                query = query.whereGreaterThanOrEqualTo("lastActivity", startTime);
            }
            
            QuerySnapshot snapshot = query
                .orderBy("lastActivity", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get();
            
            List<UserActivityDto> users = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : snapshot) {
                UserActivityDto dto = new UserActivityDto();
                dto.setUserId(document.getId());
                dto.setUsername(document.getString("username"));
                dto.setEmail(document.getString("email"));
                dto.setTotalSessions(document.getLong("totalSessions") != null ? document.getLong("totalSessions") : 0L);
                dto.setTotalExplorationTime(document.getDouble("totalExplorationTime"));
                dto.setTotalInteractions(document.getLong("totalInteractions") != null ? document.getLong("totalInteractions") : 0L);
                dto.setCommentsCount(document.getLong("commentsCount") != null ? document.getLong("commentsCount") : 0L);
                dto.setReactionsCount(document.getLong("reactionsCount") != null ? document.getLong("reactionsCount") : 0L);
                dto.setSharesCount(document.getLong("sharesCount") != null ? document.getLong("sharesCount") : 0L);
                dto.setPhotosCount(document.getLong("photosCount") != null ? document.getLong("photosCount") : 0L);
                // Engagement level calculated by AnalyticsCalculationService
                
                users.add(dto);
            }
            
            return users;
            
        } catch (Exception e) {
            log.error("Error getting user activity: {}", e.getMessage(), e);
            return List.of();
        }
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
        analytics.setTopViewedObjects(getMostViewedObjects(timeRange, 5));
        analytics.setTopActiveZones(getCampusZoneActivity(timeRange, 5));
        analytics.setTopThemes(getThemeInteractions(timeRange, 5));
        analytics.setTopActiveUsers(getUserActivity(timeRange, 5));
        analytics.setStats(getOverallStats(timeRange));
        
        return analytics;
    }
    
    // Helper methods
    
    private Timestamp getStartTimeForRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        
        switch (timeRange.toLowerCase()) {
            case "today":
                startTime = now.toLocalDate().atStartOfDay();
                break;
            case "week":
                startTime = now.minusWeeks(1);
                break;
            case "month":
                startTime = now.minusMonths(1);
                break;
            case "year":
                startTime = now.minusYears(1);
                break;
            default:
                startTime = now.minusDays(7); // Default to last week
        }
        
        return Timestamp.of(java.util.Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()));
    }
    
    private String getCultureFromTheme(String theme) {
        // Simple mapping - can be expanded
        if (theme.toLowerCase().contains("inca")) return "Inca";
        if (theme.toLowerCase().contains("colonial")) return "Colonial";
        if (theme.toLowerCase().contains("republican")) return "Republican";
        if (theme.toLowerCase().contains("modern")) return "Modern";
        return "Mixed";
    }
    
    private Long countByType(List<QueryDocumentSnapshot> interactions, String type) {
        return interactions.stream()
            .filter(doc -> type.equals(doc.getString("interactionType")))
            .count();
    }
    
    private Double calculateAverageEngagementTime(List<QueryDocumentSnapshot> interactions) {
        return interactions.stream()
            .mapToDouble(doc -> doc.getDouble("engagementTime") != null ? doc.getDouble("engagementTime") : 0.0)
            .average()
            .orElse(0.0);
    }
    
    private Long calculateActiveUsersToday() {
        try {
            Timestamp startOfDay = Timestamp.of(java.util.Date.from(
                LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
            ));
            
            QuerySnapshot snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("lastActivity", startOfDay)
                .get()
                .get();
            
            return (long) snapshot.size();
        } catch (Exception e) {
            log.error("Error calculating active users today", e);
            return 0L;
        }
    }
    
    private Long calculateActiveUsersThisWeek() {
        try {
            Timestamp startOfWeek = Timestamp.of(java.util.Date.from(
                LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant()
            ));
            
            QuerySnapshot snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("lastActivity", startOfWeek)
                .get()
                .get();
            
            return (long) snapshot.size();
        } catch (Exception e) {
            log.error("Error calculating active users this week", e);
            return 0L;
        }
    }
    
    private Long calculateActiveUsersThisMonth() {
        try {
            Timestamp startOfMonth = Timestamp.of(java.util.Date.from(
                LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant()
            ));
            
            QuerySnapshot snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("lastActivity", startOfMonth)
                .get()
                .get();
            
            return (long) snapshot.size();
        } catch (Exception e) {
            log.error("Error calculating active users this month", e);
            return 0L;
        }
    }
    
    private Double calculateAverageSessionDuration() {
        try {
            QuerySnapshot snapshot = firestore.collection("user_sessions").get().get();
            
            return snapshot.getDocuments().stream()
                .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") : 0.0)
                .average()
                .orElse(0.0);
        } catch (Exception e) {
            log.error("Error calculating average session duration", e);
            return 0.0;
        }
    }
    
    private Long calculateSocialFunctionUsage() {
        try {
            QuerySnapshot snapshot = firestore.collection("cultural_object_interactions")
                .whereIn("interactionType", Arrays.asList("COMMENT", "REACTION", "SHARE", "PHOTO"))
                .get()
                .get();
            
            return (long) snapshot.size();
        } catch (Exception e) {
            log.error("Error calculating social function usage", e);
            return 0L;
        }
    }
    
    private Double calculateUserRetentionRate() {
        try {
            // Simple calculation: users active this week / total users
            Long activeUsers = calculateActiveUsersThisWeek();
            Long totalUsers = Long.valueOf(firestore.collection("users").get().get().size());
            
            if (totalUsers > 0) {
                return (activeUsers.doubleValue() / totalUsers.doubleValue()) * 100.0;
            }
            return 0.0;
        } catch (Exception e) {
            log.error("Error calculating user retention rate", e);
            return 0.0;
        }
    }
    
    /**
     * Track user interaction event
     */
    public void trackInteractionEvent(Object interaction) {
        log.info("Tracking interaction event: {}", interaction);
        firebaseAnalyticsService.saveInteractionEvent(interaction);
    }
    
    /**
     * Track user session
     */
    public void trackUserSession(Object session) {
        log.info("Tracking user session: {}", session);
        firebaseAnalyticsService.saveUserSession(session);
    }
    
    /**
     * Track social interaction
     */
    public void trackSocialInteraction(Object interaction) {
        log.info("Tracking social interaction: {}", interaction);
        firebaseAnalyticsService.saveSocialInteraction(interaction);
    }
    
    /**
     * Track analytics event
     */
    public void trackAnalyticsEvent(Object event) {
        log.info("Tracking analytics event: {}", event);
        firebaseAnalyticsService.saveAnalyticsEvent(event);
    }
}
