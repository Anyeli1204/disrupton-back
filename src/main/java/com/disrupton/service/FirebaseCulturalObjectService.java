package com.disrupton.service;

import com.disrupton.dto.CulturalObjectDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FirebaseCulturalObjectService {

    private final Firestore db = FirestoreClient.getFirestore();
    private static final String COLLECTION_NAME = "cultural_objects";

    /**
     * Guarda un nuevo objeto cultural en Firestore
     */
    public CulturalObjectDto saveCulturalObject(CulturalObjectDto culturalObject) throws ExecutionException, InterruptedException {
        log.info("💾 Guardando objeto cultural: {}", culturalObject.getTitle());
        
        // Establecer timestamp de creación si no existe
        if (culturalObject.getCreatedAt() == null) {
            culturalObject.setCreatedAt(com.google.cloud.Timestamp.now());
        }
        
        // Establecer estado por defecto si no existe
        if (culturalObject.getStatus() == null) {
            culturalObject.setStatus("pending");
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String objectId = docRef.getId();
        
        ApiFuture<WriteResult> future = docRef.set(culturalObject);
        
        WriteResult result = future.get();
        log.info("✅ Objeto cultural guardado exitosamente con ID: {}. Timestamp: {}", objectId, result.getUpdateTime());
        
        return culturalObject;
    }

    /**
     * Obtiene un objeto cultural por ID
     */
    public CulturalObjectDto getCulturalObjectById(String objectId) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando objeto cultural con ID: {}", objectId);
        
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(objectId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            CulturalObjectDto culturalObject = document.toObject(CulturalObjectDto.class);
            log.info("✅ Objeto cultural encontrado: {}", culturalObject.getTitle());
            return culturalObject;
        } else {
            log.warn("⚠️ Objeto cultural no encontrado con ID: {}", objectId);
            return null;
        }
    }

    /**
     * Obtiene todos los objetos culturales aprobados
     */
    public List<CulturalObjectDto> getApprovedCulturalObjects() throws ExecutionException, InterruptedException {
        log.info("📋 Obteniendo objetos culturales aprobados");
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "approved")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<CulturalObjectDto> culturalObjects = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CulturalObjectDto culturalObject = document.toObject(CulturalObjectDto.class);
            culturalObjects.add(culturalObject);
        }
        
        log.info("✅ {} objetos culturales aprobados encontrados", culturalObjects.size());
        return culturalObjects;
    }

    /**
     * Obtiene objetos culturales pendientes de revisión
     */
    public List<CulturalObjectDto> getPendingCulturalObjects() throws ExecutionException, InterruptedException {
        log.info("⏳ Obteniendo objetos culturales pendientes de revisión");
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<CulturalObjectDto> culturalObjects = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CulturalObjectDto culturalObject = document.toObject(CulturalObjectDto.class);
            culturalObjects.add(culturalObject);
        }
        
        log.info("✅ {} objetos culturales pendientes encontrados", culturalObjects.size());
        return culturalObjects;
    }

    /**
     * Obtiene objetos culturales por creador
     */
    public List<CulturalObjectDto> getCulturalObjectsByCreator(String createdBy) throws ExecutionException, InterruptedException {
        log.info("👤 Buscando objetos culturales del creador: {}", createdBy);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("createdBy", createdBy)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<CulturalObjectDto> culturalObjects = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CulturalObjectDto culturalObject = document.toObject(CulturalObjectDto.class);
            culturalObjects.add(culturalObject);
        }
        
        log.info("✅ {} objetos culturales encontrados del creador {}", culturalObjects.size(), createdBy);
        return culturalObjects;
    }

    /**
     * Actualiza el estado de un objeto cultural (para moderación)
     */
    public CulturalObjectDto updateCulturalObjectStatus(String objectId, String status) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando estado del objeto cultural {} a: {}", objectId, status);
        
        CulturalObjectDto culturalObject = getCulturalObjectById(objectId);
        if (culturalObject == null) {
            log.error("❌ Objeto cultural no encontrado: {}", objectId);
            return null;
        }
        
        culturalObject.setStatus(status);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(objectId)
                .set(culturalObject);
        
        WriteResult result = future.get();
        log.info("✅ Estado del objeto cultural actualizado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return culturalObject;
    }

    /**
     * Actualiza la URL del modelo 3D
     */
    public CulturalObjectDto updateModelUrl(String objectId, String modelUrl) throws ExecutionException, InterruptedException {
        log.info("🏗️ Actualizando URL del modelo 3D para: {}", objectId);
        
        CulturalObjectDto culturalObject = getCulturalObjectById(objectId);
        if (culturalObject == null) {
            log.error("❌ Objeto cultural no encontrado: {}", objectId);
            return null;
        }
        
        culturalObject.setModelUrl(modelUrl);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(objectId)
                .set(culturalObject);
        
        WriteResult result = future.get();
        log.info("✅ URL del modelo 3D actualizada exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return culturalObject;
    }

    /**
     * Elimina un objeto cultural
     */
    public boolean deleteCulturalObject(String objectId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando objeto cultural: {}", objectId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(objectId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Objeto cultural eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }
} 