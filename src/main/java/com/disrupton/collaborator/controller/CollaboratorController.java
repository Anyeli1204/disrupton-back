package com.disrupton.collaborator.controller;

import com.disrupton.collaborator.dto.CollaboratorDto;
import com.disrupton.collaborator.dto.CommentCollabRequestDto;
import com.disrupton.collaborator.dto.CommentCollabResponseDto;
import com.disrupton.collaborator.service.CollaboratorService;
import com.disrupton.exception.ModerationRejectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CollaboratorDto> createCollaborator(@RequestBody CollaboratorDto dto) {
        CollaboratorDto saved = collaboratorService.createCollaborator(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    /**
     * GET /api/collaborators - Listado de agentes culturales con filtros
     */
    @GetMapping
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
     * POST /api/collaborators/{id}/unlock - Desbloquear redes de contacto
     */
    @PostMapping("/{id}/unlock")
    public ResponseEntity<Map<String, Object>> unlockCollaborator(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, Object> paymentData) {

        String userId;
        
        if (userDetails != null) {
            // Si hay autenticación, usar el usuario autenticado
            userId = userDetails.getUsername();
            log.info("Usuario autenticado {} intentando desbloquear redes de contacto del agente {}", userId, id);
        } else if (paymentData != null && paymentData.containsKey("userId")) {
            // Si no hay autenticación pero hay userId en el body (para pruebas)
            userId = paymentData.get("userId").toString();
            log.info("Usuario no autenticado {} intentando desbloquear redes de contacto del agente {} (MODO PRUEBA)", userId, id);
        } else {
            log.warn("Intento de desbloqueo sin autenticación ni userId para agente {}", id);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado o falta userId en el body"));
        }

        Map<String, Object> result = collaboratorService.unlockCollaborator(id, userId, paymentData);

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/comments")
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



    @DeleteMapping("/{id}")
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