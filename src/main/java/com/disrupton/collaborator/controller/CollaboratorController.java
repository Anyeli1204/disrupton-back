package com.disrupton.collaborator.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import com.disrupton.collaborator.dto.*;
import com.disrupton.collaborator.service.CollaboratorService;
import com.disrupton.exception.ModerationRejectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborators")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    /**
     * POST /api/collaborators - Crear un colaborador o agente cultural
     */
    @PostMapping
    @RequireRole({UserRole.ADMIN})
    public ResponseEntity<CollaboratorDto> createCollaborator(@RequestBody CollaboratorDto dto) {
        CollaboratorDto saved = collaboratorService.createCollaborator(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    /**
     * GET /api/collaborators - Listado de agentes culturales con filtros
     */
    @GetMapping
    @RequireRole({UserRole.ADMIN, UserRole.USER})
    public ResponseEntity<Page<CollaboratorDto>> getCollaborators(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String nombre,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        String caller = userDetails != null ? userDetails.getUsername() : "anónimo";
        log.info("Usuario {} obteniendo agentes culturales - región: {}, tipo: {}, nombre: {}", caller, region, tipo, nombre);

        String currentUserId = userDetails != null ? userDetails.getUsername() : null;
        Page<CollaboratorDto> collaborators = collaboratorService.getCollaborators(region, tipo, nombre, pageable, currentUserId);

        return ResponseEntity.ok(collaborators);
    }

    /**
     * GET /api/collaborators/{id} - Obtener detalle de agente cultural
     */
    @GetMapping("/{id}")
    @RequireRole({UserRole.ADMIN, UserRole.USER})
    public ResponseEntity<CollaboratorDto> getCollaboratorDetail(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Usuario {} solicitando detalle del agente cultural {}",
                userDetails != null ? userDetails.getUsername() : "anónimo", id);

        String userId = userDetails != null ? userDetails.getUsername() : null;
        CollaboratorDto collaborator = collaboratorService.getCollaboratorDetail(id, userId);

        return ResponseEntity.ok(collaborator);
    }

    /**
     * PUT /api/collaborators/{id} - Actualizar campos de un colaborador
     */
    @PutMapping("/{id}")
    @RequireRole({UserRole.ADMIN})
    public ResponseEntity<?> updateCollaborator(
            @PathVariable String id,
            @RequestBody CollaboratorDto updateDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Usuario {} actualizando colaborador {}",
                userDetails != null ? userDetails.getUsername() : "anónimo", id);

        try {
            CollaboratorDto updatedCollaborator = collaboratorService.updateCollaborator(id, updateDto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "Colaborador actualizado exitosamente",
                    "colaborador", updatedCollaborator
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error actualizando colaborador {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error interno al actualizar colaborador"
            ));
        }
    }
    /**
     * POST /api/collaborators/{id}/unlock - Desbloquear redes de contacto
     * Requiere autenticación.
     */
    @PostMapping("/{id}/unlock")
    @RequireRole({UserRole.ADMIN, UserRole.USER})
    public ResponseEntity<?> unlockCollaborator(
            @PathVariable String id, @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UnlockRequestDto requestDto) {
        // 1. Seguridad obligatoria: se elimina el modo de prueba
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticación requerida para esta operación"));
        }

        String userId = userDetails.getUsername();
        log.info("Usuario autenticado {} intentando desbloquear al agente {}", userId, id);

        try {
            // 4. El servicio ahora puede devolver un DTO o lanzar una excepción
            UnlockResponseDto result = collaboratorService.unlockCollaborator(id, userId, requestDto);
            return ResponseEntity.ok(result); // 3. Devolver un DTO de respuesta

        } catch (IllegalStateException e) {
            // 4. Capturar excepciones de negocio específicas del servicio
            log.warn("Intento de desbloqueo fallido para el usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/comments")
    @RequireRole({UserRole.ADMIN, UserRole.USER})
    public ResponseEntity<?> addCommentToCollaborator(
            @PathVariable String id,
            @RequestBody CommentCollabRequestDto request) {

        if (request.getUserId() == null || request.getUserId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falta el ID del usuario"));
        }
        if (request.getUserId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "El mismo agente cultural no puede comentarse a si mismo"));
        }

        try {
            CommentCollabResponseDto response = collaboratorService.processAndSaveComment(id, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mensaje", "✅ Comentario aprobado y guardado",
                    "comentario", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (ModerationRejectedException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "rechazado", true,
                    "motivo", e.getMessage(),
                    "mensaje", "⚠️ Comentario rechazado por contenido inapropiado"
            ));
        } catch (Exception e) {
            log.error("Error interno al procesar comentario del colaborador: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error interno al guardar comentario"
            ));
        }
    }

    /**
     * GET /api/collaborators/{id}/comments - Obtener comentarios de un colaborador
     */
    @GetMapping("/{id}/comments")
    @RequireRole({UserRole.ADMIN, UserRole.USER})
    public ResponseEntity<List<CommentCollabResponseDto>> getCollaboratorComments(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Usuario {} solicitando comentarios del colaborador {}",
                userDetails != null ? userDetails.getUsername() : "anónimo", id);

        try {
            List<CommentCollabResponseDto> comments = collaboratorService.getCollaboratorComments(id);
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error obteniendo comentarios del colaborador {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.ADMIN})
    public ResponseEntity<?> deleteCollaborator(@PathVariable String id) {
        collaboratorService.deleteCollaborator(id);
        return ResponseEntity.ok(Map.of("message", "Colaborador eliminado correctamente"));
    }

    /**
     * Exception handler para manejar errores específicos
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Error de argumento inválido: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Error interno: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(Map.of("error", "Error interno del servidor"));
    }
}