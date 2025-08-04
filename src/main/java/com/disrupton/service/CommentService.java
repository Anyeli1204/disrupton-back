package com.disrupton.service;

import com.disrupton.dto.CommentDto;
import com.disrupton.model.Comment;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class CommentService {
    private static final String COMENTARIOS_O = "comentarios_objetos";
    private static final String COMENTARIOS_M = "comentarios_mural";
    private final Firestore db = FirestoreClient.getFirestore();

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
            dto.setUserId(comment.getAuthorUserId());
            dto.setCulturalObjectId(comment.getCulturalObjectId()); // Nuevo campo
            dto.setCreatedAt(comment.getCreatedAt());
            result.add(dto);
        }
        return result;
    }


    public void saveCommentObject(String content, String culturalObjectId, String userId, boolean isModerated, long responseTimeMs) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("authorUserId", userId); // Nuevo campo
        data.put("isModerated", isModerated);
        data.put("culturalObjectId", culturalObjectId); // Cambio: id_Object → culturalObjectId
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("responseTimeMs", responseTimeMs);

        ApiFuture<DocumentReference> future = db.collection(COMENTARIOS_O).add(data);
        ApiFutures.addCallback(future, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(DocumentReference result) {
                System.out.println("Comentario objeto cultural guardado con ID: " + result.getId());
            }
            @Override
            public void onFailure(Throwable t) {
                System.err.println("Error al guardar comentario objeto cultural: " + t.getMessage());
            }
        }, Runnable::run);
    }

    public List<CommentDto> getCommentsByPreguntaId(String preguntaId) throws ExecutionException, InterruptedException {
        List<CommentDto> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection(COMENTARIOS_M)
                .whereEqualTo("preguntaId", preguntaId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();

        for (DocumentSnapshot doc : future.get().getDocuments()) {
            Comment comment = doc.toObject(Comment.class);
            CommentDto dto = new CommentDto();
            dto.setId(comment.getId());
            dto.setText(comment.getContent());
            dto.setUserId(comment.getAuthorUserId());
            dto.setPreguntaId(comment.getPreguntaId());
            dto.setParentCommentId(comment.getParentCommentId());
            dto.setCreatedAt(comment.getCreatedAt());
            result.add(dto);
        }
        return result;
    }
    public CommentDto saveCommentToMural(CommentDto dto) throws ExecutionException, InterruptedException {
        String commentId = UUID.randomUUID().toString();
        Timestamp createdAt = Timestamp.now();

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setContent(dto.getText());
        comment.setAuthorUserId(dto.getUserId());
        comment.setCreatedAt(createdAt);
        comment.setPreguntaId(dto.getPreguntaId());
        comment.setParentCommentId(dto.getParentCommentId());
        comment.setModerated(true); // Ya fue moderado antes del service

        db.collection(COMENTARIOS_M).document(commentId).set(comment).get();

        // Retornar DTO con ID y timestamp asignado
        dto.setId(commentId);
        dto.setCreatedAt(createdAt);
        return dto;
    }

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
            if (!comment.getAuthorUserId().equals(userId)) {
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

    public boolean deleteCommentMural(String commentId, String userId) throws ExecutionException, InterruptedException {
        try {
            // 1. Verificar que el comentario existe y obtener sus datos
            DocumentSnapshot doc = db.collection(COMENTARIOS_M).document(commentId).get().get();

            if (!doc.exists()) {
                log.warn("⚠️ Comentario no encontrado: {}", commentId);
                return false;
            }

            Comment comment = doc.toObject(Comment.class);

            // 2. Verificar que el usuario es el autor del comentario
            if (!comment.getAuthorUserId().equals(userId)) {
                log.warn("⚠️ Usuario {} no autorizado para eliminar comentario {}", userId, commentId);
                return false;
            }

            // 3. Eliminar el comentario
            db.collection(COMENTARIOS_M).document(commentId).delete().get();
            log.info("✅ Comentario eliminado exitosamente: {}", commentId);
            return true;

        } catch (Exception e) {
            log.error("❌ Error al eliminar comentario {}: {}", commentId, e.getMessage(), e);
            throw e;
        }
    }

}
