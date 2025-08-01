package com.disrupton.mural;

import com.disrupton.model.Comment;
import com.disrupton.moderation.ModerationService;
import com.disrupton.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class CommentMuralController {

    private final ModerationService moderationService;
    private final CommentService comentarioService;
    private final MuralService muralService;


    @PostMapping("/mural")
    public ResponseEntity<Map<String, Object>> crearPregunta(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String pregunta = (String) request.get("pregunta");
            List<String> imagenes = (List<String>) request.get("imagenes");

            if (pregunta == null || pregunta.isBlank()) {
                response.put("error", "La pregunta no puede estar vacía.");
                return ResponseEntity.badRequest().body(response);
            }

            Mural nuevaPregunta = muralService.crearPregunta(pregunta, imagenes);
            response.put("mensaje", "✅ Pregunta del mural creada.");
            response.put("id", nuevaPregunta.getId());
            response.put("pregunta", nuevaPregunta.getPregunta());
            response.put("imagenes", nuevaPregunta.getImagenes());
        } catch (Exception e) {
            response.put("error", "❌ No se pudo crear la pregunta del mural.");
            response.put("detalle", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mural/{preguntaId}")
    public ResponseEntity<List<Comment>> obtenerComentarios(
            @PathVariable String preguntaId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Comment> comentarios = comentarioService.obtenerComentariosMural(preguntaId, page, size);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/mural/{preguntaId}")
    public ResponseEntity<Map<String, Object>> comentarMural(@PathVariable String preguntaId,
                                                             @RequestBody Map<String, String> request) {
        String comentario = request.get("comentario");

        Map<String, Object> response = new HashMap<>();
        response.put("comentario", comentario);
        response.put("preguntaId", preguntaId);

        try {
            long inicio = System.currentTimeMillis();
            boolean esSeguro = moderationService.isCommentSafe(comentario);
            long tiempoRespuesta = System.currentTimeMillis() - inicio;

            response.put("tiempoRespuestaGemini", tiempoRespuesta + " ms");
            response.put("aprobado", esSeguro);

            if (esSeguro) {
                comentarioService.saveCommentMural(comentario, preguntaId, true, tiempoRespuesta);
                response.put("mensaje", "✅ Comentario para mural aprobado y guardado.");
            } else {
                response.put("rechazado", true);
                response.put("motivo", moderationService.getReasonIfUnsafe(comentario));
                response.put("mensaje", "⚠️ Comentario rechazado por contenido inapropiado.");
            }

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("mensaje", "❌ Error interno al procesar comentario del mural.");
        }

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/mural/{preguntaId}")
    public ResponseEntity<String> eliminarComentarioMural(
            @PathVariable String preguntaId,
            @RequestBody Map<String, String> request) {
        String comentarioId = request.get("id");

        if (comentarioId == null || comentarioId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del comentario es obligatorio.");
        }

        try {
            boolean eliminado = comentarioService.deleteComment(comentarioId);
            if (eliminado) {
                return ResponseEntity.ok("Comentario eliminado exitosamente.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el comentario con ese ID.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar comentario: " + e.getMessage());
        }
    }

}
