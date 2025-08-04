package com.disrupton.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Controlador simplificado para conocimiento de avatares
 * Esta versión simplificada no implementa funcionalidad completa ya que se usará Gemini API
 */
@RestController
@RequestMapping("/api/v1/knowledge")
@Slf4j
@CrossOrigin(origins = "*")
public class AvatarKnowledgeController {
    
    /**
     * Endpoint informativo que indica que esta funcionalidad está desactivada
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "message", "Funcionalidad de conocimiento de avatares implementada a través de Gemini API. Use la aplicación frontend para interactuar con los avatares predefinidos."
        ));
    }
}
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error interno al agregar conocimiento al avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "AVATAR_KNOWLEDGE_ERROR",
                "message", "Error interno al agregar conocimiento al avatar"
            ));
        }
    }
    
    /**
     * Obtiene un elemento de conocimiento por su ID
     */
    @GetMapping("/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> getKnowledgeById(@PathVariable @NotBlank String knowledgeId) {
        try {
            log.info("📄 GET /api/v1/knowledge/{} - Obteniendo conocimiento", knowledgeId);
            
            AvatarKnowledgeDto knowledge = knowledgeService.getKnowledgeById(knowledgeId);
            
            if (knowledge == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "KNOWLEDGE_NOT_FOUND",
                    "message", "Conocimiento no encontrado"
                ));
            }
            
            // Incrementar contador de accesos
            knowledgeService.incrementAccessCount(knowledgeId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", knowledge
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conocimiento {}: {}", knowledgeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener el conocimiento"
            ));
        }
    }
    
    /**
     * Actualiza un elemento de conocimiento existente
     */
    @PutMapping("/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> updateKnowledge(
            @PathVariable @NotBlank String knowledgeId,
            @Valid @RequestBody AvatarKnowledgeDto knowledgeData) {
        try {
            log.info("✏️ PUT /api/v1/knowledge/{} - Actualizando conocimiento", knowledgeId);
            
            knowledgeData.setKnowledgeId(knowledgeId);
            AvatarKnowledgeDto updatedKnowledge = knowledgeService.updateKnowledge(knowledgeData);
            
            if (updatedKnowledge == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "KNOWLEDGE_NOT_FOUND",
                    "message", "Conocimiento no encontrado para actualizar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conocimiento actualizado exitosamente",
                "data", updatedKnowledge
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al actualizar conocimiento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar conocimiento {}: {}", knowledgeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "UPDATE_ERROR",
                "message", "Error interno al actualizar el conocimiento"
            ));
        }
    }
    
    /**
     * Elimina un elemento de conocimiento
     */
    @DeleteMapping("/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> deleteKnowledge(@PathVariable @NotBlank String knowledgeId) {
        try {
            log.info("🗑️ DELETE /api/v1/knowledge/{} - Eliminando conocimiento", knowledgeId);
            
            boolean deleted = knowledgeService.deleteKnowledge(knowledgeId);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "KNOWLEDGE_NOT_FOUND",
                    "message", "Conocimiento no encontrado para eliminar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conocimiento eliminado exitosamente"
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al eliminar conocimiento {}: {}", knowledgeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "DELETION_ERROR",
                "message", "Error interno al eliminar el conocimiento"
            ));
        }
    }
    
    // ===== OPERACIONES DE CONSULTA Y BÚSQUEDA =====
    
    /**
     * Obtiene todo el conocimiento de un avatar
     */
    @GetMapping("/avatar/{avatarId}")
    public ResponseEntity<Map<String, Object>> getKnowledgeByAvatar(
            @PathVariable @NotBlank String avatarId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        try {
            log.info("🎭 GET /api/v1/knowledge/avatar/{} - Obteniendo conocimiento del avatar", avatarId);
            
            List<AvatarKnowledgeDto> knowledge = knowledgeService.getKnowledgeByAvatarId(avatarId);
            
            // Implementar paginación simple
            int start = page * size;
            int end = Math.min(start + size, knowledge.size());
            
            List<AvatarKnowledgeDto> paginatedKnowledge = knowledge.subList(
                Math.min(start, knowledge.size()), 
                Math.min(end, knowledge.size())
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedKnowledge,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", knowledge.size(),
                    "totalPages", (int) Math.ceil((double) knowledge.size() / size)
                ),
                "avatarId", avatarId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conocimiento del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conocimiento del avatar"
            ));
        }
    }
    
    /**
     * Busca conocimiento por palabras clave
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(
            @RequestParam @NotBlank String query,
            @RequestParam(required = false) String avatarId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region) {
        try {
            log.info("🔍 GET /api/v1/knowledge/search - Query: {}, Avatar: {}, Categoría: {}, Región: {}", 
                    query, avatarId, category, region);
            
            // Convertir query en keywords
            String[] queryWords = query.toLowerCase().split("\\s+");
            List<String> keywords = Arrays.asList(queryWords);
            
            List<AvatarKnowledgeDto> searchResults = knowledgeService.searchKnowledgeByKeywords(keywords);
            
            // Filtrar por avatar si se especifica
            if (avatarId != null) {
                searchResults = searchResults.stream()
                        .filter(knowledge -> avatarId.equals(knowledge.getAvatarId()))
                        .collect(Collectors.toList());
            }
            
            // Filtrar por categoría si se especifica
            if (category != null) {
                searchResults = searchResults.stream()
                        .filter(knowledge -> category.equals(knowledge.getKnowledgeCategory()))
                        .collect(Collectors.toList());
            }
            
            // Filtrar por región si se especifica
            if (region != null) {
                searchResults = searchResults.stream()
                        .filter(knowledge -> region.equals(knowledge.getCulturalRegion()))
                        .collect(Collectors.toList());
            }
            
            // Ordenar por relevancia
            searchResults = searchResults.stream()
                    .sorted((a, b) -> Double.compare(
                            b.getRelevanceScore() != null ? b.getRelevanceScore() : 0.0,
                            a.getRelevanceScore() != null ? a.getRelevanceScore() : 0.0
                    ))
                    .limit(50)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", searchResults,
                "count", searchResults.size(),
                "searchParams", Map.of(
                    "query", query,
                    "avatarId", avatarId != null ? avatarId : "",
                    "category", category != null ? category : "",
                    "region", region != null ? region : ""
                )
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error en búsqueda de conocimiento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SEARCH_ERROR",
                "message", "Error interno en la búsqueda"
            ));
        }
    }
    
    /**
     * Busca conocimiento relevante para una consulta específica
     */
    @GetMapping("/relevant")
    public ResponseEntity<Map<String, Object>> findRelevantKnowledge(
            @RequestParam @NotBlank String avatarId,
            @RequestParam @NotBlank String query,
            @RequestParam(required = false) String culturalContext) {
        try {
            log.info("🎯 GET /api/v1/knowledge/relevant - Avatar: {}, Query: {}, Contexto: {}", 
                    avatarId, query, culturalContext);
            
            List<AvatarKnowledgeDto> relevantKnowledge = avatarCulturalService.findRelevantKnowledge(
                    avatarId, query, culturalContext);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", relevantKnowledge,
                "count", relevantKnowledge.size(),
                "avatarId", avatarId,
                "query", query
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al buscar conocimiento relevante: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RELEVANT_SEARCH_ERROR",
                "message", "Error interno al buscar conocimiento relevante"
            ));
        }
    }
    
    /**
     * Obtiene conocimiento por categoría
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getKnowledgeByCategory(@PathVariable @NotBlank String category) {
        try {
            log.info("📚 GET /api/v1/knowledge/category/{} - Obteniendo conocimiento por categoría", category);
            
            List<AvatarKnowledgeDto> knowledge = knowledgeService.getKnowledgeByCategory(category);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", knowledge,
                "count", knowledge.size(),
                "category", category
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conocimiento por categoría {}: {}", category, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conocimiento por categoría"
            ));
        }
    }
    
    /**
     * Obtiene conocimiento por región cultural
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<Map<String, Object>> getKnowledgeByRegion(@PathVariable @NotBlank String region) {
        try {
            log.info("🌍 GET /api/v1/knowledge/region/{} - Obteniendo conocimiento por región", region);
            
            List<AvatarKnowledgeDto> knowledge = knowledgeService.getKnowledgeByRegion(region);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", knowledge,
                "count", knowledge.size(),
                "region", region
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conocimiento por región {}: {}", region, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conocimiento por región"
            ));
        }
    }
    
    /**
     * Obtiene conocimiento más popular
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularKnowledge(
            @RequestParam(defaultValue = "10") @PositiveOrZero int limit) {
        try {
            log.info("⭐ GET /api/v1/knowledge/popular - Obteniendo conocimiento popular (límite: {})", limit);
            
            List<AvatarKnowledgeDto> popularKnowledge = knowledgeService.getPopularKnowledge(limit);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", popularKnowledge,
                "count", popularKnowledge.size(),
                "limit", limit
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conocimiento popular: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conocimiento popular"
            ));
        }
    }
    
    /**
     * Obtiene conocimiento reciente
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentKnowledge(
            @RequestParam(defaultValue = "20") @PositiveOrZero int limit) {
        try {
            log.info("🆕 GET /api/v1/knowledge/recent - Obteniendo conocimiento reciente (límite: {})", limit);
            
            List<AvatarKnowledgeDto> recentKnowledge = knowledgeService.getRecentKnowledge(limit);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", recentKnowledge,
                "count", recentKnowledge.size(),
                "limit", limit
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conocimiento reciente: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conocimiento reciente"
            ));
        }
    }
    
    // ===== OPERACIONES DE GESTIÓN Y MODERACIÓN =====
    
    /**
     * Actualiza el estado de verificación de un conocimiento
     */
    @PutMapping("/{knowledgeId}/verification")
    public ResponseEntity<Map<String, Object>> updateVerificationStatus(
            @PathVariable @NotBlank String knowledgeId,
            @RequestParam @NotBlank String status,
            @RequestParam(required = false) String verifiedBy) {
        try {
            log.info("✅ PUT /api/v1/knowledge/{}/verification - Actualizando verificación: {}", knowledgeId, status);
            
            // Validar estado de verificación
            if (!Arrays.asList("PENDING", "VERIFIED", "REJECTED").contains(status)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_STATUS",
                    "message", "Estado de verificación inválido. Debe ser: PENDING, VERIFIED o REJECTED"
                ));
            }
            
            boolean updated = knowledgeService.updateVerificationStatus(knowledgeId, status, verifiedBy);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "KNOWLEDGE_NOT_FOUND",
                    "message", "Conocimiento no encontrado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estado de verificación actualizado exitosamente",
                "knowledgeId", knowledgeId,
                "verificationStatus", status,
                "verifiedBy", verifiedBy
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar verificación del conocimiento {}: {}", knowledgeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "VERIFICATION_ERROR",
                "message", "Error interno al actualizar verificación"
            ));
        }
    }
    
    /**
     * Incrementa el contador de accesos de un conocimiento
     */
    @POST("/{knowledgeId}/access")
    public ResponseEntity<Map<String, Object>> incrementAccessCount(@PathVariable @NotBlank String knowledgeId) {
        try {
            log.info("🔄 POST /api/v1/knowledge/{}/access - Incrementando contador de accesos", knowledgeId);
            
            boolean updated = knowledgeService.incrementAccessCount(knowledgeId);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "KNOWLEDGE_NOT_FOUND",
                    "message", "Conocimiento no encontrado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Acceso registrado exitosamente",
                "knowledgeId", knowledgeId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al incrementar accesos del conocimiento {}: {}", knowledgeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "ACCESS_COUNT_ERROR",
                "message", "Error interno al registrar acceso"
            ));
        }
    }
    
    // ===== OPERACIONES DE ANÁLISIS Y ESTADÍSTICAS =====
    
    /**
     * Obtiene estadísticas de conocimiento
     */
    @GetMapping("/analytics/stats")
    public ResponseEntity<Map<String, Object>> getKnowledgeStats() {
        try {
            log.info("📊 GET /api/v1/knowledge/analytics/stats - Obteniendo estadísticas de conocimiento");
            
            Map<String, Object> stats = knowledgeService.getKnowledgeStatistics();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener estadísticas de conocimiento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "STATS_ERROR",
                "message", "Error interno al obtener estadísticas"
            ));
        }
    }
    
    /**
     * Obtiene distribución de conocimiento por categorías
     */
    @GetMapping("/analytics/distribution")
    public ResponseEntity<Map<String, Object>> getKnowledgeDistribution() {
        try {
            log.info("📈 GET /api/v1/knowledge/analytics/distribution - Obteniendo distribución de conocimiento");
            
            Map<String, Long> categoryDistribution = knowledgeService.getCategoryDistribution();
            Map<String, Long> regionDistribution = knowledgeService.getRegionDistribution();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "categoryDistribution", categoryDistribution,
                    "regionDistribution", regionDistribution
                )
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener distribución de conocimiento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "DISTRIBUTION_ERROR",
                "message", "Error interno al obtener distribución"
            ));
        }
    }
}
