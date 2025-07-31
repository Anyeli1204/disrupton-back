package com.disrupton.service;

import com.disrupton.dto.ReactionDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseReactionService {

    private final Firestore db;
    private static final String COLLECTION_NAME = "reactions";

    /**
     * Guarda una nueva reacción en Firestore
     */
    public ReactionDto saveReaction(ReactionDto reaction) throws ExecutionException, InterruptedException {
        log.info("❤️ Guardando reacción {} para objeto: {}", reaction.getType(), reaction.getObjectId());
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String reactionId = docRef.getId();
        
        // Asignar el ID generado a la reacción
        reaction.setId(reactionId);
        
        ApiFuture<WriteResult> future = docRef.set(reaction);
        
        WriteResult result = future.get();
        log.info("✅ Reacción guardada exitosamente con ID: {}. Timestamp: {}", reactionId, result.getUpdateTime());
        
        return reaction;
    }

    /**
     * Obtiene reacciones por objeto cultural
     */
    public List<ReactionDto> getReactionsByObjectId(String objectId) throws ExecutionException, InterruptedException {
        log.info("❤️ Obteniendo reacciones para objeto: {}", objectId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("objectId", objectId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<ReactionDto> reactions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            ReactionDto reaction = document.toObject(ReactionDto.class);
            // Asignar el ID del documento
            reaction.setId(document.getId());
            reactions.add(reaction);
        }
        
        log.info("✅ {} reacciones encontradas para el objeto {}", reactions.size(), objectId);
        return reactions;
    }

    /**
     * Obtiene reacciones por usuario
     */
    public List<ReactionDto> getReactionsByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("👤 Obteniendo reacciones del usuario: {}", userId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<ReactionDto> reactions = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            ReactionDto reaction = document.toObject(ReactionDto.class);
            // Asignar el ID del documento
            reaction.setId(document.getId());
            reactions.add(reaction);
        }
        
        log.info("✅ {} reacciones encontradas del usuario {}", reactions.size(), userId);
        return reactions;
    }

    /**
     * Verifica si un usuario ya reaccionó a un objeto
     */
    public ReactionDto getUserReaction(String userId, String objectId) throws ExecutionException, InterruptedException {
        log.info("🔍 Verificando reacción del usuario {} al objeto {}", userId, objectId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("objectId", objectId)
                .limit(1)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            ReactionDto reaction = document.toObject(ReactionDto.class);
            // Asignar el ID del documento
            reaction.setId(document.getId());
            log.info("✅ Reacción encontrada: {}", reaction.getType());
            return reaction;
        } else {
            log.info("ℹ️ No se encontró reacción del usuario al objeto");
            return null;
        }
    }

    /**
     * Elimina una reacción
     */
    public boolean deleteReaction(String reactionId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando reacción: {}", reactionId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(reactionId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Reacción eliminada exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }

    /**
     * Elimina la reacción de un usuario a un objeto específico
     */
    public boolean deleteUserReaction(String userId, String objectId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando reacción del usuario {} al objeto {}", userId, objectId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("objectId", objectId)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        boolean deleted = false;
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            ApiFuture<WriteResult> deleteFuture = document.getReference().delete();
            deleteFuture.get();
            deleted = true;
        }
        
        if (deleted) {
            log.info("✅ Reacción del usuario eliminada exitosamente");
        } else {
            log.warn("⚠️ No se encontró reacción para eliminar");
        }
        
        return deleted;
    }
} 