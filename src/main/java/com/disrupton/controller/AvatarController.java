package com.disrupton.controller;

import com.disrupton.dto.AvatarDto;
import com.disrupton.service.AvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Controlador REST para la gestión de Avatares
 * Proporciona endpoints para consultar los 3 tipos de avatares disponibles
 */
@RestController
@RequestMapping("/api/v1/avatars")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
public class AvatarController {
    
    private final AvatarService avatarService;
    
    /**
     * Obtiene todos los avatares disponibles (Vicuña, Perro Peruano, Gallito de las Rocas)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAvatars() {
        try {
            log.info("📋 GET /api/v1/avatars - Obteniendo todos los avatares disponibles");
            
            List<AvatarDto> avatars = avatarService.getAllAvatars();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Avatares obtenidos exitosamente",
                "data", avatars,
                "count", avatars.size()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatares: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener los avatares"
            ));
        }
    }
    
    /**
     * Obtiene un avatar por su ID
     */
    @GetMapping("/{avatarId}")
    public ResponseEntity<Map<String, Object>> getAvatarById(@PathVariable @NotBlank String avatarId) {
        try {
            log.info("👤 GET /api/v1/avatars/{} - Obteniendo avatar", avatarId);
            
            AvatarDto avatar = avatarService.getAvatarById(avatarId);
            
            if (avatar == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "AVATAR_NOT_FOUND",
                    "message", "Avatar no encontrado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", avatar
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener el avatar"
            ));
        }
    }
}
    
    /**
     * Actualiza un avatar existente
     */
    @PutMapping("/{avatarId}")
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @PathVariable @NotBlank String avatarId,
            @Valid @RequestBody AvatarDto avatarData) {
        try {
            log.info("✏️ PUT /api/v1/avatars/{} - Actualizando avatar", avatarId);
            
            // Establecer el ID del avatar
            avatarData.setAvatarId(avatarId);
            
            AvatarDto updatedAvatar = firebaseAvatarService.updateAvatar(avatarData);
            
            if (updatedAvatar == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "AVATAR_NOT_FOUND",
                    "message", "Avatar no encontrado para actualizar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Avatar actualizado exitosamente",
                "data", updatedAvatar
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al actualizar avatar: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "UPDATE_ERROR",
                "message", "Error interno al actualizar el avatar"
            ));
        }
    }
    
    /**
     * Elimina un avatar (desactivación lógica)
     */
    @DeleteMapping("/{avatarId}")
    public ResponseEntity<Map<String, Object>> deleteAvatar(@PathVariable @NotBlank String avatarId) {
        try {
            log.info("🗑️ DELETE /api/v1/avatars/{} - Eliminando avatar", avatarId);
            
            boolean deleted = firebaseAvatarService.deleteAvatar(avatarId);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "AVATAR_NOT_FOUND",
                    "message", "Avatar no encontrado para eliminar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Avatar eliminado exitosamente"
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al eliminar avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "DELETION_ERROR",
                "message", "Error interno al eliminar el avatar"
            ));
        }
    }
    
    // ===== OPERACIONES DE CONSULTA Y BÚSQUEDA =====
    
    /**
     * Obtiene todos los avatares activos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAvatars(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        try {
            log.info("📋 GET /api/v1/avatars - Obteniendo todos los avatares (página: {}, tamaño: {})", page, size);
            
            List<AvatarDto> avatars = firebaseAvatarService.getActiveAvatars();
            
            // Implementar paginación simple
            int start = page * size;
            int end = Math.min(start + size, avatars.size());
            
            List<AvatarDto> paginatedAvatars = avatars.subList(
                Math.min(start, avatars.size()), 
                Math.min(end, avatars.size())
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedAvatars,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", avatars.size(),
                    "totalPages", (int) Math.ceil((double) avatars.size() / size)
                )
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatares: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener los avatares"
            ));
        }
    }
    
    /**
     * Busca avatares usando búsqueda inteligente
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchAvatars(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String expertise) {
        try {
            log.info("🔍 GET /api/v1/avatars/search - Búsqueda: query={}, region={}, language={}, expertise={}", 
                    query, region, language, expertise);
            
            List<AvatarDto> searchResults = avatarCulturalService.smartSearchAvatars(query, region, language, expertise);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", searchResults,
                "count", searchResults.size(),
                "searchParams", Map.of(
                    "query", query != null ? query : "",
                    "region", region != null ? region : "",
                    "language", language != null ? language : "",
                    "expertise", expertise != null ? expertise : ""
                )
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error en búsqueda de avatares: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SEARCH_ERROR",
                "message", "Error interno en la búsqueda"
            ));
        }
    }
    
    /**
     * Obtiene avatares recomendados para un usuario
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @RequestParam @NotBlank String userId,
            @RequestParam(required = false) String campusZone,
            @RequestParam(required = false) String culturalInterest) {
        try {
            log.info("🎯 GET /api/v1/avatars/recommendations - Usuario: {}, zona: {}, interés: {}", 
                    userId, campusZone, culturalInterest);
            
            List<AvatarDto> recommendations = avatarCulturalService.getRecommendedAvatars(userId, campusZone, culturalInterest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", recommendations,
                "count", recommendations.size(),
                "userId", userId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener recomendaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RECOMMENDATION_ERROR",
                "message", "Error interno al obtener recomendaciones"
            ));
        }
    }
    
    /**
     * Obtiene avatares por región cultural
     */
    @GetMapping("/by-region/{region}")
    public ResponseEntity<Map<String, Object>> getAvatarsByRegion(@PathVariable @NotBlank String region) {
        try {
            log.info("🌍 GET /api/v1/avatars/by-region/{} - Obteniendo avatares por región", region);
            
            List<AvatarDto> avatars = firebaseAvatarService.getAvatarsByRegion(region);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", avatars,
                "count", avatars.size(),
                "region", region
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatares por región {}: {}", region, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener avatares por región"
            ));
        }
    }
    
    /**
     * Obtiene avatares por área de conocimiento
     */
    @GetMapping("/by-knowledge/{knowledgeArea}")
    public ResponseEntity<Map<String, Object>> getAvatarsByKnowledge(@PathVariable @NotBlank String knowledgeArea) {
        try {
            log.info("📚 GET /api/v1/avatars/by-knowledge/{} - Obteniendo avatares por área de conocimiento", knowledgeArea);
            
            List<AvatarDto> avatars = firebaseAvatarService.getAvatarsByKnowledgeArea(knowledgeArea);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", avatars,
                "count", avatars.size(),
                "knowledgeArea", knowledgeArea
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatares por conocimiento {}: {}", knowledgeArea, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener avatares por conocimiento"
            ));
        }
    }
    
    /**
     * Obtiene avatares por zona del campus
     */
    @GetMapping("/by-campus-zone/{campusZone}")
    public ResponseEntity<Map<String, Object>> getAvatarsByCampusZone(@PathVariable @NotBlank String campusZone) {
        try {
            log.info("🏛️ GET /api/v1/avatars/by-campus-zone/{} - Obteniendo avatares por zona del campus", campusZone);
            
            List<AvatarDto> avatars = firebaseAvatarService.getAvatarsByCampusZone(campusZone);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", avatars,
                "count", avatars.size(),
                "campusZone", campusZone
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatares por zona del campus {}: {}", campusZone, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener avatares por zona del campus"
            ));
        }
    }
    
    /**
     * Obtiene avatares más populares
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularAvatars(
            @RequestParam(defaultValue = "10") @PositiveOrZero int limit) {
        try {
            log.info("⭐ GET /api/v1/avatars/popular - Obteniendo avatares populares (límite: {})", limit);
            
            List<AvatarDto> popularAvatars = firebaseAvatarService.getPopularAvatars(limit);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", popularAvatars,
                "count", popularAvatars.size(),
                "limit", limit
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener avatares populares: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener avatares populares"
            ));
        }
    }
    
    // ===== OPERACIONES DE ESTADÍSTICAS Y ANALYTICS =====
    
    /**
     * Obtiene analytics detallados de un avatar
     */
    @GetMapping("/{avatarId}/analytics")
    public ResponseEntity<Map<String, Object>> getAvatarAnalytics(
            @PathVariable @NotBlank String avatarId,
            @RequestParam(defaultValue = "30d") String period) {
        try {
            log.info("📊 GET /api/v1/avatars/{}/analytics - Obteniendo analytics (período: {})", avatarId, period);
            
            Map<String, Object> analytics = avatarCulturalService.getAvatarAnalytics(avatarId, period);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", analytics,
                "avatarId", avatarId,
                "period", period
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Avatar no encontrado para analytics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", "AVATAR_NOT_FOUND",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener analytics del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "ANALYTICS_ERROR",
                "message", "Error interno al obtener analytics"
            ));
        }
    }
    
    /**
     * Actualiza el rating de un avatar
     */
    @POST("/{avatarId}/rating")
    public ResponseEntity<Map<String, Object>> updateAvatarRating(
            @PathVariable @NotBlank String avatarId,
            @RequestParam Double rating) {
        try {
            log.info("⭐ POST /api/v1/avatars/{}/rating - Actualizando rating: {}", avatarId, rating);
            
            if (rating < 1.0 || rating > 5.0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_RATING",
                    "message", "El rating debe estar entre 1.0 y 5.0"
                ));
            }
            
            boolean updated = firebaseAvatarService.updateAvatarRating(avatarId, rating);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "AVATAR_NOT_FOUND",
                    "message", "Avatar no encontrado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Rating actualizado exitosamente",
                "avatarId", avatarId,
                "newRating", rating
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar rating del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RATING_UPDATE_ERROR",
                "message", "Error interno al actualizar rating"
            ));
        }
    }
    
    /**
     * Incrementa el contador de interacciones de un avatar
     */
    @POST("/{avatarId}/interaction")
    public ResponseEntity<Map<String, Object>> incrementInteractions(@PathVariable @NotBlank String avatarId) {
        try {
            log.info("🔄 POST /api/v1/avatars/{}/interaction - Incrementando interacciones", avatarId);
            
            boolean updated = firebaseAvatarService.incrementAvatarInteractions(avatarId);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "AVATAR_NOT_FOUND",
                    "message", "Avatar no encontrado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Interacción registrada exitosamente",
                "avatarId", avatarId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al incrementar interacciones del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "INTERACTION_ERROR",
                "message", "Error interno al registrar interacción"
            ));
        }
    }
    
    /**
     * Obtiene resumen general del sistema de avatares
     */
    @GetMapping("/system/summary")
    public ResponseEntity<Map<String, Object>> getSystemSummary() {
        try {
            log.info("📈 GET /api/v1/avatars/system/summary - Obteniendo resumen del sistema");
            
            Map<String, Object> summary = avatarCulturalService.getSystemSummary();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", summary
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener resumen del sistema: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SYSTEM_SUMMARY_ERROR",
                "message", "Error interno al obtener resumen del sistema"
            ));
        }
    }
}
