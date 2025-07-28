package com.disrupton.service;

import com.disrupton.dto.AvatarSessionDto;
import com.disrupton.model.AvatarSession;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Servicio para gestionar sesiones de avatares en Firebase
 */
@Service
@Slf4j
public class FirebaseAvatarSessionService {
    
    private final Firestore db;
    private static final String COLLECTION_NAME = "avatar_sessions";
    
    public FirebaseAvatarSessionService(Firestore db) {
        this.db = db;
    }
    
    /**
     * Crea una nueva sesión de avatar
     */
    public AvatarSessionDto createSession(AvatarSessionDto session) throws ExecutionException, InterruptedException {
        log.info("🎬 Creando nueva sesión para avatar {} y usuario {}", 
                session.getAvatarId(), session.getUserId());
        
        // Establecer timestamps si no existen
        if (session.getStartTime() == null) {
            session.setStartTime(com.google.cloud.Timestamp.now());
        }
        
        // Generar ID de sesión único si no existe
        if (session.getSessionId() == null) {
            session.setSessionId(generateSessionId());
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        
        ApiFuture<WriteResult> future = docRef.set(session);
        WriteResult result = future.get();
        
        log.info("✅ Sesión creada exitosamente con ID: {}. Timestamp: {}", 
                session.getSessionId(), result.getUpdateTime());
        
        return session;
    }
    
    /**
     * Obtiene una sesión por ID
     */
    public AvatarSessionDto getSessionById(String sessionId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando sesión con ID: {}", sessionId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            log.info("✅ Sesión encontrada: {}", sessionId);
            return session;
        } else {
            log.warn("⚠️ Sesión no encontrada con ID: {}", sessionId);
            return null;
        }
    }
    
    /**
     * Obtiene sesiones por avatar ID
     */
    public List<AvatarSessionDto> getSessionsByAvatarId(String avatarId) throws ExecutionException, InterruptedException {
        log.info("🤖 Obteniendo sesiones del avatar: {}", avatarId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("avatarId", avatarId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones encontradas para el avatar {}", sessions.size(), avatarId);
        return sessions;
    }
    
    /**
     * Obtiene sesiones por usuario ID
     */
    public List<AvatarSessionDto> getSessionsByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("👤 Obteniendo sesiones del usuario: {}", userId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones encontradas para el usuario {}", sessions.size(), userId);
        return sessions;
    }
    
    /**
     * Obtiene sesiones activas
     */
    public List<AvatarSessionDto> getActiveSessions() throws ExecutionException, InterruptedException {
        log.info("🔄 Obteniendo sesiones activas");
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionStatus", "ACTIVE")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones activas encontradas", sessions.size());
        return sessions;
    }
    
    /**
     * Obtiene sesiones por zona del campus
     */
    public List<AvatarSessionDto> getSessionsByCampusZone(String campusZone) throws ExecutionException, InterruptedException {
        log.info("🏫 Obteniendo sesiones en la zona: {}", campusZone);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("campusZone", campusZone)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones encontradas en la zona {}", sessions.size(), campusZone);
        return sessions;
    }
    
    /**
     * Obtiene sesiones por tipo de dispositivo
     */
    public List<AvatarSessionDto> getSessionsByDeviceType(String deviceType) throws ExecutionException, InterruptedException {
        log.info("📱 Obteniendo sesiones del tipo de dispositivo: {}", deviceType);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("deviceType", deviceType)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones encontradas para dispositivo {}", sessions.size(), deviceType);
        return sessions;
    }
    
    /**
     * Obtiene sesiones por tipo de sesión
     */
    public List<AvatarSessionDto> getSessionsByType(String sessionType) throws ExecutionException, InterruptedException {
        log.info("🎭 Obteniendo sesiones del tipo: {}", sessionType);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionType", sessionType)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones encontradas del tipo {}", sessions.size(), sessionType);
        return sessions;
    }
    
    /**
     * Obtiene sesiones completadas con alta satisfacción
     */
    public List<AvatarSessionDto> getHighSatisfactionSessions(double minRating) throws ExecutionException, InterruptedException {
        log.info("⭐ Obteniendo sesiones con satisfacción >= {}", minRating);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionCompleted", true)
                .whereGreaterThanOrEqualTo("userSatisfactionScore", minRating)
                .orderBy("userSatisfactionScore", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones de alta satisfacción encontradas", sessions.size());
        return sessions;
    }
    
    /**
     * Actualiza el estado de una sesión
     */
    public AvatarSessionDto updateSessionStatus(String sessionId, String status, String endReason) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando estado de sesión {} a: {}", sessionId, status);
        
        ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = queryFuture.get();
        
        if (querySnapshot.isEmpty()) {
            log.error("❌ Sesión no encontrada: {}", sessionId);
            return null;
        }
        
        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
        String docId = document.getId();
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(docId)
                .update(
                        "sessionStatus", status,
                        "endReason", endReason,
                        "endTime", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Estado de sesión actualizado. Timestamp: {}", result.getUpdateTime());
        
        return getSessionById(sessionId);
    }
    
    /**
     * Finaliza una sesión
     */
    public void endSession(String sessionId, String endReason) throws ExecutionException, InterruptedException {
        log.info("🔚 Finalizando sesión: {} por {}", sessionId, endReason);
        
        ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = queryFuture.get();
        
        if (querySnapshot.isEmpty()) {
            log.error("❌ Sesión no encontrada: {}", sessionId);
            return;
        }
        
        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
        String docId = document.getId();
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(docId)
                .update(
                        "sessionStatus", "COMPLETED",
                        "endTime", com.google.cloud.Timestamp.now(),
                        "endReason", endReason,
                        "sessionCompleted", true
                );
        
        WriteResult result = future.get();
        log.info("✅ Sesión finalizada. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Actualiza estadísticas de sesión
     */
    public void updateSessionStats(String sessionId, int messages, int questions, int stories) throws ExecutionException, InterruptedException {
        log.info("📊 Actualizando estadísticas de sesión: {}", sessionId);
        
        ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = queryFuture.get();
        
        if (querySnapshot.isEmpty()) {
            log.error("❌ Sesión no encontrada: {}", sessionId);
            return;
        }
        
        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
        String docId = document.getId();
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(docId)
                .update(
                        "totalMessages", FieldValue.increment(messages),
                        "userQuestions", FieldValue.increment(questions),
                        "storiesTold", FieldValue.increment(stories),
                        "lastActivityTime", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Estadísticas de sesión actualizadas. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Actualiza la satisfacción del usuario en la sesión
     */
    public void updateSessionSatisfaction(String sessionId, Double satisfaction, String feedback) throws ExecutionException, InterruptedException {
        log.info("⭐ Actualizando satisfacción de sesión {}: {}", sessionId, satisfaction);
        
        ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = queryFuture.get();
        
        if (querySnapshot.isEmpty()) {
            log.error("❌ Sesión no encontrada: {}", sessionId);
            return;
        }
        
        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
        String docId = document.getId();
        
        String rating = satisfaction >= 4.5 ? "EXCELLENT" : 
                       satisfaction >= 3.5 ? "GOOD" : 
                       satisfaction >= 2.5 ? "AVERAGE" : "POOR";
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(docId)
                .update(
                        "userSatisfactionScore", satisfaction,
                        "sessionRating", rating,
                        "userFeedback", feedback
                );
        
        WriteResult result = future.get();
        log.info("✅ Satisfacción de sesión actualizada. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Obtiene sesiones por período de tiempo
     */
    public List<AvatarSessionDto> getSessionsByPeriod(String period) throws ExecutionException, InterruptedException {
        log.info("📅 Obteniendo sesiones del período: {}", period);
        
        com.google.cloud.Timestamp startTime = calculateStartTime(period);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("startTime", startTime)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarSessionDto> sessions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarSessionDto session = document.toObject(AvatarSessionDto.class);
            sessions.add(session);
        }
        
        log.info("✅ {} sesiones encontradas en el período {}", sessions.size(), period);
        return sessions;
    }
    
    /**
     * Elimina una sesión
     */
    public boolean deleteSession(String sessionId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando sesión: {}", sessionId);
        
        ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = queryFuture.get();
        
        if (querySnapshot.isEmpty()) {
            log.error("❌ Sesión no encontrada: {}", sessionId);
            return false;
        }
        
        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
        String docId = document.getId();
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(docId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Sesión eliminada exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }
    
    /**
     * Genera un ID único para la sesión
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Calcula tiempo de inicio basado en período
     */
    private com.google.cloud.Timestamp calculateStartTime(String period) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startTime;
        
        switch (period.toLowerCase()) {
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
                startTime = now.minusDays(1);
        }
        
        return com.google.cloud.Timestamp.of(
                java.sql.Timestamp.valueOf(startTime)
        );
    }
}
