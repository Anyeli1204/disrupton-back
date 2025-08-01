    package com.disrupton.controller;
    import com.disrupton.model.Comment;
    import com.disrupton.moderation.ModerationService;
    import com.disrupton.service.CommentService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.client.HttpClientErrorException;
    import org.springframework.web.client.HttpServerErrorException;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.ExecutionException;
    import org.springframework.web.client.ResourceAccessException;


    @RestController
    @RequestMapping("/api/objects")
    @RequiredArgsConstructor
    public class CommentObjectController {

        private final ModerationService moderationService;
        private final CommentService comentarioService;

        @GetMapping("/{id}/comments")
        public List<Comment> obtenerComentarios(
                @PathVariable String id,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size
        ) throws ExecutionException, InterruptedException {
            return comentarioService.obtenerComentariosObject(id, page, size);
        }

        @PostMapping("/{objetoId}")
        public ResponseEntity<Map<String, Object>> comentarObjeto(
                @PathVariable String objetoId, @RequestBody Map<String, String> request) {
            String comentario = request.get("comentario");

            Map<String, Object> response = new HashMap<>();
            response.put("comentario", comentario);
            response.put("objetoId", objetoId);

            try {
                long inicio = System.currentTimeMillis();
                boolean esSeguro = moderationService.isCommentSafe(comentario);
                long tiempoRespuesta = System.currentTimeMillis() - inicio;

                response.put("tiempoRespuestaGemini", tiempoRespuesta + " ms");
                response.put("aprobado", esSeguro);

                if (esSeguro) {
                    comentarioService.saveCommentObject(comentario, objetoId, true, tiempoRespuesta);
                    response.put("mensaje", "✅ Comentario aprobado y guardado.");
                } else {
                    response.put("rechazado", true);
                    response.put("motivo", moderationService.getReasonIfUnsafe(comentario));
                    response.put("mensaje", "⚠️ Comentario rechazado por contenido inapropiado.");
                }

            } catch (Exception e) {
                response.put("error", e.getMessage());
                response.put("mensaje", "❌ Error interno al procesar comentario.");
            }

            return ResponseEntity.ok(response);
        }
    }
