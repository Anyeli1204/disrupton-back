package com.disrupton.cultural.controller;

import com.disrupton.cultural.dto.*;
import com.disrupton.user.dto.UserDto;
import com.disrupton.comment.dto.CommentDto;
import com.disrupton.reaction.dto.ReactionDto;
import com.disrupton.cultural.service.FirebaseCulturalObjectService;
import com.disrupton.user.service.FirebaseUserService;
import com.disrupton.comment.service.FirebaseCommentService;
import com.disrupton.reaction.service.FirebaseReactionService;
import com.disrupton.storage.service.FirebaseStorageService;
import com.disrupton.kiriengine.service.KiriEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/firebase/cultural")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FirebaseCulturalController {

    private final FirebaseCulturalObjectService culturalObjectService;
    private final FirebaseUserService userService;
    private final FirebaseCommentService commentService;
    private final FirebaseReactionService reactionService;
    private final FirebaseStorageService storageService;
    private final KiriEngineService kiriEngineService;

    // ===== ENDPOINTS DE USUARIOS =====

    /**
     * Crear nuevo usuario
     */
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
        try {
            log.info("👤 Creando nuevo usuario: {}", user.getEmail());
            UserDto savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            log.error("❌ Error al crear usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        try {
            log.info("🔍 Obteniendo usuario: {}", userId);
            UserDto user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("❌ Error al obtener usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todos los usuarios
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            log.info("📋 Obteniendo todos los usuarios");
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Error al obtener usuarios: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINTS DE OBJETOS CULTURALES =====

    /**
     * Subir un nuevo objeto cultural con imágenes
     */
    @PostMapping("/objects")
    public ResponseEntity<?> uploadCulturalObject(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("createdBy") String createdBy,
            @RequestParam("images") List<MultipartFile> images) {
        
        try {
            log.info("🎨 Iniciando subida de objeto cultural: {}", title);
            
            // 1. Verificar que el creador existe
            UserDto creator = userService.getUserById(createdBy);
            if (creator == null) {
                return ResponseEntity.badRequest().body("Creador no encontrado");
            }
            
            // 2. Crear objeto cultural en Firebase
            CulturalObjectDto culturalObject = new CulturalObjectDto();
            culturalObject.setTitle(title);
            culturalObject.setDescription(description);
            culturalObject.setCreatedBy(createdBy);
            culturalObject.setStatus("pending");
            
            CulturalObjectDto savedObject = culturalObjectService.saveCulturalObject(culturalObject);
            
            // 3. TODO: Subir imágenes a KIRI Engine y obtener modelo 3D
            // TODO: Subir modelo 3D a Firebase Storage
            // TODO: Actualizar modelUrl en el objeto cultural
            
            log.info("✅ Objeto cultural guardado exitosamente: {}", savedObject.getTitle());
            return ResponseEntity.ok(savedObject);
            
        } catch (Exception e) {
            log.error("❌ Error al subir objeto cultural: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al procesar la solicitud");
        }
    }

    /**
     * Obtener todos los objetos culturales aprobados
     */
    @GetMapping("/objects")
    public ResponseEntity<List<CulturalObjectDto>> getApprovedCulturalObjects() {
        try {
            log.info("📋 Obteniendo objetos culturales aprobados");
            List<CulturalObjectDto> objects = culturalObjectService.getApprovedCulturalObjects();
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("❌ Error al obtener objetos culturales: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener objeto cultural por ID
     */
    @GetMapping("/objects/{objectId}")
    public ResponseEntity<CulturalObjectDto> getCulturalObjectById(@PathVariable String objectId) {
        try {
            log.info("🔍 Obteniendo objeto cultural: {}", objectId);
            CulturalObjectDto culturalObject = culturalObjectService.getCulturalObjectById(objectId);
            if (culturalObject == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(culturalObject);
        } catch (Exception e) {
            log.error("❌ Error al obtener objeto cultural: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener objetos culturales pendientes de revisión (solo moderadores)
     */
    @GetMapping("/objects/pending")
    public ResponseEntity<List<CulturalObjectDto>> getPendingCulturalObjects() {
        try {
            log.info("⏳ Obteniendo objetos culturales pendientes de revisión");
            List<CulturalObjectDto> objects = culturalObjectService.getPendingCulturalObjects();
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("❌ Error al obtener objetos culturales pendientes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Aprobar/rechazar objeto cultural (solo moderadores)
     */
    @PutMapping("/objects/{objectId}/status")
    public ResponseEntity<CulturalObjectDto> updateCulturalObjectStatus(
            @PathVariable String objectId,
            @RequestParam String status) {
        
        try {
            log.info("🔄 Actualizando estado del objeto cultural {} a: {}", objectId, status);
            
            CulturalObjectDto updatedObject = culturalObjectService.updateCulturalObjectStatus(objectId, status);
            
            if (updatedObject == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(updatedObject);
        } catch (Exception e) {
            log.error("❌ Error al actualizar estado del objeto cultural: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener objetos culturales de un creador
     */
    @GetMapping("/objects/creator/{createdBy}")
    public ResponseEntity<List<CulturalObjectDto>> getCulturalObjectsByCreator(@PathVariable String createdBy) {
        try {
            log.info("👤 Obteniendo objetos culturales del creador: {}", createdBy);
            List<CulturalObjectDto> objects = culturalObjectService.getCulturalObjectsByCreator(createdBy);
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            log.error("❌ Error al obtener objetos culturales del creador: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINTS DE COMENTARIOS =====

    /**
     * Agregar comentario a un objeto cultural
     */
    @PostMapping("/comments")
    public ResponseEntity<CommentDto> addComment(@RequestBody CommentDto comment) {
        try {
            log.info("💬 Agregando comentario al objeto: {}", comment.getObjectId());
            CommentDto savedComment = commentService.saveComment(comment);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            log.error("❌ Error al agregar comentario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener comentarios de un objeto cultural
     */
    @GetMapping("/objects/{objectId}/comments")
    public ResponseEntity<List<CommentDto>> getCommentsByObjectId(@PathVariable String objectId) {
        try {
            log.info("💬 Obteniendo comentarios del objeto: {}", objectId);
            List<CommentDto> comments = commentService.getCommentsByObjectId(objectId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("❌ Error al obtener comentarios: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINTS DE REACCIONES =====

    /**
     * Agregar reacción a un objeto cultural
     */
    @PostMapping("/reactions")
    public ResponseEntity<ReactionDto> addReaction(@RequestBody ReactionDto reaction) {
        try {
            log.info("❤️ Agregando reacción {} al objeto: {}", reaction.getType(), reaction.getObjectId());
            ReactionDto savedReaction = reactionService.saveReaction(reaction);
            return ResponseEntity.ok(savedReaction);
        } catch (Exception e) {
            log.error("❌ Error al agregar reacción: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener reacciones de un objeto cultural
     */
    @GetMapping("/objects/{objectId}/reactions")
    public ResponseEntity<List<ReactionDto>> getReactionsByObjectId(@PathVariable String objectId) {
        try {
            log.info("❤️ Obteniendo reacciones del objeto: {}", objectId);
            List<ReactionDto> reactions = reactionService.getReactionsByObjectId(objectId);
            return ResponseEntity.ok(reactions);
        } catch (Exception e) {
            log.error("❌ Error al obtener reacciones: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar reacción de un usuario a un objeto
     */
    @DeleteMapping("/reactions")
    public ResponseEntity<?> deleteUserReaction(
            @RequestParam String userId,
            @RequestParam String objectId) {
        try {
            log.info("🗑️ Eliminando reacción del usuario {} al objeto {}", userId, objectId);
            boolean deleted = reactionService.deleteUserReaction(userId, objectId);
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
} 