package com.disrupton.avatar.controller;

import com.disrupton.avatar.dto.AvatarDto;
import com.disrupton.avatar.model.Avatar;
import com.disrupton.avatar.service.AvatarService;
import com.disrupton.avatar.service.GeminiAvatarService;
import com.disrupton.avatar.service.AvatarSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/avatars")
@CrossOrigin(origins = "*")
@Slf4j
public class AvatarController {

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private GeminiAvatarService geminiAvatarService;
    
    @Autowired
    private AvatarSessionService sessionService;

    /**
     * Obtener todos los avatares disponibles (solo 3 tipos)
     */
    @GetMapping
    public ResponseEntity<List<AvatarDto>> getAllAvatars() {
        try {
            List<Avatar> avatars = avatarService.getAllAvatars();
            List<AvatarDto> avatarDtos = avatars.stream()
                .map(this::convertToDto)
                .toList();
            return ResponseEntity.ok(avatarDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener avatar específico por ID
     */
    @GetMapping("/{avatarId}")
    public ResponseEntity<AvatarDto> getAvatarById(@PathVariable String avatarId) {
        try {
            Avatar avatar = avatarService.getAvatarById(avatarId);
            if (avatar != null) {
                return ResponseEntity.ok(convertToDto(avatar));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crear nuevo avatar
     */
    @PostMapping
    public ResponseEntity<AvatarDto> createAvatar(@RequestBody AvatarDto avatarDto) {
        try {
            Avatar avatar = convertToEntity(avatarDto);
            Avatar savedAvatar = avatarService.createAvatar(avatar);
            return ResponseEntity.ok(convertToDto(savedAvatar));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar avatar existente
     */
    @PutMapping("/{avatarId}")
    public ResponseEntity<AvatarDto> updateAvatar(@PathVariable String avatarId, @RequestBody AvatarDto avatarDto) {
        try {
            Avatar avatar = convertToEntity(avatarDto);
            avatar.setAvatarId(avatarId);
            Avatar updatedAvatar = avatarService.updateAvatar(avatar);
            return ResponseEntity.ok(convertToDto(updatedAvatar));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar avatar
     */
    @DeleteMapping("/{avatarId}")
    public ResponseEntity<Void> deleteAvatar(@PathVariable String avatarId) {
        try {
            avatarService.deleteAvatar(avatarId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Procesar mensaje del usuario con el avatar usando Gemini API
     */
    @PostMapping("/{avatarId}/chat")
    public ResponseEntity<Map<String, Object>> chatWithAvatar(
            @PathVariable String avatarId,
            @RequestBody Map<String, String> request) {
        
        try {
            String message = request.get("message");
            String userId = request.get("userId");
            String sessionId = request.get("sessionId");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "El mensaje no puede estar vacío"
                ));
            }

            Avatar avatar = avatarService.getAvatarById(avatarId);
            if (avatar == null) {
                return ResponseEntity.notFound().build();
            }

            // Procesar mensaje con Gemini AI
            String response = geminiAvatarService.processUserMessage(avatar, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "avatarId", avatarId,
                "avatarType", avatar.getType().toString(),
                "avatarName", avatar.getType().getDisplayName(),
                "userMessage", message,
                "response", response,
                "timestamp", System.currentTimeMillis(),
                "sessionId", sessionId != null ? sessionId : "anonymous"
            ));
            
        } catch (Exception e) {
            log.error("Error en chat con avatar: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtener historial de conversaciones con un avatar
     */
    @GetMapping("/{avatarId}/conversations")
    public ResponseEntity<Map<String, Object>> getConversationHistory(
            @PathVariable String avatarId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            Avatar avatar = avatarService.getAvatarById(avatarId);
            if (avatar == null) {
                return ResponseEntity.notFound().build();
            }

            var conversations = geminiAvatarService.getConversationHistory(userId, avatarId, limit);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "avatarId", avatarId,
                "avatarType", avatar.getType().toString(),
                "conversations", conversations,
                "totalCount", 0 // Simplified for now
            ));
            
        } catch (Exception e) {
            log.error("Error obteniendo historial: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error obteniendo historial: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtener estadísticas de uso de un avatar
     */
    @GetMapping("/{avatarId}/stats")
    public ResponseEntity<Map<String, Object>> getAvatarStats(@PathVariable String avatarId) {
        try {
            Avatar avatar = avatarService.getAvatarById(avatarId);
            if (avatar == null) {
                return ResponseEntity.notFound().build();
            }

            long usageCount = geminiAvatarService.getAvatarUsageCount(avatarId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "avatarId", avatarId,
                "avatarType", avatar.getType().toString(),
                "avatarName", avatar.getType().getDisplayName(),
                "totalConversations", usageCount,
                "isActive", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error obteniendo estadísticas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Crear nueva sesión de chat
     */
    @PostMapping("/{avatarId}/sessions")
    public ResponseEntity<Map<String, Object>> createSession(
            @PathVariable String avatarId,
            @RequestBody Map<String, String> request) {
        
        try {
            String userId = request.get("userId");
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "El userId es requerido"
                ));
            }

            Avatar avatar = avatarService.getAvatarById(avatarId);
            if (avatar == null) {
                return ResponseEntity.notFound().build();
            }

            String sessionId = sessionService.createSession(userId, avatarId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", sessionId,
                "avatarId", avatarId,
                "avatarType", avatar.getType().toString(),
                "avatarName", avatar.getType().getDisplayName(),
                "userId", userId,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error creando sesión: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Finalizar sesión de chat
     */
    @DeleteMapping("/{avatarId}/sessions")
    public ResponseEntity<Map<String, Object>> endSession(
            @PathVariable String avatarId,
            @RequestParam String userId) {
        
        try {
            sessionService.endSession(userId, avatarId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Sesión finalizada correctamente"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error finalizando sesión: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtener estadísticas de sesión
     */
    @GetMapping("/sessions/{sessionId}/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats(@PathVariable String sessionId) {
        try {
            Map<String, Object> stats = sessionService.getSessionStats(sessionId);
            stats.put("success", true);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error obteniendo estadísticas de sesión: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtener información básica de todos los tipos de avatares disponibles
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getAvatarTypes() {
        try {
            List<Map<String, Object>> types = List.of(
                Map.of(
                    "type", "VICUNA",
                    "name", "Vicuña Andina",
                    "description", "Representante de la fauna andina peruana, símbolo de elegancia y resistencia."
                ),
                Map.of(
                    "type", "PERUVIAN_DOG", 
                    "name", "Perro Peruano",
                    "description", "Guardián ancestral sin pelo, compañero leal de las culturas preincaicas."
                ),
                Map.of(
                    "type", "COCK_OF_THE_ROCK",
                    "name", "Gallito de las Rocas", 
                    "description", "Ave nacional del Perú, símbolo de belleza y orgullo patrio."
                )
            );
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convierte una entidad Avatar a DTO
     */
    private AvatarDto convertToDto(Avatar avatar) {
        AvatarDto dto = new AvatarDto();
        dto.setAvatarId(avatar.getAvatarId());
        dto.setType(avatar.getType().toString());
        dto.setDisplayName(avatar.getDisplayName());
        dto.setAvatar3DModelUrl(avatar.getAvatar3DModelUrl());
        dto.setCreatedAt(avatar.getCreatedAt());
        dto.setUpdatedAt(avatar.getUpdatedAt());
        return dto;
    }

    /**
     * Convierte un DTO a entidad Avatar
     */
    private Avatar convertToEntity(AvatarDto dto) {
        return Avatar.builder()
                .avatarId(dto.getAvatarId())
                .type(Avatar.AvatarType.valueOf(dto.getType()))
                .displayName(dto.getDisplayName())
                .avatar3DModelUrl(dto.getAvatar3DModelUrl())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
