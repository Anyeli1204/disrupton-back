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
     * Listar comentarios por ID de pregunta (incluye respuestas anidadas)
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
            dto.setId(doc.getId()); // Usar el ID del documento
            dto.setText(comment.getContent());
            dto.setUserId(comment.getAuthorId());
            dto.setPreguntaId(comment.getPreguntaId());
            dto.setParentCommentId(comment.getParentCommentId());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setImageUrls(comment.getImageUrls()); // Agregar URLs de imágenes

            // Obtener nombre del usuario
            try {
                String userName = getUserName(comment.getAuthorId());
                dto.setUserName(userName);
            } catch (Exception e) {
                log.warn("No se pudo obtener el nombre del usuario {}: {}", comment.getAuthorId(), e.getMessage());
                dto.setUserName("Usuario Anónimo");
            }

            // Obtener conteo de reacciones para este comentario
            dto.setLikeCount(getReactionCount(doc.getId(), "like"));
            dto.setDislikeCount(getReactionCount(doc.getId(), "dislike"));

            result.add(dto);
        }

        // Organizar comentarios en estructura jerárquica (comentarios principales primero, luego respuestas)
        return organizeCommentsHierarchy(result);
    }

    /**
     * Organizar comentarios en jerarquía: comentarios principales primero, seguidos de sus respuestas
     */
    private List<CommentDto> organizeCommentsHierarchy(List<CommentDto> allComments) {
        List<CommentDto> organized = new ArrayList<>();
        Map<String, List<CommentDto>> repliesByParent = new HashMap<>();

        // Separar comentarios principales de respuestas
        for (CommentDto comment : allComments) {
            if (comment.getParentCommentId() == null || comment.getParentCommentId().isEmpty()) {
                // Es un comentario principal
                organized.add(comment);
            } else {
                // Es una respuesta, agrupar por comentario padre
                repliesByParent.computeIfAbsent(comment.getParentCommentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        // Insertar respuestas después de cada comentario principal
        List<CommentDto> finalResult = new ArrayList<>();
        for (CommentDto mainComment : organized) {
            finalResult.add(mainComment);

            // Agregar respuestas de este comentario
            List<CommentDto> replies = repliesByParent.get(mainComment.getId());
            if (replies != null) {
                // Ordenar respuestas por fecha de creación
                replies.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
                finalResult.addAll(replies);
            }
        }

        return finalResult;
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
        comment.setImageUrls(dto.getImageUrls()); // Agregar URLs de imágenes
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
            if (!commentExists(commentId)) {
                log.warn("⚠️ Comentario no encontrado: {}", commentId);
                return false;
            }

            DocumentSnapshot doc = db.collection(COMENTARIOS_M).document(commentId).get().get();

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

    /**
     * Obtener conteo de reacciones para un comentario específico
     */
    public Integer getReactionCount(String commentId, String reactionType) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("reactions")
                    .whereEqualTo("commentId", commentId)
                    .whereEqualTo("type", reactionType)
                    .get();

            QuerySnapshot querySnapshot = future.get();
            int count = querySnapshot.size();
            // Asegurar que el conteo nunca sea negativo
            return Math.max(0, count);
        } catch (Exception e) {
            log.error("Error al obtener conteo de reacciones: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Obtener la reacción de un usuario específico para un comentario
     */
    public String getUserReactionForComment(String userId, String commentId) {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("reactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("commentId", commentId)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                return (String) doc.get("type");
            }
            return null;
        } catch (Exception e) {
            log.error("Error al obtener reacción del usuario: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Agregar o actualizar reacción a un comentario del mural
     */
    public boolean addOrUpdateCommentReaction(String userId, String commentId, String reactionType) {
        try {
            // Validar parámetros de entrada
            if (userId == null || userId.trim().isEmpty()) {
                log.error("UserId no puede ser nulo o vacío");
                return false;
            }
            if (commentId == null || commentId.trim().isEmpty()) {
                log.error("CommentId no puede ser nulo o vacío");
                return false;
            }
            if (!reactionType.equals("like") && !reactionType.equals("dislike")) {
                log.error("Tipo de reacción inválido: {}", reactionType);
                return false;
            }

            // Verificar si el comentario existe
            DocumentSnapshot commentDoc = db.collection(COMENTARIOS_M).document(commentId).get().get();
            if (!commentDoc.exists()) {
                log.error("Comentario no encontrado: {}", commentId);
                return false;
            }

            // Primero verificar si ya existe una reacción del usuario para este comentario
            ApiFuture<QuerySnapshot> existingReaction = db.collection("reactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("commentId", commentId)
                    .get();

            QuerySnapshot snapshot = existingReaction.get();

            if (!snapshot.isEmpty()) {
                // Ya existe una reacción del usuario para este comentario
                DocumentSnapshot existingDoc = snapshot.getDocuments().get(0);
                String currentType = (String) existingDoc.get("type");

                if (currentType != null && currentType.equals(reactionType)) {
                    // Si es la misma reacción, eliminarla (toggle off)
                    existingDoc.getReference().delete().get();
                    log.info("Reacción {} eliminada para comentario {} por usuario {}", reactionType, commentId, userId);
                } else {
                    // Cambiar el tipo de reacción (de like a dislike o viceversa)
                    existingDoc.getReference().update("type", reactionType).get();
                    log.info("Reacción cambiada de {} a {} para comentario {} por usuario {}", currentType, reactionType, commentId, userId);
                }
            } else {
                // No existe reacción previa, crear nueva
                Map<String, Object> newReaction = new HashMap<>();
                newReaction.put("userId", userId);
                newReaction.put("commentId", commentId);
                newReaction.put("type", reactionType);
                newReaction.put("createdAt", Timestamp.now());

                db.collection("reactions").add(newReaction).get();
                log.info("Nueva reacción {} creada para comentario {} por usuario {}", reactionType, commentId, userId);
            }

            return true;
        } catch (Exception e) {
            log.error("Error al procesar reacción para comentario {} por usuario {}: {}", commentId, userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verificar si un comentario existe
     */
    public boolean commentExists(String commentId) {
        try {
            DocumentSnapshot doc = db.collection(COMENTARIOS_M).document(commentId).get().get();
            return doc.exists();
        } catch (Exception e) {
            log.error("Error al verificar existencia del comentario {}: {}", commentId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtener el nombre del usuario por su ID
     */
    private String getUserName(String userId) throws ExecutionException, InterruptedException {
        try {
            DocumentSnapshot userDoc = db.collection("users").document(userId).get().get();
            if (userDoc.exists()) {
                String name = userDoc.getString("name");
                String email = userDoc.getString("email");

                // Priorizar nombre, luego email, luego ID
                if (name != null && !name.trim().isEmpty()) {
                    return name;
                } else if (email != null && !email.trim().isEmpty()) {
                    return email.split("@")[0]; // Usar parte antes del @ del email
                }
            }
            return "Usuario " + userId.substring(0, Math.min(8, userId.length()));
        } catch (Exception e) {
            log.warn("Error al obtener nombre del usuario {}: {}", userId, e.getMessage());
            return "Usuario " + userId.substring(0, Math.min(8, userId.length()));
        }
    }
}
