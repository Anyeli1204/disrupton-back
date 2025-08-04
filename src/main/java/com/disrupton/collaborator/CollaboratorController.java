package com.disrupton.collaborator;

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
            Pageable pageable) {

        log.info("Obteniendo agentes culturales - región: {}, tipo: {}, nombre: {}", region, tipo, nombre);

        Page<CollaboratorDto> collaborators = collaboratorService.getCollaborators(region, tipo, nombre, pageable);

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

        if (userDetails == null) {
            log.warn("Intento de desbloqueo sin autenticación para agente {}", id);
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        String userId = userDetails.getUsername();
        log.info("Usuario {} intentando desbloquear redes de contacto del agente {}", userId, id);

        Map<String, Object> result = collaboratorService.unlockCollaborator(id, userId, paymentData);

        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/collaborators/{id}/access - Verificar acceso a redes de contacto
     */
    @GetMapping("/{id}/access")
    public ResponseEntity<Map<String, Object>> checkAccess(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails != null ? userDetails.getUsername() : null;
        log.info("Verificando acceso del usuario {} al agente {}", userId, id);

        boolean hasAccess = collaboratorService.hasAccess(id, userId);

        Map<String, Object> response = Map.of(
                "agentId", id,
                "userId", userId != null ? userId : "anónimo",
                "hasAccess", hasAccess,
                "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/collaborators/{id}/stats - Estadísticas del agente cultural
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getCollaboratorStats(@PathVariable String id) {
        log.info("Obteniendo estadísticas del agente cultural {}", id);

        Map<String, Object> stats = collaboratorService.getCollaboratorStats(id);
        return ResponseEntity.ok(stats);
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