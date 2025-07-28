package com.disrupton.service;

import com.disrupton.dto.AvatarConversationDto;
import com.disrupton.model.AvatarConversation;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Servicio para gestionar conversaciones de avatares en Firebase
 */
@Service
@Slf4j
public class FirebaseAvatarConversationService {
    
    private final Firestore db;
    private static final String COLLECTION_NAME = "avatar_conversations";
    
    public FirebaseAvatarConversationService(Firestore db) {
        this.db = db;
    }
    
    /**
     * Guarda una nueva conversación en Firestore
     */
    public AvatarConversationDto saveConversation(AvatarConversationDto conversation) throws ExecutionException, InterruptedException {
        log.info("💬 Guardando conversación entre avatar {} y usuario {}", 
                conversation.getAvatarId(), conversation.getUserId());
        
        // Establecer timestamp si no existe
        if (conversation.getTimestamp() == null) {
            conversation.setTimestamp(com.google.cloud.Timestamp.now());
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String conversationId = docRef.getId();
        conversation.setConversationId(conversationId);
        
        ApiFuture<WriteResult> future = docRef.set(conversation);
        WriteResult result = future.get();
        
        log.info("✅ Conversación guardada exitosamente con ID: {}. Timestamp: {}", 
                conversationId, result.getUpdateTime());
        
        return conversation;
    }
    
    /**
     * Obtiene conversaciones por avatar ID
     */
    public List<AvatarConversationDto> getConversationsByAvatarId(String avatarId) throws ExecutionException, InterruptedException {
        log.info("💬 Obteniendo conversaciones del avatar: {}", avatarId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("avatarId", avatarId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas para el avatar {}", conversations.size(), avatarId);
        return conversations;
    }
    
    /**
     * Obtiene conversaciones por usuario ID
     */
    public List<AvatarConversationDto> getConversationsByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("👤 Obteniendo conversaciones del usuario: {}", userId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas para el usuario {}", conversations.size(), userId);
        return conversations;
    }
    
    /**
     * Obtiene conversaciones por sesión
     */
    public List<AvatarConversationDto> getConversationsBySessionId(String sessionId) throws ExecutionException, InterruptedException {
        log.info("🔗 Obteniendo conversaciones de la sesión: {}", sessionId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("sessionId", sessionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas en la sesión {}", conversations.size(), sessionId);
        return conversations;
    }
    
    /**
     * Obtiene conversaciones por tema cultural
     */
    public List<AvatarConversationDto> getConversationsByCulturalTopic(String culturalTopic) throws ExecutionException, InterruptedException {
        log.info("🎭 Obteniendo conversaciones sobre el tema: {}", culturalTopic);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("culturalTopic", culturalTopic)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas sobre el tema {}", conversations.size(), culturalTopic);
        return conversations;
    }
    
    /**
     * Obtiene conversaciones por objeto cultural
     */
    public List<AvatarConversationDto> getConversationsByCulturalObject(String culturalObjectId) throws ExecutionException, InterruptedException {
        log.info("🏺 Obteniendo conversaciones sobre el objeto cultural: {}", culturalObjectId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("culturalObjectId", culturalObjectId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas sobre el objeto cultural {}", conversations.size(), culturalObjectId);
        return conversations;
    }
    
    /**
     * Obtiene conversaciones recientes
     */
    public List<AvatarConversationDto> getRecentConversations(int limit) throws ExecutionException, InterruptedException {
        log.info("⏰ Obteniendo las {} conversaciones más recientes", limit);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones recientes obtenidas", conversations.size());
        return conversations;
    }
    
    /**
     * Obtiene conversaciones por idioma
     */
    public List<AvatarConversationDto> getConversationsByLanguage(String language) throws ExecutionException, InterruptedException {
        log.info("🗣️ Obteniendo conversaciones en idioma: {}", language);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("language", language)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas en idioma {}", conversations.size(), language);
        return conversations;
    }
    
    /**
     * Obtiene conversaciones por tipo de respuesta
     */
    public List<AvatarConversationDto> getConversationsByResponseType(String responseType) throws ExecutionException, InterruptedException {
        log.info("📝 Obteniendo conversaciones del tipo: {}", responseType);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("responseType", responseType)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas del tipo {}", conversations.size(), responseType);
        return conversations;
    }
    
    /**
     * Busca conversaciones por contenido
     */
    public List<AvatarConversationDto> searchConversations(String searchText) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando conversaciones con texto: {}", searchText);
        
        // Nota: Firestore no soporta búsqueda de texto completo nativa
        // Esta es una búsqueda simple por coincidencia parcial
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("content", searchText)
                .whereLessThanOrEqualTo("content", searchText + "\uf8ff")
                .orderBy("content")
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones encontradas con la búsqueda", conversations.size());
        return conversations;
    }
    
    /**
     * Marca una conversación como leída
     */
    public void markConversationAsRead(String conversationId) throws ExecutionException, InterruptedException {
        log.info("👁️ Marcando conversación como leída: {}", conversationId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(conversationId)
                .update("isRead", true);
        
        WriteResult result = future.get();
        log.info("✅ Conversación marcada como leída. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Archiva una conversación
     */
    public void archiveConversation(String conversationId) throws ExecutionException, InterruptedException {
        log.info("📁 Archivando conversación: {}", conversationId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(conversationId)
                .update("isArchived", true);
        
        WriteResult result = future.get();
        log.info("✅ Conversación archivada. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Actualiza la satisfacción del usuario
     */
    public void updateUserSatisfaction(String conversationId, Double satisfaction, String feedback) throws ExecutionException, InterruptedException {
        log.info("⭐ Actualizando satisfacción de conversación {}: {}", conversationId, satisfaction);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(conversationId)
                .update(
                        "userSatisfaction", satisfaction,
                        "feedback", feedback,
                        "wasHelpful", satisfaction >= 4.0
                );
        
        WriteResult result = future.get();
        log.info("✅ Satisfacción actualizada. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Elimina una conversación
     */
    public boolean deleteConversation(String conversationId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando conversación: {}", conversationId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(conversationId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Conversación eliminada exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }
    
    /**
     * Obtiene estadísticas de conversaciones por avatar
     */
    public List<AvatarConversationDto> getConversationAnalytics(String avatarId, String period) throws ExecutionException, InterruptedException {
        log.info("📊 Obteniendo analíticas de conversaciones para avatar {} en período {}", avatarId, period);
        
        // Calcular fecha de inicio basada en el período
        com.google.cloud.Timestamp startTime = calculateStartTime(period);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("avatarId", avatarId)
                .whereGreaterThanOrEqualTo("timestamp", startTime)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarConversationDto> conversations = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarConversationDto conversation = document.toObject(AvatarConversationDto.class);
            conversations.add(conversation);
        }
        
        log.info("✅ {} conversaciones analizadas para el período {}", conversations.size(), period);
        return conversations;
    }
    
    /**
     * Método auxiliar para calcular tiempo de inicio
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
