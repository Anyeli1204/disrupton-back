package com.disrupton.userAccess.controller;

import com.disrupton.userAccess.dto.UserAccessDto;
import com.disrupton.userAccess.dto.UserAccessRequest;
import com.disrupton.userAccess.service.UserAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-access")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserAccessController {

    private final UserAccessService userAccessService;

    /**
     * Obtiene todos los accesos de usuario
     */
    @GetMapping
    public ResponseEntity<List<UserAccessDto>> getAllUserAccess() {
        try {
            log.info("Getting all user access");
            List<UserAccessDto> accesses = userAccessService.getAllUserAccess();
            return ResponseEntity.ok(accesses);
        } catch (Exception e) {
            log.error("Error getting all user access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene un acceso por ID
     */
    @GetMapping("/{accessId}")
    public ResponseEntity<UserAccessDto> getUserAccessById(@PathVariable String accessId) {
        try {
            log.info("Getting user access by ID: {}", accessId);
            UserAccessDto access = userAccessService.getUserAccessById(accessId);
            if (access != null) {
                return ResponseEntity.ok(access);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting user access by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea un nuevo acceso de usuario
     */
    @PostMapping
    public ResponseEntity<UserAccessDto> createUserAccess(@RequestBody UserAccessRequest request) {
        try {
            log.info("Creating new user access for user: {} - Type: {}", request.getUserId(), request.getAccessType());
            UserAccessDto createdAccess = userAccessService.createUserAccess(request);
            return ResponseEntity.ok(createdAccess);
        } catch (Exception e) {
            log.error("Error creating user access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualiza un acceso de usuario
     */
    @PutMapping("/{accessId}")
    public ResponseEntity<UserAccessDto> updateUserAccess(
            @PathVariable String accessId,
            @RequestBody UserAccessRequest request) {
        try {
            log.info("Updating user access: {}", accessId);
            UserAccessDto updatedAccess = userAccessService.updateUserAccess(accessId, request);
            if (updatedAccess != null) {
                return ResponseEntity.ok(updatedAccess);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating user access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Elimina un acceso de usuario
     */
    @DeleteMapping("/{accessId}")
    public ResponseEntity<?> deleteUserAccess(@PathVariable String accessId) {
        try {
            log.info("Deleting user access: {}", accessId);
            boolean deleted = userAccessService.deleteUserAccess(accessId);
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"User access deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting user access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene accesos por usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAccessDto>> getUserAccessByUserId(@PathVariable String userId) {
        try {
            log.info("Getting user access for user: {}", userId);
            List<UserAccessDto> accesses = userAccessService.getUserAccessByUserId(userId);
            return ResponseEntity.ok(accesses);
        } catch (Exception e) {
            log.error("Error getting user access by user ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene accesos por tipo
     */
    @GetMapping("/type/{accessType}")
    public ResponseEntity<List<UserAccessDto>> getUserAccessByType(@PathVariable String accessType) {
        try {
            log.info("Getting user access by type: {}", accessType);
            List<UserAccessDto> accesses = userAccessService.getUserAccessByType(accessType);
            return ResponseEntity.ok(accesses);
        } catch (Exception e) {
            log.error("Error getting user access by type: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene accesos activos
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserAccessDto>> getActiveUserAccess() {
        try {
            log.info("Getting active user access");
            List<UserAccessDto> accesses = userAccessService.getActiveUserAccess();
            return ResponseEntity.ok(accesses);
        } catch (Exception e) {
            log.error("Error getting active user access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verifica si un usuario tiene acceso activo a un tipo específico
     */
    @GetMapping("/check/{userId}/{accessType}")
    public ResponseEntity<?> checkUserAccess(@PathVariable String userId, @PathVariable String accessType) {
        try {
            log.info("Checking user access for user: {} - Type: {}", userId, accessType);
            boolean hasAccess = userAccessService.hasActiveAccess(userId, accessType);
            
            return ResponseEntity.ok().body("{\"hasAccess\": " + hasAccess + "}");
        } catch (Exception e) {
            log.error("Error checking user access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene estadísticas de accesos
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getUserAccessStats() {
        try {
            log.info("Getting user access statistics");
            Object stats = userAccessService.getUserAccessStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting user access stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
