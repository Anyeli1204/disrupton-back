package com.disrupton.reaction.controller;

import com.disrupton.reaction.service.FirebaseReactionService;
import com.disrupton.reaction.dto.ReactionDto;
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
public class FirebaseReactionController {
    
    private final FirebaseReactionService reactionService;
    
    /**
     * Crear nueva reacción
     */
    @PostMapping("/reactions")
    public ResponseEntity<ReactionDto> createReaction(@RequestBody ReactionDto reaction) {
        try {
            log.info("❤️ Creando nueva reacción {} para objeto: {}", reaction.getType(), reaction.getObjectId());
            ReactionDto savedReaction = reactionService.saveReaction(reaction);
            return ResponseEntity.ok(savedReaction);
        } catch (Exception e) {
            log.error("❌ Error al crear reacción: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener reacciones por objeto cultural
     */
    @GetMapping("/reactions/object/{objectId}")
    public ResponseEntity<List<ReactionDto>> getReactionsByObjectId(@PathVariable String objectId) {
        try {
            log.info("❤️ Obteniendo reacciones del objeto: {}", objectId);
            List<ReactionDto> reactions = reactionService.getReactionsByObjectId(objectId);
            return ResponseEntity.ok(reactions);
        } catch (Exception e) {
            log.error("❌ Error al obtener reacciones del objeto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener reacciones por usuario
     */
    @GetMapping("/reactions/user/{userId}")
    public ResponseEntity<List<ReactionDto>> getReactionsByUserId(@PathVariable String userId) {
        try {
            log.info("👤 Obteniendo reacciones del usuario: {}", userId);
            List<ReactionDto> reactions = reactionService.getReactionsByUserId(userId);
            return ResponseEntity.ok(reactions);
        } catch (Exception e) {
            log.error("❌ Error al obtener reacciones del usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Eliminar reacción
     */
    @DeleteMapping("/reactions/{reactionId}")
    public ResponseEntity<?> deleteReaction(@PathVariable String reactionId) {
        try {
            log.info("🗑️ Eliminando reacción: {}", reactionId);
            boolean deleted = reactionService.deleteReaction(reactionId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar reacción: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Eliminar reacción de usuario específica
     */
    @DeleteMapping("/reactions/user/{userId}/object/{objectId}")
    public ResponseEntity<?> deleteUserReaction(@PathVariable String userId, @PathVariable String objectId) {
        try {
            log.info("🗑️ Eliminando reacción del usuario {} al objeto {}", userId, objectId);
            boolean deleted = reactionService.deleteUserReaction(userId, objectId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar reacción del usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 