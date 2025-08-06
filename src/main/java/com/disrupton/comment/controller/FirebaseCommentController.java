package com.disrupton.comment.controller;

import com.disrupton.comment.dto.CommentDto;
import com.disrupton.comment.service.FirebaseCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/firebase/cultural")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FirebaseCommentController {
    
    private final FirebaseCommentService commentService;
    
    /**
     * Crear nuevo comentario
     */
    @PostMapping("/comments")
    public ResponseEntity<CommentDto> createComment(@RequestBody CommentDto comment) {
        try {
            log.info("💬 Creando nuevo comentario para objeto: {}", comment.getObjectId());
            CommentDto savedComment = commentService.saveComment(comment);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            log.error("❌ Error al crear comentario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener comentarios por objeto cultural
     */
    @GetMapping("/comments/object/{objectId}")
    public ResponseEntity<List<CommentDto>> getCommentsByObjectId(@PathVariable String objectId) {
        try {
            log.info("💬 Obteniendo comentarios del objeto: {}", objectId);
            List<CommentDto> comments = commentService.getCommentsByObjectId(objectId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("❌ Error al obtener comentarios del objeto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener comentarios por usuario
     */
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<CommentDto>> getCommentsByUserId(@PathVariable String userId) {
        try {
            log.info("👤 Obteniendo comentarios del usuario: {}", userId);
            List<CommentDto> comments = commentService.getCommentsByUserId(userId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("❌ Error al obtener comentarios del usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Eliminar comentario
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId) {
        try {
            log.info("🗑️ Eliminando comentario: {}", commentId);
            boolean deleted = commentService.deleteComment(commentId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar comentario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 