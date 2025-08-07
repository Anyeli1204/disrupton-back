package com.disrupton.mural.service;

import com.disrupton.comment.dto.CommentDto;
import com.disrupton.comment.model.Comment;
import com.disrupton.mural.model.Mural;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class MuralService {

    private static final String PREGUNTAS_MURALES = "preguntas_murales";
    private static final String COMENTARIOS_M = "comentarios_mural";
    private final Firestore db = FirestoreClient.getFirestore();

    public Mural crearPregunta(String textoPregunta, List<String> imagenes) {
        Firestore db = FirestoreClient.getFirestore();

        Map<String, Object> data = new HashMap<>();
        data.put("pregunta", textoPregunta);
        data.put("imagenes", imagenes);
        data.put("timestamp", FieldValue.serverTimestamp());

        String id = UUID.randomUUID().toString();

        db.collection(PREGUNTAS_MURALES).document(id).set(data); // No es necesario capturar ApiFuture aquí

        Mural pregunta = new Mural();
        pregunta.setId(id);
        pregunta.setPregunta(textoPregunta);
        pregunta.setImagenes(imagenes);
        pregunta.setTimestamp(System.currentTimeMillis()); // Solo representativo

        return pregunta;
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
            dto.setUserId(comment.getAuthorId());
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
        comment.setAuthorId(dto.getUserId());
        comment.setCreatedAt(createdAt);
        comment.setPreguntaId(dto.getPreguntaId());
        comment.setParentCommentId(dto.getParentCommentId());
        comment.setIsModerated(true); // Ya fue moderado antes del service

        db.collection(COMENTARIOS_M).document(commentId).set(comment).get();

        // Retornar DTO con ID y timestamp asignado
        dto.setId(commentId);
        dto.setCreatedAt(createdAt);
        return dto;
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
            if (!comment.getAuthorId().equals(userId)) {
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
