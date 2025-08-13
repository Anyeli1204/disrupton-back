package com.disrupton.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Servicio para manejar analytics de Firebase
 * Guarda eventos en Firestore y genera métricas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAnalyticsService {
    
    private final Firestore firestore;
    
    // Nombres de las colecciones
    private static final String ANALYTICS_EVENTS_COLLECTION = "analytics_events";
    private static final String USER_SESSIONS_COLLECTION = "user_sessions";
    private static final String INTERACTION_EVENTS_COLLECTION = "interaction_events";
    private static final String SOCIAL_INTERACTIONS_COLLECTION = "social_interactions";
    
    /**
     * Guarda un evento de analytics genérico
     */
    public void logEvent(String eventName, Object eventData) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", UUID.randomUUID().toString());
            event.put("eventName", eventName);
            event.put("eventData", eventData);
            event.put("timestamp", Timestamp.now());
            event.put("createdAt", LocalDateTime.now());
            
            firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .document(event.get("eventId").toString())
                    .set(event)
                    .get();
            
            log.info("Analytics event logged: {} - {}", eventName, eventData);
        } catch (Exception e) {
            log.error("Error logging analytics event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene métricas específicas de analytics
     */
    public Object getMetrics(String metricName) {
        try {
            switch (metricName.toLowerCase()) {
                case "total_events":
                    return getTotalEvents();
                case "events_today":
                    return getEventsToday();
                case "events_this_week":
                    return getEventsThisWeek();
                case "events_by_type":
                    return getEventsByType();
                case "user_engagement":
                    return getUserEngagementMetrics();
                default:
                    log.warn("Unknown metric requested: {}", metricName);
                    return null;
            }
        } catch (Exception e) {
            log.error("Error getting metric {}: {}", metricName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Guarda evento de interacción de usuario
     */
    public void saveInteractionEvent(Object interaction) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("interactionId", UUID.randomUUID().toString());
            event.put("interactionData", interaction);
            event.put("timestamp", Timestamp.now());
            event.put("createdAt", LocalDateTime.now());
            event.put("eventType", "USER_INTERACTION");
            
            firestore.collection(INTERACTION_EVENTS_COLLECTION)
                    .document(event.get("interactionId").toString())
                    .set(event)
                    .get();
            
            log.info("Interaction event saved: {}", event.get("interactionId"));
        } catch (Exception e) {
            log.error("Error saving interaction event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Guarda sesión de usuario
     */
    public void saveUserSession(Object session) {
        try {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("sessionId", UUID.randomUUID().toString());
            sessionData.put("sessionData", session);
            sessionData.put("startTime", Timestamp.now());
            sessionData.put("createdAt", LocalDateTime.now());
            sessionData.put("eventType", "USER_SESSION");
            
            firestore.collection(USER_SESSIONS_COLLECTION)
                    .document(sessionData.get("sessionId").toString())
                    .set(sessionData)
                    .get();
            
            log.info("User session saved: {}", sessionData.get("sessionId"));
        } catch (Exception e) {
            log.error("Error saving user session: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Actualiza sesión de usuario con tiempo de fin
     */
    public void updateUserSession(String sessionId, Object sessionData) {
        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("sessionData", sessionData);
            updateData.put("endTime", Timestamp.now());
            updateData.put("updatedAt", LocalDateTime.now());
            
            firestore.collection(USER_SESSIONS_COLLECTION)
                    .document(sessionId)
                    .update(updateData)
                    .get();
            
            log.info("User session updated: {}", sessionId);
        } catch (Exception e) {
            log.error("Error updating user session: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Guarda interacción social (comentarios, likes, shares)
     */
    public void saveSocialInteraction(Object interaction) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("socialInteractionId", UUID.randomUUID().toString());
            event.put("interactionData", interaction);
            event.put("timestamp", Timestamp.now());
            event.put("createdAt", LocalDateTime.now());
            event.put("eventType", "SOCIAL_INTERACTION");
            
            firestore.collection(SOCIAL_INTERACTIONS_COLLECTION)
                    .document(event.get("socialInteractionId").toString())
                    .set(event)
                    .get();
            
            log.info("Social interaction saved: {}", event.get("socialInteractionId"));
        } catch (Exception e) {
            log.error("Error saving social interaction: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Guarda evento de analytics específico
     */
    public void saveAnalyticsEvent(Object event) {
        try {
            Map<String, Object> analyticsEvent = new HashMap<>();
            analyticsEvent.put("analyticsEventId", UUID.randomUUID().toString());
            analyticsEvent.put("eventData", event);
            analyticsEvent.put("timestamp", Timestamp.now());
            analyticsEvent.put("createdAt", LocalDateTime.now());
            analyticsEvent.put("eventType", "ANALYTICS_EVENT");
            
            firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .document(analyticsEvent.get("analyticsEventId").toString())
                    .set(analyticsEvent)
                    .get();
            
            log.info("Analytics event saved: {}", analyticsEvent.get("analyticsEventId"));
        } catch (Exception e) {
            log.error("Error saving analytics event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Guarda evento de vista de objeto cultural
     */
    public void saveCulturalObjectView(String objectId, String userId, Double latitude, Double longitude) {
        try {
            Map<String, Object> viewEvent = new HashMap<>();
            viewEvent.put("viewId", UUID.randomUUID().toString());
            viewEvent.put("objectId", objectId);
            viewEvent.put("userId", userId);
            viewEvent.put("latitude", latitude);
            viewEvent.put("longitude", longitude);
            viewEvent.put("timestamp", Timestamp.now());
            viewEvent.put("createdAt", LocalDateTime.now());
            viewEvent.put("eventType", "CULTURAL_OBJECT_VIEW");
            
            firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .document(viewEvent.get("viewId").toString())
                    .set(viewEvent)
                    .get();
            
            log.info("Cultural object view saved: {} for object: {}", viewEvent.get("viewId"), objectId);
        } catch (Exception e) {
            log.error("Error saving cultural object view: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Guarda evento de sesión AR
     */
    public void saveARSession(String userId, String zoneId, Double latitude, Double longitude, Long duration) {
        try {
            Map<String, Object> arSession = new HashMap<>();
            arSession.put("arSessionId", UUID.randomUUID().toString());
            arSession.put("userId", userId);
            arSession.put("zoneId", zoneId);
            arSession.put("latitude", latitude);
            arSession.put("longitude", longitude);
            arSession.put("duration", duration);
            arSession.put("startTime", Timestamp.now());
            arSession.put("createdAt", LocalDateTime.now());
            arSession.put("eventType", "AR_SESSION");
            
            firestore.collection(USER_SESSIONS_COLLECTION)
                    .document(arSession.get("arSessionId").toString())
                    .set(arSession)
                    .get();
            
            log.info("AR session saved: {} for user: {} in zone: {}", arSession.get("arSessionId"), userId, zoneId);
        } catch (Exception e) {
            log.error("Error saving AR session: {}", e.getMessage(), e);
        }
    }
    
    // Métodos privados para obtener métricas
    
    private Long getTotalEvents() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(ANALYTICS_EVENTS_COLLECTION).get().get();
        return (long) snapshot.size();
    }
    
    private Long getEventsToday() throws ExecutionException, InterruptedException {
        Timestamp startOfDay = Timestamp.of(java.util.Date.from(
            LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        ));
        
        QuerySnapshot snapshot = firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .get();
        
        return (long) snapshot.size();
    }
    
    private Long getEventsThisWeek() throws ExecutionException, InterruptedException {
        Timestamp startOfWeek = Timestamp.of(java.util.Date.from(
            LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant()
        ));
        
        QuerySnapshot snapshot = firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                .whereGreaterThanOrEqualTo("timestamp", startOfWeek)
                .get()
                .get();
        
        return (long) snapshot.size();
    }
    
    private Map<String, Long> getEventsByType() throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(ANALYTICS_EVENTS_COLLECTION).get().get();
        
        Map<String, Long> eventsByType = new HashMap<>();
        
        for (QueryDocumentSnapshot document : snapshot) {
            String eventType = document.getString("eventType");
            if (eventType != null) {
                eventsByType.put(eventType, eventsByType.getOrDefault(eventType, 0L) + 1);
            }
        }
        
        return eventsByType;
    }
    
    private Map<String, Object> getUserEngagementMetrics() throws ExecutionException, InterruptedException {
        Map<String, Object> metrics = new HashMap<>();
        
        // Total de sesiones
        QuerySnapshot sessionsSnapshot = firestore.collection(USER_SESSIONS_COLLECTION).get().get();
        metrics.put("totalSessions", sessionsSnapshot.size());
        
        // Sesiones activas hoy
        Timestamp startOfDay = Timestamp.of(java.util.Date.from(
            LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        ));
        
        QuerySnapshot todaySessionsSnapshot = firestore.collection(USER_SESSIONS_COLLECTION)
                .whereGreaterThanOrEqualTo("startTime", startOfDay)
                .get()
                .get();
        
        metrics.put("activeSessionsToday", todaySessionsSnapshot.size());
        
        // Total de interacciones sociales
        QuerySnapshot socialSnapshot = firestore.collection(SOCIAL_INTERACTIONS_COLLECTION).get().get();
        metrics.put("totalSocialInteractions", socialSnapshot.size());
        
        // Interacciones sociales hoy
        QuerySnapshot todaySocialSnapshot = firestore.collection(SOCIAL_INTERACTIONS_COLLECTION)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .get();
        
        metrics.put("socialInteractionsToday", todaySocialSnapshot.size());
        
        return metrics;
    }
    
    /**
     * Obtiene estadísticas de engagement por usuario
     */
    public Map<String, Object> getUserEngagementStats(String userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Sesiones del usuario
            QuerySnapshot userSessions = firestore.collection(USER_SESSIONS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            stats.put("totalSessions", userSessions.size());
            
            // Interacciones sociales del usuario
            QuerySnapshot userSocialInteractions = firestore.collection(SOCIAL_INTERACTIONS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            stats.put("totalSocialInteractions", userSocialInteractions.size());
            
            // Vistas de objetos culturales del usuario
            QuerySnapshot userViews = firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("eventType", "CULTURAL_OBJECT_VIEW")
                    .get()
                    .get();
            
            stats.put("totalObjectViews", userViews.size());
            
            return stats;
        } catch (Exception e) {
            log.error("Error getting user engagement stats: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * Obtiene estadísticas de engagement por objeto cultural
     */
    public Map<String, Object> getCulturalObjectStats(String objectId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Vistas del objeto
            QuerySnapshot objectViews = firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .whereEqualTo("objectId", objectId)
                    .whereEqualTo("eventType", "CULTURAL_OBJECT_VIEW")
                    .get()
                    .get();
            
            stats.put("totalViews", objectViews.size());
            
            // Vistas hoy
            Timestamp startOfDay = Timestamp.of(java.util.Date.from(
                LocalDateTime.now().toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
            ));
            
            QuerySnapshot todayViews = firestore.collection(ANALYTICS_EVENTS_COLLECTION)
                    .whereEqualTo("objectId", objectId)
                    .whereEqualTo("eventType", "CULTURAL_OBJECT_VIEW")
                    .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                    .get()
                    .get();
            
            stats.put("viewsToday", todayViews.size());
            
            return stats;
        } catch (Exception e) {
            log.error("Error getting cultural object stats: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
} 