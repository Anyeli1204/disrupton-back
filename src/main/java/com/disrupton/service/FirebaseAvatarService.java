package com.disrupton.service;

import com.disrupton.dto.AvatarDto;
import com.disrupton.model.Avatar;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar avatares culturales en Firebase
 */
@Service
@Slf4j
public class FirebaseAvatarService {
    
    private final Firestore db;
    private static final String COLLECTION_NAME = "avatars";
    
    public FirebaseAvatarService(Firestore db) {
        this.db = db;
    }
    
    /**
     * Guarda un nuevo avatar en Firestore
     */
    public AvatarDto saveAvatar(AvatarDto avatar) throws ExecutionException, InterruptedException {
        log.info("💾 Guardando avatar: {}", avatar.getName());
        
        // Establecer timestamp de creación si no existe
        if (avatar.getCreatedAt() == null) {
            avatar.setCreatedAt(com.google.cloud.Timestamp.now());
        }
        
        // Establecer estado por defecto
        if (avatar.getStatus() == null) {
            avatar.setStatus("ACTIVE");
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String avatarId = docRef.getId();
        avatar.setAvatarId(avatarId);
        
        ApiFuture<WriteResult> future = docRef.set(avatar);
        WriteResult result = future.get();
        
        log.info("✅ Avatar guardado exitosamente con ID: {}. Timestamp: {}", avatarId, result.getUpdateTime());
        
        return avatar;
    }
    
    /**
     * Obtiene un avatar por ID
     */
    public AvatarDto getAvatarById(String avatarId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando avatar con ID: {}", avatarId);
        
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(avatarId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            log.info("✅ Avatar encontrado: {}", avatar.getName());
            return avatar;
        } else {
            log.warn("⚠️ Avatar no encontrado con ID: {}", avatarId);
            return null;
        }
    }
    
    /**
     * Obtiene todos los avatares activos
     */
    public List<AvatarDto> getActiveAvatars() throws ExecutionException, InterruptedException {
        log.info("📋 Obteniendo todos los avatares activos");
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .whereEqualTo("isActive", true)
                .orderBy("popularity", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares activos encontrados", avatars.size());
        return avatars;
    }
    
    /**
     * Obtiene avatares por región cultural
     */
    public List<AvatarDto> getAvatarsByRegion(String culturalRegion) throws ExecutionException, InterruptedException {
        log.info("🌍 Buscando avatares de la región: {}", culturalRegion);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("culturalRegion", culturalRegion)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares encontrados en la región {}", avatars.size(), culturalRegion);
        return avatars;
    }
    
    /**
     * Obtiene avatares por tipo
     */
    public List<AvatarDto> getAvatarsByType(String type) throws ExecutionException, InterruptedException {
        log.info("🎭 Buscando avatares del tipo: {}", type);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("type", type)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("totalInteractions", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares encontrados del tipo {}", avatars.size(), type);
        return avatars;
    }
    
    /**
     * Obtiene avatares por zona del campus
     */
    public List<AvatarDto> getAvatarsByCampusZone(String campusZone) throws ExecutionException, InterruptedException {
        log.info("🏫 Buscando avatares en la zona del campus: {}", campusZone);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("campusZone", campusZone)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("popularity", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares encontrados en la zona {}", avatars.size(), campusZone);
        return avatars;
    }
    
    /**
     * Obtiene avatares por área de conocimiento
     */
    public List<AvatarDto> getAvatarsByKnowledgeArea(String knowledgeArea) throws ExecutionException, InterruptedException {
        log.info("📚 Buscando avatares con conocimiento en: {}", knowledgeArea);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereArrayContains("knowledgeAreas", knowledgeArea)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares encontrados con conocimiento en {}", avatars.size(), knowledgeArea);
        return avatars;
    }
    
    /**
     * Busca avatares por idioma
     */
    public List<AvatarDto> getAvatarsByLanguage(String language) throws ExecutionException, InterruptedException {
        log.info("🗣️ Buscando avatares que hablen: {}", language);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereArrayContains("languages", language)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("totalInteractions", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares encontrados que hablan {}", avatars.size(), language);
        return avatars;
    }
    
    /**
     * Obtiene avatares populares (más interacciones)
     */
    public List<AvatarDto> getPopularAvatars(int limit) throws ExecutionException, InterruptedException {
        log.info("⭐ Obteniendo los {} avatares más populares", limit);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("totalInteractions", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares populares obtenidos", avatars.size());
        return avatars;
    }
    
    /**
     * Obtiene avatares mejor calificados
     */
    public List<AvatarDto> getTopRatedAvatars(int limit) throws ExecutionException, InterruptedException {
        log.info("🏆 Obteniendo los {} avatares mejor calificados", limit);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .whereGreaterThan("rating", 4.0)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares mejor calificados obtenidos", avatars.size());
        return avatars;
    }
    
    /**
     * Actualiza el estado de un avatar
     */
    public AvatarDto updateAvatarStatus(String avatarId, String status) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando estado del avatar {} a: {}", avatarId, status);
        
        AvatarDto avatar = getAvatarById(avatarId);
        if (avatar == null) {
            log.error("❌ Avatar no encontrado: {}", avatarId);
            return null;
        }
        
        avatar.setStatus(status);
        avatar.setUpdatedAt(com.google.cloud.Timestamp.now());
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(avatarId)
                .set(avatar);
        
        WriteResult result = future.get();
        log.info("✅ Estado del avatar actualizado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return avatar;
    }
    
    /**
     * Incrementa las interacciones de un avatar
     */
    public void incrementAvatarInteractions(String avatarId) throws ExecutionException, InterruptedException {
        log.info("📈 Incrementando interacciones del avatar: {}", avatarId);
        
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(avatarId);
        
        ApiFuture<WriteResult> future = docRef.update(
                "totalInteractions", FieldValue.increment(1),
                "todayInteractions", FieldValue.increment(1),
                "updatedAt", com.google.cloud.Timestamp.now()
        );
        
        WriteResult result = future.get();
        log.info("✅ Interacciones incrementadas. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Actualiza la calificación de un avatar
     */
    public void updateAvatarRating(String avatarId, Double newRating) throws ExecutionException, InterruptedException {
        log.info("⭐ Actualizando calificación del avatar {} con: {}", avatarId, newRating);
        
        AvatarDto avatar = getAvatarById(avatarId);
        if (avatar == null) {
            return;
        }
        
        // Calcular nueva calificación promedio
        Double currentRating = avatar.getRating() != null ? avatar.getRating() : 0.0;
        Double newAverage = (currentRating + newRating) / 2.0;
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(avatarId)
                .update(
                        "rating", newAverage,
                        "updatedAt", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Calificación actualizada a {}. Timestamp: {}", newAverage, result.getUpdateTime());
    }
    
    /**
     * Actualiza la popularidad de un avatar
     */
    public void updateAvatarPopularity(String avatarId, int popularityScore) throws ExecutionException, InterruptedException {
        log.info("📊 Actualizando popularidad del avatar {} a: {}", avatarId, popularityScore);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(avatarId)
                .update(
                        "popularity", popularityScore,
                        "updatedAt", com.google.cloud.Timestamp.now()
                );
        
        WriteResult result = future.get();
        log.info("✅ Popularidad actualizada. Timestamp: {}", result.getUpdateTime());
    }
    
    /**
     * Elimina un avatar
     */
    public boolean deleteAvatar(String avatarId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando avatar: {}", avatarId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(avatarId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Avatar eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }
    
    /**
     * Busca avatares por texto (nombre, descripción)
     */
    public List<AvatarDto> searchAvatars(String searchText) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando avatares con texto: {}", searchText);
        
        // Búsqueda simple por nombre
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("name", searchText)
                .whereLessThanOrEqualTo("name", searchText + "\uf8ff")
                .whereEqualTo("status", "ACTIVE")
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<AvatarDto> avatars = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            AvatarDto avatar = document.toObject(AvatarDto.class);
            avatars.add(avatar);
        }
        
        log.info("✅ {} avatares encontrados con la búsqueda", avatars.size());
        return avatars;
    }
}
