package com.disrupton.service;

import com.disrupton.dto.*;
import com.disrupton.model.*;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAnalyticsService {
    
    private final Firestore firestore;
    
    private static final String INTERACTIONS_COLLECTION = "cultural_interactions";
    private static final String SESSIONS_COLLECTION = "user_sessions";
    private static final String SOCIAL_INTERACTIONS_COLLECTION = "social_interactions";
    private static final String ANALYTICS_EVENTS_COLLECTION = "analytics_events";
    private static final String CAMPUS_ZONES_COLLECTION = "campus_zones";
    private static final String CULTURAL_OBJECTS_COLLECTION = "cultural_objects";
    private static final String USERS_COLLECTION = "users";
    
    /**
     * Get most viewed cultural objects
     */
    public List<CulturalObjectViewMetricDto> getMostViewedObjects(String timeRange, int limit) {
        try {
            LocalDateTime startDate = getStartDateForRange(timeRange);
            Query query = firestore.collection(INTERACTIONS_COLLECTION)
                    .whereEqualTo("interactionType", "view")
                    .whereGreaterThanOrEqualTo("timestamp", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            
            QuerySnapshot snapshot = query.get().get();
            
            // Group by objectId and calculate metrics
            Map<String, List<QueryDocumentSnapshot>> groupedByObject = snapshot.getDocuments()
                    .stream()
                    .collect(Collectors.groupingBy(doc -> doc.getString("objectId")));
            
            List<CulturalObjectViewMetricDto> metrics = new ArrayList<>();
            
            for (Map.Entry<String, List<QueryDocumentSnapshot>> entry : groupedByObject.entrySet()) {
                String objectId = entry.getKey();
                List<QueryDocumentSnapshot> interactions = entry.getValue();
                
                CulturalObjectViewMetricDto metric = calculateObjectMetrics(objectId, interactions, timeRange);
                metrics.add(metric);
            }
            
            return metrics.stream()
                    .sorted((a, b) -> Long.compare(b.getTotalViews(), a.getTotalViews()))
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting most viewed objects: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get campus zone activity
     */
    public List<CampusZoneActivityDto> getCampusZoneActivity(String timeRange, int limit) {
        try {
            LocalDateTime startDate = getStartDateForRange(timeRange);
            Query query = firestore.collection(SESSIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("startTime", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            
            QuerySnapshot snapshot = query.get().get();
            
            // Group by zoneId and calculate metrics
            Map<String, List<QueryDocumentSnapshot>> groupedByZone = snapshot.getDocuments()
                    .stream()
                    .filter(doc -> doc.getString("zoneId") != null)
                    .collect(Collectors.groupingBy(doc -> doc.getString("zoneId")));
            
            List<CampusZoneActivityDto> activities = new ArrayList<>();
            
            for (Map.Entry<String, List<QueryDocumentSnapshot>> entry : groupedByZone.entrySet()) {
                String zoneId = entry.getKey();
                List<QueryDocumentSnapshot> sessions = entry.getValue();
                
                CampusZoneActivityDto activity = calculateZoneMetrics(zoneId, sessions, timeRange);
                activities.add(activity);
            }
            
            return activities.stream()
                    .sorted((a, b) -> Long.compare(b.getTotalArSessions(), a.getTotalArSessions()))
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting campus zone activity: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get theme interactions
     */
    public List<ThemeInteractionDto> getThemeInteractions(String timeRange, int limit) {
        try {
            LocalDateTime startDate = getStartDateForRange(timeRange);
            Query query = firestore.collection(INTERACTIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("timestamp", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            
            QuerySnapshot snapshot = query.get().get();
            
            // Group by theme/culture/period and calculate metrics
            Map<String, List<QueryDocumentSnapshot>> groupedByTheme = snapshot.getDocuments()
                    .stream()
                    .filter(doc -> doc.getString("theme") != null)
                    .collect(Collectors.groupingBy(doc -> 
                            doc.getString("theme") + "|" + 
                            doc.getString("culture") + "|" + 
                            doc.getString("period")));
            
            List<ThemeInteractionDto> interactions = new ArrayList<>();
            
            for (Map.Entry<String, List<QueryDocumentSnapshot>> entry : groupedByTheme.entrySet()) {
                String[] themeParts = entry.getKey().split("\\|");
                List<QueryDocumentSnapshot> themeInteractions = entry.getValue();
                
                ThemeInteractionDto interaction = calculateThemeMetrics(themeParts, themeInteractions, timeRange);
                interactions.add(interaction);
            }
            
            return interactions.stream()
                    .sorted((a, b) -> Double.compare(b.getInteractionScore(), a.getInteractionScore()))
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting theme interactions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get overall statistics
     */
    public OverallStatsDto getOverallStats(String timeRange) {
        try {
            LocalDateTime startDate = getStartDateForRange(timeRange);
            OverallStatsDto stats = new OverallStatsDto();
            
            // Get total users
            QuerySnapshot usersSnapshot = firestore.collection(USERS_COLLECTION).get().get();
            stats.setTotalUsers((long) usersSnapshot.size());
            
            // Get active users for different periods
            stats.setActiveUsersToday(getActiveUsersCount(LocalDateTime.now().minusDays(1)));
            stats.setActiveUsersThisWeek(getActiveUsersCount(LocalDateTime.now().minusWeeks(1)));
            stats.setActiveUsersThisMonth(getActiveUsersCount(LocalDateTime.now().minusMonths(1)));
            
            // Get cultural objects count
            QuerySnapshot objectsSnapshot = firestore.collection(CULTURAL_OBJECTS_COLLECTION).get().get();
            stats.setTotalCulturalObjects((long) objectsSnapshot.size());
            
            // Get interaction statistics
            Query interactionsQuery = firestore.collection(INTERACTIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("timestamp", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            QuerySnapshot interactionsSnapshot = interactionsQuery.get().get();
            
            stats.setTotalViews(interactionsSnapshot.getDocuments().stream()
                    .filter(doc -> "view".equals(doc.getString("interactionType")))
                    .count());
            
            // Get social interactions statistics
            Query socialQuery = firestore.collection(SOCIAL_INTERACTIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("timestamp", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            QuerySnapshot socialSnapshot = socialQuery.get().get();
            
            stats.setTotalComments(socialSnapshot.getDocuments().stream()
                    .filter(doc -> "comment".equals(doc.getString("interactionType")))
                    .count());
            
            stats.setTotalReactions(socialSnapshot.getDocuments().stream()
                    .filter(doc -> "reaction".equals(doc.getString("interactionType")))
                    .count());
            
            stats.setTotalShares(socialSnapshot.getDocuments().stream()
                    .filter(doc -> "share".equals(doc.getString("interactionType")))
                    .count());
            
            stats.setTotalPhotos(socialSnapshot.getDocuments().stream()
                    .filter(doc -> "photo".equals(doc.getString("interactionType")))
                    .count());
            
            // Calculate average session duration
            Query sessionsQuery = firestore.collection(SESSIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("startTime", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            QuerySnapshot sessionsSnapshot = sessionsQuery.get().get();
            
            double avgSessionDuration = sessionsSnapshot.getDocuments().stream()
                    .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") : 0.0)
                    .average()
                    .orElse(0.0);
            stats.setAverageSessionDuration(avgSessionDuration);
            
            // Calculate average exploration time per object
            double avgExplorationTime = interactionsSnapshot.getDocuments().stream()
                    .filter(doc -> "explore".equals(doc.getString("interactionType")))
                    .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") / 60.0 : 0.0)
                    .average()
                    .orElse(0.0);
            stats.setAverageExplorationTimePerObject(avgExplorationTime);
            
            // Calculate social function usage
            stats.setSocialFunctionUsage((long) socialSnapshot.size());
            
            // Calculate user retention rate (simplified)
            stats.setUserRetentionRate(calculateRetentionRate(timeRange));
            
            return stats;
            
        } catch (Exception e) {
            log.error("Error getting overall stats: {}", e.getMessage(), e);
            return new OverallStatsDto();
        }
    }
    
    /**
     * Get user activity
     */
    public List<UserActivityDto> getUserActivity(String timeRange, int limit) {
        try {
            LocalDateTime startDate = getStartDateForRange(timeRange);
            Query query = firestore.collection(SESSIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("startTime", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(startDate)));
            
            QuerySnapshot snapshot = query.get().get();
            
            // Group by userId and calculate metrics
            Map<String, List<QueryDocumentSnapshot>> groupedByUser = snapshot.getDocuments()
                    .stream()
                    .collect(Collectors.groupingBy(doc -> doc.getString("userId")));
            
            List<UserActivityDto> activities = new ArrayList<>();
            
            for (Map.Entry<String, List<QueryDocumentSnapshot>> entry : groupedByUser.entrySet()) {
                String userId = entry.getKey();
                List<QueryDocumentSnapshot> sessions = entry.getValue();
                
                UserActivityDto activity = calculateUserMetrics(userId, sessions, timeRange);
                activities.add(activity);
            }
            
            return activities.stream()
                    .sorted((a, b) -> Long.compare(b.getTotalInteractions(), a.getTotalInteractions()))
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting user activity: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // Time range specific methods
    public List<CulturalObjectViewMetricDto> getMostViewedObjectsInRange(LocalDateTime start, LocalDateTime end, int limit) {
        // Implementation similar to getMostViewedObjects but with specific date range
        return getMostViewedObjects("custom", limit);
    }
    
    public List<CampusZoneActivityDto> getCampusZoneActivityInRange(LocalDateTime start, LocalDateTime end, int limit) {
        // Implementation similar to getCampusZoneActivity but with specific date range
        return getCampusZoneActivity("custom", limit);
    }
    
    public List<ThemeInteractionDto> getThemeInteractionsInRange(LocalDateTime start, LocalDateTime end, int limit) {
        // Implementation similar to getThemeInteractions but with specific date range
        return getThemeInteractions("custom", limit);
    }
    
    public List<UserActivityDto> getUserActivityInRange(LocalDateTime start, LocalDateTime end, int limit) {
        // Implementation similar to getUserActivity but with specific date range
        return getUserActivity("custom", limit);
    }
    
    public OverallStatsDto getOverallStatsInRange(LocalDateTime start, LocalDateTime end) {
        // Implementation similar to getOverallStats but with specific date range
        return getOverallStats("custom");
    }
    
    // Save methods
    public void saveInteractionEvent(CulturalObjectInteraction interaction) {
        try {
            firestore.collection(INTERACTIONS_COLLECTION)
                    .document(interaction.getInteractionId())
                    .set(interaction)
                    .get();
            log.info("Interaction event saved successfully: {}", interaction.getInteractionId());
        } catch (Exception e) {
            log.error("Error saving interaction event: {}", e.getMessage(), e);
        }
    }
    
    public void saveUserSession(UserSession session) {
        try {
            firestore.collection(SESSIONS_COLLECTION)
                    .document(session.getSessionId())
                    .set(session)
                    .get();
            log.info("User session saved successfully: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Error saving user session: {}", e.getMessage(), e);
        }
    }
    
    public void saveSocialInteraction(SocialInteraction interaction) {
        try {
            firestore.collection(SOCIAL_INTERACTIONS_COLLECTION)
                    .document(interaction.getInteractionId())
                    .set(interaction)
                    .get();
            log.info("Social interaction saved successfully: {}", interaction.getInteractionId());
        } catch (Exception e) {
            log.error("Error saving social interaction: {}", e.getMessage(), e);
        }
    }
    
    public void saveAnalyticsEvent(AnalyticsEvent event) {
        try {
            firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .document(event.getEventId())
                    .set(event)
                    .get();
            log.info("Analytics event saved successfully: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Error saving analytics event: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods
    private LocalDateTime getStartDateForRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange.toLowerCase()) {
            case "daily":
                return now.minusDays(1);
            case "weekly":
                return now.minusWeeks(1);
            case "monthly":
                return now.minusMonths(1);
            default:
                return now.minusDays(1);
        }
    }
    
    private Long getActiveUsersCount(LocalDateTime since) {
        try {
            Query query = firestore.collection(SESSIONS_COLLECTION)
                    .whereGreaterThanOrEqualTo("startTime", 
                            com.google.cloud.Timestamp.of(java.sql.Timestamp.valueOf(since)));
            QuerySnapshot snapshot = query.get().get();
            
            return snapshot.getDocuments().stream()
                    .map(doc -> doc.getString("userId"))
                    .distinct()
                    .count();
        } catch (Exception e) {
            log.error("Error getting active users count: {}", e.getMessage(), e);
            return 0L;
        }
    }
    
    private Double calculateRetentionRate(String timeRange) {
        // Simplified retention rate calculation
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime previousPeriod = getStartDateForRange(timeRange);
            LocalDateTime beforePrevious = previousPeriod.minus(
                    now.until(previousPeriod, java.time.temporal.ChronoUnit.DAYS),
                    java.time.temporal.ChronoUnit.DAYS
            );
            
            Long currentUsers = getActiveUsersCount(previousPeriod);
            Long previousUsers = getActiveUsersCount(beforePrevious);
            
            if (previousUsers == 0) return 0.0;
            return (currentUsers.doubleValue() / previousUsers.doubleValue()) * 100.0;
        } catch (Exception e) {
            log.error("Error calculating retention rate: {}", e.getMessage(), e);
            return 0.0;
        }
    }
    
    private CulturalObjectViewMetricDto calculateObjectMetrics(String objectId, List<QueryDocumentSnapshot> interactions, String timeRange) {
        CulturalObjectViewMetricDto metric = new CulturalObjectViewMetricDto();
        metric.setObjectId(objectId);
        
        // Get object details from first interaction
        if (!interactions.isEmpty()) {
            QueryDocumentSnapshot first = interactions.get(0);
            metric.setObjectName(first.getString("objectName"));
            metric.setCulturalType(first.getString("culturalType"));
            metric.setRegion(first.getString("region"));
        }
        
        // Calculate view metrics based on time periods
        LocalDateTime now = LocalDateTime.now();
        long dailyViews = interactions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusDays(1)))
                .count();
        long weeklyViews = interactions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusWeeks(1)))
                .count();
        long monthlyViews = interactions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusMonths(1)))
                .count();
        
        metric.setTotalViews((long) interactions.size());
        metric.setDailyViews(dailyViews);
        metric.setWeeklyViews(weeklyViews);
        metric.setMonthlyViews(monthlyViews);
        
        // Calculate average exploration time
        double avgTime = interactions.stream()
                .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") : 0.0)
                .average()
                .orElse(0.0);
        metric.setAverageExplorationTime(avgTime);
        
        // Set default values for social metrics (would need additional queries)
        metric.setTotalComments(0L);
        metric.setTotalReactions(0L);
        metric.setTotalShares(0L);
        
        return metric;
    }
    
    private CampusZoneActivityDto calculateZoneMetrics(String zoneId, List<QueryDocumentSnapshot> sessions, String timeRange) {
        CampusZoneActivityDto activity = new CampusZoneActivityDto();
        activity.setZoneId(zoneId);
        
        // Get zone details from first session
        if (!sessions.isEmpty()) {
            QueryDocumentSnapshot first = sessions.get(0);
            activity.setZoneName(first.getString("zoneName"));
            activity.setLatitude(first.getDouble("latitude"));
            activity.setLongitude(first.getDouble("longitude"));
        }
        
        // Calculate session metrics based on time periods
        LocalDateTime now = LocalDateTime.now();
        long dailySessions = sessions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusDays(1)))
                .count();
        long weeklySessions = sessions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusWeeks(1)))
                .count();
        long monthlySessions = sessions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusMonths(1)))
                .count();
        
        activity.setTotalArSessions((long) sessions.size());
        activity.setDailyArSessions(dailySessions);
        activity.setWeeklyArSessions(weeklySessions);
        activity.setMonthlyArSessions(monthlySessions);
        
        // Calculate average session duration
        double avgDuration = sessions.stream()
                .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") : 0.0)
                .average()
                .orElse(0.0);
        activity.setAverageSessionDuration(avgDuration);
        
        // Calculate unique visitors
        long uniqueVisitors = sessions.stream()
                .map(doc -> doc.getString("userId"))
                .distinct()
                .count();
        activity.setUniqueVisitors(uniqueVisitors);
        
        // Set default most active time slot
        activity.setMostActiveTimeSlot("afternoon");
        
        return activity;
    }
    
    private ThemeInteractionDto calculateThemeMetrics(String[] themeParts, List<QueryDocumentSnapshot> interactions, String timeRange) {
        ThemeInteractionDto metric = new ThemeInteractionDto();
        metric.setTheme(themeParts.length > 0 ? themeParts[0] : "Unknown");
        metric.setCulture(themeParts.length > 1 ? themeParts[1] : "Unknown");
        metric.setPeriod(themeParts.length > 2 ? themeParts[2] : "Unknown");
        
        // Calculate interaction metrics based on time periods
        LocalDateTime now = LocalDateTime.now();
        long dailyInteractions = interactions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusDays(1)))
                .count();
        long weeklyInteractions = interactions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusWeeks(1)))
                .count();
        long monthlyInteractions = interactions.stream()
                .filter(doc -> isWithinTimeRange(doc, now.minusMonths(1)))
                .count();
        
        metric.setTotalInteractions((long) interactions.size());
        metric.setDailyInteractions(dailyInteractions);
        metric.setWeeklyInteractions(weeklyInteractions);
        metric.setMonthlyInteractions(monthlyInteractions);
        
        // Calculate average engagement time
        double avgEngagement = interactions.stream()
                .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") / 60.0 : 0.0)
                .average()
                .orElse(0.0);
        metric.setAverageEngagementTime(avgEngagement);
        
        // Count different interaction types
        metric.setCommentsCount(interactions.stream()
                .filter(doc -> "comment".equals(doc.getString("interactionType")))
                .count());
        metric.setReactionsCount(interactions.stream()
                .filter(doc -> "reaction".equals(doc.getString("interactionType")))
                .count());
        metric.setSharesCount(interactions.stream()
                .filter(doc -> "share".equals(doc.getString("interactionType")))
                .count());
        metric.setPhotosCount(interactions.stream()
                .filter(doc -> "photo".equals(doc.getString("interactionType")))
                .count());
        
        // Calculate interaction score (weighted sum)
        double score = (metric.getTotalInteractions() * 1.0) +
                      (metric.getCommentsCount() * 2.0) +
                      (metric.getReactionsCount() * 1.5) +
                      (metric.getSharesCount() * 3.0) +
                      (metric.getPhotosCount() * 2.5) +
                      (avgEngagement * 0.1);
        metric.setInteractionScore(score);
        
        return metric;
    }
    
    private UserActivityDto calculateUserMetrics(String userId, List<QueryDocumentSnapshot> sessions, String timeRange) {
        UserActivityDto activity = new UserActivityDto();
        activity.setUserId(userId);
        
        // Calculate basic metrics
        activity.setTotalSessions((long) sessions.size());
        
        double totalTime = sessions.stream()
                .mapToDouble(doc -> doc.getDouble("duration") != null ? doc.getDouble("duration") : 0.0)
                .sum();
        activity.setTotalExplorationTime(totalTime);
        
        // Find last activity
        LocalDateTime lastActivity = sessions.stream()
                .map(doc -> {
                    com.google.cloud.Timestamp timestamp = doc.getTimestamp("startTime");
                    return timestamp != null ? 
                            timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() :
                            LocalDateTime.MIN;
                })
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        activity.setLastActivity(lastActivity);
        
        // Find first visit
        LocalDateTime firstVisit = sessions.stream()
                .map(doc -> {
                    com.google.cloud.Timestamp timestamp = doc.getTimestamp("startTime");
                    return timestamp != null ? 
                            timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() :
                            LocalDateTime.MAX;
                })
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        activity.setFirstVisit(firstVisit);
        
        // Set default values for other metrics
        activity.setTotalInteractions(0L);
        activity.setCommentsCount(0L);
        activity.setReactionsCount(0L);
        activity.setSharesCount(0L);
        activity.setPhotosCount(0L);
        activity.setFavoriteTheme("Unknown");
        activity.setFavoriteCulture("Unknown");
        activity.setMostVisitedZone("Unknown");
        activity.setUserType("student");
        
        return activity;
    }
    
    private boolean isWithinTimeRange(QueryDocumentSnapshot doc, LocalDateTime since) {
        com.google.cloud.Timestamp timestamp = doc.getTimestamp("timestamp");
        if (timestamp == null) {
            timestamp = doc.getTimestamp("startTime");
        }
        if (timestamp == null) return false;
        
        LocalDateTime docTime = timestamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        return docTime.isAfter(since);
    }
}
