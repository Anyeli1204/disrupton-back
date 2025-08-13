package com.disrupton.comment.service;

import com.disrupton.comment.dto.CommentDto;
import com.disrupton.comment.model.Comment;
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
    private static final String COMENTARIOS_O = "comments";

    /**
     * Guarda un nuevo comentario en Firestore
     */
    public CommentDto saveComment(CommentDto comment) throws ExecutionException, InterruptedException {
        log.info("💬 Guardando comentario para objeto: {}", comment.getCulturalObjectId());

        // Establecer timestamp de creación si no existe
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(com.google.cloud.Timestamp.now());
        }

        // Crear documento con ID automático
        DocumentReference docRef = db.collection(COMENTARIOS_O).document();
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
    public List<CommentDto> obtenerComentariosObject(String objectId) throws ExecutionException, InterruptedException {
        List<CommentDto> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection(COMENTARIOS_O)
                .whereEqualTo("culturalObjectId", objectId) // Cambio: objetoCulturalId → culturalObjectId
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();

        for (DocumentSnapshot doc : future.get().getDocuments()) {
            Comment comment = doc.toObject(Comment.class);
            CommentDto dto = new CommentDto();
            dto.setId(doc.getId());
            dto.setText(comment.getContent());
            dto.setUserId(comment.getAuthorId());
            dto.setCulturalObjectId(comment.getCulturalObjectId()); // Nuevo campo
            dto.setCreatedAt(comment.getCreatedAt());
            result.add(dto);
        }
        return result;
    }

    /**
     * Obtiene comentarios por usuario
     */
    public List<CommentDto> getCommentsByUserId(String userId) throws ExecutionException, InterruptedException {
        log.info("👤 Obteniendo comentarios del usuario: {}", userId);
        
        ApiFuture<QuerySnapshot> future = db.collection(COMENTARIOS_O)
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
    public boolean deleteCommentObject(String commentId, String userId) throws ExecutionException, InterruptedException {
        try {
            // 1. Verificar que el comentario existe y obtener sus datos
            DocumentSnapshot doc = db.collection(COMENTARIOS_O).document(commentId).get().get();

            if (!doc.exists()) {
                log.warn("⚠️ Comentario no encontrado: {}", commentId);
                return false;
            }

            Comment comment = doc.toObject(Comment.class);

            // 2. Verificar que el usuario es el autor del comentario
            if (!comment.getAuthorId().equals(userId)) {
                log.warn("⚠️ Usuario {} no autorizado para eliminar comentario {}", userId, commentId);
                return false;
            }

            // 3. Eliminar el comentario
            db.collection(COMENTARIOS_O).document(commentId).delete().get();
            log.info("✅ Comentario eliminado exitosamente: {}", commentId);
            return true;

        } catch (Exception e) {
            log.error("❌ Error al eliminar comentario {}: {}", commentId, e.getMessage(), e);
            throw e;
        }
    }
} 