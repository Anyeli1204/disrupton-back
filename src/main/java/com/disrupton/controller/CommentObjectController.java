    package com.disrupton.controller;
    import com.disrupton.dto.CommentDto;
    import com.disrupton.moderation.ModerationService;
    import com.disrupton.service.CommentService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;


    @RestController
    @RequestMapping("/api/objects")
    @Slf4j
    @RequiredArgsConstructor
    public class CommentObjectController {

        private final ModerationService moderationService;
        private final CommentService comentarioService;

        @GetMapping("/{objectId}/comments")
        public ResponseEntity<List<CommentDto>> obtenerComentarios(@PathVariable String objectId) {
            try {
                log.info("💬 Obteniendo comentarios del objeto: {}", objectId);
                List<CommentDto> comentarios = comentarioService.obtenerComentariosObject(objectId);
                return ResponseEntity.ok(comentarios);
            } catch (Exception e) {
                log.error("❌ Error al obtener comentarios del objeto {}: {}", objectId, e.getMessage(), e);
                return ResponseEntity.internalServerError().build();
            }
        }

        @PostMapping("/{objectId}/comments")
        public ResponseEntity<Map<String, Object>> comentarObjetoV2(
                @PathVariable String objectId, @RequestBody CommentDto commentDto) {

            Map<String, Object> response = new HashMap<>();

            try {
                log.info("💬 Procesando comentario DTO para objeto: {}", objectId);

                // Asegurar que el objectId coincida
                commentDto.setCulturalObjectId(objectId);

                long inicio = System.currentTimeMillis();
                boolean esSeguro = moderationService.isCommentSafe(commentDto.getText());
                long tiempoRespuesta = System.currentTimeMillis() - inicio;

                response.put("comentario", commentDto.getText());
                response.put("objectId", objectId);
                response.put("userId", commentDto.getUserId());
                response.put("tiempoRespuestaGemini", tiempoRespuesta + " ms");
                response.put("aprobado", esSeguro);

                if (esSeguro) {
                    comentarioService.saveCommentObject(
                            commentDto.getText(),
                            objectId,
                            commentDto.getUserId(),
                            true,
                            tiempoRespuesta
                    );
                    response.put("mensaje", "✅ Comentario aprobado y guardado.");
                    log.info("✅ Comentario DTO aprobado y guardado para objeto: {}", objectId);
                } else {
                    response.put("rechazado", true);
                    response.put("motivo", moderationService.getReasonIfUnsafe(commentDto.getText()));
                    response.put("mensaje", "⚠️ Comentario rechazado por contenido inapropiado.");
                    log.warn("⚠️ Comentario DTO rechazado para objeto: {}", objectId);
                }

            } catch (Exception e) {
                log.error("❌ Error al procesar comentario DTO para objeto {}: {}", objectId, e.getMessage(), e);
                response.put("error", e.getMessage());
                response.put("mensaje", "❌ Error interno al procesar comentario.");
                return ResponseEntity.internalServerError().body(response);
            }

            return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{objectId}/comments/{commentId}")
        public ResponseEntity<Map<String, Object>> eliminarComentario(
                @PathVariable String objectId, @PathVariable String commentId, @RequestParam String userId) {

            Map<String, Object> response = new HashMap<>();

            try {
                log.info("🗑️ Intentando eliminar comentario {} del objeto {} por usuario {}",
                        commentId, objectId, userId);

                boolean eliminado = comentarioService.deleteCommentObject(commentId, userId);

                if (eliminado) {
                    response.put("mensaje", "✅ Comentario eliminado exitosamente.");
                    response.put("commentId", commentId);
                    response.put("objectId", objectId);
                    response.put("eliminado", true);
                    log.info("✅ Comentario {} eliminado exitosamente", commentId);
                    return ResponseEntity.ok(response);
                } else {
                    response.put("mensaje", "❌ No se pudo eliminar el comentario. Verifica que seas el autor.");
                    response.put("eliminado", false);
                    log.warn("⚠️ No se pudo eliminar comentario {} - Sin permisos o no existe", commentId);
                    return ResponseEntity.badRequest().body(response);
                }

            } catch (Exception e) {
                log.error("❌ Error al eliminar comentario {}: {}", commentId, e.getMessage(), e);
                response.put("error", e.getMessage());
                response.put("mensaje", "❌ Error interno al eliminar comentario.");
                response.put("eliminado", false);
                return ResponseEntity.internalServerError().body(response);
            }
        }
    }
