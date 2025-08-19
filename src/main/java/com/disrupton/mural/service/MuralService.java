package com.disrupton.mural.service;

import com.disrupton.comment.dto.CommentDto;
import com.disrupton.comment.model.Comment;
import com.disrupton.mural.model.MuralDto;
import com.disrupton.mural.model.MuralQuestion;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class MuralService {

    private static final String PREGUNTAS_MURALES = "preguntas_murales";
    private static final String COMENTARIOS_M = "comentarios_mural";
    private final Firestore db = FirestoreClient.getFirestore();

    /**
     * Crear una pregunta con duración definida (ej. 15 días)
     */
    public MuralDto crearPregunta(String textoPregunta, List<String> imagenes, int diasDuracion) {
        String id = UUID.randomUUID().toString();

        Date fechaCreacion = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaCreacion);
        cal.add(Calendar.DAY_OF_YEAR, diasDuracion);
        Date fechaExpiracion = cal.getTime();

        // Formato legible
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String fechaCreacionStr = sdf.format(fechaCreacion);
        String fechaExpiracionStr = sdf.format(fechaExpiracion);

        Map<String, Object> data = new HashMap<>();
        data.put("pregunta", textoPregunta);
        data.put("imagenes", imagenes);
        data.put("startDate", Timestamp.of(fechaCreacion));   // Firestore Timestamp
        data.put("endDate", Timestamp.of(fechaExpiracion));
        data.put("fechaCreacionStr", fechaCreacionStr);       // Legible
        data.put("fechaExpiracionStr", fechaExpiracionStr);

        db.collection(PREGUNTAS_MURALES).document(id).set(data);

        MuralDto pregunta = new MuralDto();
        pregunta.setId(id);
        pregunta.setPregunta(textoPregunta);
        pregunta.setImagenes(imagenes);
        pregunta.setTimestamp(fechaCreacion.getTime());

        return pregunta;
    }

    /**
     * Obtener la pregunta activa en este momento
     */
    public MuralQuestion getActiveQuestion() throws ExecutionException, InterruptedException {
        Timestamp now = Timestamp.now();
        Query query = db.collection(PREGUNTAS_MURALES)
                .whereLessThanOrEqualTo("startDate", now)
                .whereGreaterThanOrEqualTo("endDate", now)
                .limit(1);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        if (!querySnapshot.get().isEmpty()) {
            DocumentSnapshot doc = querySnapshot.get().getDocuments().get(0);
            MuralQuestion q = doc.toObject(MuralQuestion.class);
            if (q != null) {
                q.setId(doc.getId()); // ← Asignar el ID del documento
                // Mapear manualmente el campo pregunta a content
                if (doc.contains("pregunta")) {
                    q.setContent((String) doc.get("pregunta"));
                }
                q.setActive(q.isCurrentlyActive());
            }
            return q;
        }
        return null;
    }

    /**
     * Listar comentarios por ID de pregunta
     */
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

    /**
     * Guardar un comentario nuevo
     */
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

        dto.setId(commentId);
        dto.setCreatedAt(createdAt);
        return dto;
    }

    /**
     * Eliminar un comentario si el usuario es el autor
     */
    public boolean deleteCommentMural(String commentId, String userId) throws ExecutionException, InterruptedException {
        try {
            DocumentSnapshot doc = db.collection(COMENTARIOS_M).document(commentId).get().get();

            if (!doc.exists()) {
                log.warn("⚠️ Comentario no encontrado: {}", commentId);
                return false;
            }

            Comment comment = doc.toObject(Comment.class);

            if (!comment.getAuthorId().equals(userId)) {
                log.warn("⚠️ Usuario {} no autorizado para eliminar comentario {}", userId, commentId);
                return false;
            }

            db.collection(COMENTARIOS_M).document(commentId).delete().get();
            log.info("✅ Comentario eliminado exitosamente: {}", commentId);
            return true;

        } catch (Exception e) {
            log.error("❌ Error al eliminar comentario {}: {}", commentId, e.getMessage(), e);
            throw e;
        }
    }
}
