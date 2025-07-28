package com.disrupton.service;

import com.disrupton.dto.AvatarKnowledgeDto;
import com.disrupton.model.AvatarKnowledge;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Servicio para gestionar el conocimiento de avatares en Firebase
 */
@Service
@Slf4j
public class FirebaseAvatarKnowledgeService {
    
    private final Firestore db;
    private static final String COLLECTION_NAME = "avatar_knowledge";
    
    public FirebaseAvatarKnowledgeService(Firestore db) {
        this.db = db;
    }
    
    /**
     * Guarda nuevo conocimiento para un avatar
     */
    public AvatarKnowledgeDto saveKnowledge(AvatarKnowledgeDto knowledge) throws ExecutionException, InterruptedException {
        log.info("📚 Guardando conocimiento: {} para avatar {}", 
                knowledge.getTitle(), knowledge.getAvatarId());
        
        // Establecer timestamps
        if (knowledge.getCreatedAt() == null) {
            knowledge.setCreatedAt(com.google.cloud.Timestamp.now());
        }
        
        // Establecer estado por defecto
        if (knowledge.getStatus() == null) {
            knowledge.setStatus("DRAFT");
        }
        
        if (knowledge.getVerificationStatus() == null) {
            knowledge.setVerificationStatus("PENDING");
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String knowledgeId = docRef.getId();
        knowledge.setKnowledgeId(knowledgeId);
        
        ApiFuture<WriteResult> future = docRef.set(knowledge);
        WriteResult result = future.get();
        
        log.info("✅ Conocimiento guardado exitosamente con ID: {}. Timestamp: {}", 
                knowledgeId, result.getUpdateTime());
        
        return knowledge;
    }
    
    /**
     * Obtiene conocimiento por ID
     */
    public AvatarKnowledgeDto getKnowledgeById(String knowledgeId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando conocimiento con ID: {}", knowledgeId);
        
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(knowledgeId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            // Incrementar contador de acceso
            incrementAccess(knowledgeId);
            log.info("✅ Conocimiento encontrado: {}", knowledge.getTitle());
            return knowledge;
        } else {
            log.warn("⚠️ Conocimiento no encontrado con ID: {}", knowledgeId);
            return null;
        }
    }
    
    /**
     * Obtiene todo el conocimiento de un avatar
     */
    public List<AvatarKnowledgeDto> getKnowledgeByAvatarId(String avatarId) throws ExecutionException, InterruptedException {
        log.info("🤖 Obteniendo conocimiento del avatar: {}", avatarId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("avatarId", avatarId)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("relevanceScore", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos de conocimiento encontrados para el avatar {}", knowledgeList.size(), avatarId);
        return knowledgeList;
    }
    
    /**
     * Obtiene conocimiento por categoría
     */
    public List<AvatarKnowledgeDto> getKnowledgeByCategory(String category) throws ExecutionException, InterruptedException {
        log.info("📋 Obteniendo conocimiento de la categoría: {}", category);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("category", category)
                .whereEqualTo("status", "ACTIVE")
                .whereEqualTo("verificationStatus", "VERIFIED")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos encontrados en la categoría {}", knowledgeList.size(), category);
        return knowledgeList;
    }
    
    /**
     * Obtiene conocimiento por región cultural
     */
    public List<AvatarKnowledgeDto> getKnowledgeByCulturalRegion(String culturalRegion) throws ExecutionException, InterruptedException {
        log.info("🌍 Obteniendo conocimiento de la región: {}", culturalRegion);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("culturalRegion", culturalRegion)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("timesAccessed", Query.Direction.DESCENDING)
                .limit(100)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos encontrados en la región {}", knowledgeList.size(), culturalRegion);
        return knowledgeList;
    }
    
    /**
     * Obtiene conocimiento por nivel de dificultad
     */
    public List<AvatarKnowledgeDto> getKnowledgeByDifficultyLevel(String difficultyLevel) throws ExecutionException, InterruptedException {
        log.info("📊 Obteniendo conocimiento del nivel: {}", difficultyLevel);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("difficultyLevel", difficultyLevel)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos encontrados del nivel {}", knowledgeList.size(), difficultyLevel);
        return knowledgeList;
    }
    
    /**
     * Busca conocimiento por palabras clave
     */
    public List<AvatarKnowledgeDto> searchKnowledgeByKeywords(List<String> keywords) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando conocimiento por palabras clave: {}", keywords);
        
        List<AvatarKnowledgeDto> allResults = new ArrayList<>();
        
        // Buscar por cada palabra clave
        for (String keyword : keywords) {
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereArrayContains("keywords", keyword)
                    .whereEqualTo("status", "ACTIVE")
                    .limit(20)
                    .get();
            
            QuerySnapshot querySnapshot = future.get();
            
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
                // Evitar duplicados
                if (!allResults.contains(knowledge)) {
                    allResults.add(knowledge);
                }
            }
        }
        
        log.info("✅ {} elementos encontrados con las palabras clave", allResults.size());
        return allResults;
    }
    
    /**
     * Obtiene conocimiento más popular
     */
    public List<AvatarKnowledgeDto> getPopularKnowledge(int limit) throws ExecutionException, InterruptedException {
        log.info("⭐ Obteniendo los {} elementos de conocimiento más populares", limit);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .whereEqualTo("verificationStatus", "VERIFIED")
                .orderBy("timesAccessed", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos populares obtenidos", knowledgeList.size());
        return knowledgeList;
    }
    
    /**
     * Obtiene conocimiento mejor calificado
     */
    public List<AvatarKnowledgeDto> getTopRatedKnowledge(int limit) throws ExecutionException, InterruptedException {
        log.info("🏆 Obteniendo los {} elementos mejor calificados", limit);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .whereGreaterThan("averageRating", 4.0)
                .orderBy("averageRating", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos mejor calificados obtenidos", knowledgeList.size());
        return knowledgeList;
    }
    
    /**
     * Obtiene conocimiento por audiencia objetivo
     */
    public List<AvatarKnowledgeDto> getKnowledgeByTargetAudience(String targetAudience) throws ExecutionException, InterruptedException {
        log.info("👥 Obteniendo conocimiento para audiencia: {}", targetAudience);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("targetAudience", targetAudience)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("relevanceScore", Query.Direction.DESCENDING)
                .limit(50)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos encontrados para audiencia {}", knowledgeList.size(), targetAudience);
        return knowledgeList;
    }
    
    /**
     * Obtiene conocimiento pendiente de verificación
     */
    public List<AvatarKnowledgeDto> getPendingKnowledge() throws ExecutionException, InterruptedException {
        log.info("⏳ Obteniendo conocimiento pendiente de verificación");
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("verificationStatus", "PENDING")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos pendientes de verificación encontrados", knowledgeList.size());
        return knowledgeList;
    }
    
    /**
     * Actualiza el estado de verificación del conocimiento
     */
    public AvatarKnowledgeDto updateVerificationStatus(String knowledgeId, String status, String verifiedBy) throws ExecutionException, InterruptedException {
        log.info("✅ Actualizando estado de verificación del conocimiento {} a: {}", knowledgeId, status);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(knowledgeId)
                .update(
                        "verificationStatus", status,
                        "verifiedBy", verifiedBy,
                        "verificationDate", com.google.cloud.Timestamp.now(),
                        "updatedAt", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Estado de verificación actualizado. Timestamp: {}", result.getUpdateTime());
        
        return getKnowledgeById(knowledgeId);
    }
    
    /**
     * Actualiza la calificación del conocimiento
     */
    public void updateKnowledgeRating(String knowledgeId, Double newRating) throws ExecutionException, InterruptedException {
        log.info("⭐ Actualizando calificación del conocimiento {} con: {}", knowledgeId, newRating);
        
        AvatarKnowledgeDto knowledge = getKnowledgeById(knowledgeId);
        if (knowledge == null) {
            return;
        }
        
        // Calcular nueva calificación promedio
        int totalRatings = knowledge.getTotalRatings() != null ? knowledge.getTotalRatings() : 0;
        Double currentAverage = knowledge.getAverageRating() != null ? knowledge.getAverageRating() : 0.0;
        
        Double newAverage = ((currentAverage * totalRatings) + newRating) / (totalRatings + 1);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(knowledgeId)
                .update(
                        "averageRating", newAverage,
                        "totalRatings", FieldValue.increment(1),
                        "updatedAt", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Calificación actualizada a {}. Timestamp: {}", newAverage, result.getUpdateTime());
    }
    
    /**
     * Incrementa el contador de acceso
     */
    public void incrementAccess(String knowledgeId) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(knowledgeId)
                .update(
                        "timesAccessed", FieldValue.increment(1),
                        "lastAccessed", com.google.cloud.Timestamp.now()
                );
        
        future.get();
    }
    
    /**
     * Incrementa el contador de compartidos
     */
    public void incrementShares(String knowledgeId) throws ExecutionException, InterruptedException {
        log.info("📤 Incrementando compartidos del conocimiento: {}", knowledgeId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(knowledgeId)
                .update("shareCount", FieldValue.increment(1));
        
        WriteResult result = future.get();
        log.info("✅ Compartidos incrementados. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Actualiza el estado del conocimiento
     */
    public AvatarKnowledgeDto updateKnowledgeStatus(String knowledgeId, String status) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando estado del conocimiento {} a: {}", knowledgeId, status);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(knowledgeId)
                .update(
                        "status", status,
                        "updatedAt", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Estado del conocimiento actualizado. Timestamp: {}", result.getUpdateTime());
        
        return getKnowledgeById(knowledgeId);
    }
    
    /**
     * Elimina conocimiento
     */
    public boolean deleteKnowledge(String knowledgeId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando conocimiento: {}", knowledgeId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(knowledgeId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Conocimiento eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }
    
    /**
     * Busca conocimiento por texto libre
     */
    public List<AvatarKnowledgeDto> searchKnowledge(String searchText) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando conocimiento con texto: {}", searchText);
        
        // Búsqueda simple por título
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("title", searchText)
                .whereLessThanOrEqualTo("title", searchText + "\uf8ff")
                .whereEqualTo("status", "ACTIVE")
                .limit(20)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarKnowledgeDto> knowledgeList = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarKnowledgeDto knowledge = document.toObject(AvatarKnowledgeDto.class);
            knowledgeList.add(knowledge);
        }
        
        log.info("✅ {} elementos encontrados con la búsqueda", knowledgeList.size());
        return knowledgeList;
    }
}
