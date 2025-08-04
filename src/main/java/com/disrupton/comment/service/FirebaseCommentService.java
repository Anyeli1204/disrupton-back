package com.disrupton.comment.service;

import com.disrupton.comment.dto.CommentDto;
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
public class FirebaseCommentService {

    private final Firestore db;
    private static final String COLLECTION_NAME = "comments";

    /**
     * Guarda un nuevo comentario en Firestore
     */
    public CommentDto saveComment(CommentDto comment) throws ExecutionException, InterruptedException {
        log.info("💬 Guardando comentario para objeto: {}", comment.getObjectId());
        
        // Establecer timestamp de creación si no existe
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(com.google.cloud.Timestamp.now());
        }
        
        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String commentId = docRef.getId();
        
        // Asignar el ID generado al comentario
        comment.setId(commentId);
        
        ApiFuture<WriteResult> future = docRef.set(comment);
        
        WriteResult result = future.get();
        log.info("✅ Comentario guardado exitosamente con ID: {}. Timestamp: {}", commentId, result.getUpdateTime());
        
        return comment;
    }

    /**
     * Obtiene comentarios por objeto cultural
     */
    public List<CommentDto> getCommentsByObjectId(String objectId) throws ExecutionException, InterruptedException {
        log.info("💬 Obteniendo comentarios para objeto: {}", objectId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("objectId", objectId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<CommentDto> comments = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CommentDto comment = document.toObject(CommentDto.class);
            // Asignar el ID del documento
            comment.setId(document.getId());
            comments.add(comment);
        }
        
        log.info("✅ {} comentarios encontrados para el objeto {}", comments.size(), objectId);
        return comments;
    }

    /**
     * Obtiene comentarios por usuario
     */
    public List<CommentDto> getCommentsByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("👤 Obteniendo comentarios del usuario: {}", userId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
        
        QuerySnapshot querySnapshot = future.get();
        
        List<CommentDto> comments = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            CommentDto comment = document.toObject(CommentDto.class);
            // Asignar el ID del documento
            comment.setId(document.getId());
            comments.add(comment);
        }
        
        log.info("✅ {} comentarios encontrados del usuario {}", comments.size(), userId);
        return comments;
    }

    /**
     * Elimina un comentario
     */
    public boolean deleteComment(String commentId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando comentario: {}", commentId);
        
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(commentId)
                .delete();
        
        WriteResult result = future.get();
        log.info("✅ Comentario eliminado exitosamente. Timestamp: {}", result.getUpdateTime());
        
        return true;
    }
} 