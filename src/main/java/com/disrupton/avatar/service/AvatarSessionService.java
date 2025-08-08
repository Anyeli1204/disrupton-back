package com.disrupton.avatar.service;

import com.disrupton.avatar.model.AvatarConversationLog;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestionar sesiones de chat con avatares
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarSessionService {
    
    private final Firestore firestore;
    private final Map<String, String> activeSessions = new HashMap<>();
    
    /**
     * Crea una nueva sesión de chat
     */
    public String createSession(String userId, String avatarId) {
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = userId + ":" + avatarId;
        
        activeSessions.put(sessionKey, sessionId);
        
        log.info("Nueva sesión creada: {} para usuario {} con avatar {}", 
                sessionId, userId, avatarId);
        
        return sessionId;
    }
    
    /**
     * Obtiene la sesión activa para un usuario y avatar
     */
    public String getActiveSession(String userId, String avatarId) {
        String sessionKey = userId + ":" + avatarId;
        return activeSessions.get(sessionKey);
    }
    
    /**
     * Finaliza una sesión
     */
    public void endSession(String userId, String avatarId) {
        String sessionKey = userId + ":" + avatarId;
        String sessionId = activeSessions.remove(sessionKey);
        
        if (sessionId != null) {
            log.info("Sesión finalizada: {} para usuario {} con avatar {}", 
                    sessionId, userId, avatarId);
        }
    }
    
    /**
     * Obtiene estadísticas de una sesión
     */
    public Map<String, Object> getSessionStats(String sessionId) {
        try {
            List<AvatarConversationLog> logs = firestore.collection("avatar_conversation_logs")
                    .whereEqualTo("sessionId", sessionId)
                    .orderBy("createdAt")
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(AvatarConversationLog.class))
                    .toList();
            
            if (logs.isEmpty()) {
                return Map.of("sessionId", sessionId, "messageCount", 0);
            }
            
            AvatarConversationLog firstLog = logs.get(0);
            AvatarConversationLog lastLog = logs.get(logs.size() - 1);
            
            long totalResponseTime = logs.stream()
                    .mapToLong(log -> log.getResponseTimeMs() != null ? log.getResponseTimeMs() : 0)
                    .sum();
            
            double avgResponseTime = logs.size() > 0 ? (double) totalResponseTime / logs.size() : 0;
            
            return Map.of(
                "sessionId", sessionId,
                "messageCount", logs.size(),
                "startTime", firstLog.getCreatedAt(),
                "endTime", lastLog.getCreatedAt(),
                "avatarType", firstLog.getAvatarType(),
                "avgResponseTimeMs", avgResponseTime,
                "successRate", logs.stream().mapToDouble(log -> log.getIsSuccessful() ? 1.0 : 0.0).average().orElse(0.0)
            );
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de sesión {}: {}", sessionId, e.getMessage());
            return Map.of("sessionId", sessionId, "error", e.getMessage());
        }
    }
}
