package com.disrupton.controller;

import com.disrupton.service.GeminiAvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Controlador simplificado para conversaciones con avatares usando Gemini API
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AvatarConversationController {
    
    private final GeminiAvatarService geminiAvatarService;
    
    /**
     * Endpoint principal para enviar mensajes a los avatares
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam @NotBlank String avatarId,
            @RequestParam @NotBlank String userId,
            @RequestParam @NotBlank String message) {
        try {
            log.info("💬 POST /api/v1/conversations - Avatar: {}, Usuario: {}", avatarId, userId);
            
            Map<String, Object> response = geminiAvatarService.processUserMessage(avatarId, userId, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Mensaje procesado exitosamente",
                "data", response
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error interno: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "PROCESSING_ERROR",
                "message", "Error interno al procesar el mensaje"
            ));
        }
    }
    
    /**
     * Endpoint informativo
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "message", "Servicio de conversación con avatares usando Gemini API",
            "endpoints", Map.of(
                "POST /", "Enviar mensaje a un avatar",
                "parameters", "avatarId, userId, message"
            ),
            "availableAvatars", new String[]{"VICUNA", "PERUVIAN_DOG", "COCK_OF_THE_ROCK"}
        ));
    }
}
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error interno al procesar mensaje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "MESSAGE_PROCESSING_ERROR",
                "message", "Error interno al procesar mensaje"
            ));
        }
    }
    
    /**
     * Finaliza una conversación
     */
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endConversation(
            @RequestParam @NotBlank String sessionId,
            @RequestParam(required = false) String endReason,
            @RequestParam(required = false) Double userSatisfaction,
            @RequestParam(required = false) String feedback) {
        try {
            log.info("🔚 POST /api/v1/conversations/end - Sesión: {}, Satisfacción: {}", sessionId, userSatisfaction);
            
            avatarCulturalService.endConversation(sessionId, endReason, userSatisfaction, feedback);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conversación finalizada exitosamente",
                "sessionId", sessionId
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error al finalizar conversación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error interno al finalizar conversación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SESSION_END_ERROR",
                "message", "Error interno al finalizar conversación"
            ));
        }
    }
    
    // ===== OPERACIONES CRUD DE CONVERSACIONES =====
    
    /**
     * Guarda una nueva conversación/mensaje
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveConversation(@Valid @RequestBody AvatarConversationDto conversationData) {
        try {
            log.info("💾 POST /api/v1/conversations - Guardando conversación");
            
            AvatarConversationDto savedConversation = conversationService.saveConversation(conversationData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Conversación guardada exitosamente",
                "data", savedConversation,
                "conversationId", savedConversation.getConversationId()
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al guardar conversación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al guardar conversación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SAVE_ERROR",
                "message", "Error interno al guardar conversación"
            ));
        }
    }
    
    /**
     * Obtiene una conversación por ID
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<Map<String, Object>> getConversationById(@PathVariable @NotBlank String conversationId) {
        try {
            log.info("📄 GET /api/v1/conversations/{} - Obteniendo conversación", conversationId);
            
            AvatarConversationDto conversation = conversationService.getConversationById(conversationId);
            
            if (conversation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "CONVERSATION_NOT_FOUND",
                    "message", "Conversación no encontrada"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conversation
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conversación {}: {}", conversationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conversación"
            ));
        }
    }
    
    /**
     * Actualiza una conversación existente
     */
    @PutMapping("/{conversationId}")
    public ResponseEntity<Map<String, Object>> updateConversation(
            @PathVariable @NotBlank String conversationId,
            @Valid @RequestBody AvatarConversationDto conversationData) {
        try {
            log.info("✏️ PUT /api/v1/conversations/{} - Actualizando conversación", conversationId);
            
            conversationData.setConversationId(conversationId);
            AvatarConversationDto updatedConversation = conversationService.updateConversation(conversationData);
            
            if (updatedConversation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "CONVERSATION_NOT_FOUND",
                    "message", "Conversación no encontrada para actualizar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conversación actualizada exitosamente",
                "data", updatedConversation
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al actualizar conversación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar conversación {}: {}", conversationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "UPDATE_ERROR",
                "message", "Error interno al actualizar conversación"
            ));
        }
    }
    
    /**
     * Elimina una conversación
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(@PathVariable @NotBlank String conversationId) {
        try {
            log.info("🗑️ DELETE /api/v1/conversations/{} - Eliminando conversación", conversationId);
            
            boolean deleted = conversationService.deleteConversation(conversationId);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "CONVERSATION_NOT_FOUND",
                    "message", "Conversación no encontrada para eliminar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conversación eliminada exitosamente"
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al eliminar conversación {}: {}", conversationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "DELETION_ERROR",
                "message", "Error interno al eliminar conversación"
            ));
        }
    }
    
    // ===== OPERACIONES DE CONSULTA Y BÚSQUEDA =====
    
    /**
     * Obtiene todas las conversaciones de una sesión
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getConversationsBySession(@PathVariable @NotBlank String sessionId) {
        try {
            log.info("📋 GET /api/v1/conversations/session/{} - Obteniendo conversaciones de sesión", sessionId);
            
            List<AvatarConversationDto> conversations = conversationService.getConversationsBySessionId(sessionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conversations,
                "count", conversations.size(),
                "sessionId", sessionId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conversaciones de sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conversaciones"
            ));
        }
    }
    
    /**
     * Obtiene conversaciones de un avatar específico
     */
    @GetMapping("/avatar/{avatarId}")
    public ResponseEntity<Map<String, Object>> getConversationsByAvatar(
            @PathVariable @NotBlank String avatarId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        try {
            log.info("🎭 GET /api/v1/conversations/avatar/{} - Obteniendo conversaciones del avatar", avatarId);
            
            List<AvatarConversationDto> conversations = conversationService.getConversationsByAvatarId(avatarId);
            
            // Implementar paginación simple
            int start = page * size;
            int end = Math.min(start + size, conversations.size());
            
            List<AvatarConversationDto> paginatedConversations = conversations.subList(
                Math.min(start, conversations.size()), 
                Math.min(end, conversations.size())
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedConversations,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", conversations.size(),
                    "totalPages", (int) Math.ceil((double) conversations.size() / size)
                ),
                "avatarId", avatarId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conversaciones del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conversaciones del avatar"
            ));
        }
    }
    
    /**
     * Obtiene conversaciones de un usuario específico
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getConversationsByUser(
            @PathVariable @NotBlank String userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        try {
            log.info("👤 GET /api/v1/conversations/user/{} - Obteniendo conversaciones del usuario", userId);
            
            List<AvatarConversationDto> conversations = conversationService.getConversationsByUserId(userId);
            
            // Implementar paginación simple
            int start = page * size;
            int end = Math.min(start + size, conversations.size());
            
            List<AvatarConversationDto> paginatedConversations = conversations.subList(
                Math.min(start, conversations.size()), 
                Math.min(end, conversations.size())
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedConversations,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", conversations.size(),
                    "totalPages", (int) Math.ceil((double) conversations.size() / size)
                ),
                "userId", userId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conversaciones del usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conversaciones del usuario"
            ));
        }
    }
    
    /**
     * Busca conversaciones por contenido
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchConversations(
            @RequestParam @NotBlank String query,
            @RequestParam(required = false) String avatarId,
            @RequestParam(required = false) String userId) {
        try {
            log.info("🔍 GET /api/v1/conversations/search - Query: {}, Avatar: {}, Usuario: {}", 
                    query, avatarId, userId);
            
            List<AvatarConversationDto> searchResults = conversationService.searchConversations(query);
            
            // Filtrar por avatar si se especifica
            if (avatarId != null) {
                searchResults = searchResults.stream()
                        .filter(conv -> avatarId.equals(conv.getAvatarId()))
                        .toList();
            }
            
            // Filtrar por usuario si se especifica
            if (userId != null) {
                searchResults = searchResults.stream()
                        .filter(conv -> userId.equals(conv.getUserId()))
                        .toList();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", searchResults,
                "count", searchResults.size(),
                "searchParams", Map.of(
                    "query", query,
                    "avatarId", avatarId != null ? avatarId : "",
                    "userId", userId != null ? userId : ""
                )
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error en búsqueda de conversaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SEARCH_ERROR",
                "message", "Error interno en la búsqueda"
            ));
        }
    }
    
    /**
     * Obtiene conversaciones por tema cultural
     */
    @GetMapping("/by-topic/{topic}")
    public ResponseEntity<Map<String, Object>> getConversationsByTopic(@PathVariable @NotBlank String topic) {
        try {
            log.info("🏛️ GET /api/v1/conversations/by-topic/{} - Obteniendo conversaciones por tema", topic);
            
            List<AvatarConversationDto> conversations = conversationService.getConversationsByTopic(topic);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", conversations,
                "count", conversations.size(),
                "topic", topic
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener conversaciones por tema {}: {}", topic, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener conversaciones por tema"
            ));
        }
    }
    
    // ===== OPERACIONES DE ANÁLISIS Y ESTADÍSTICAS =====
    
    /**
     * Obtiene analytics de conversaciones para un avatar
     */
    @GetMapping("/analytics/avatar/{avatarId}")
    public ResponseEntity<Map<String, Object>> getConversationAnalytics(
            @PathVariable @NotBlank String avatarId,
            @RequestParam(defaultValue = "30d") String period) {
        try {
            log.info("📊 GET /api/v1/conversations/analytics/avatar/{} - Analytics (período: {})", avatarId, period);
            
            List<AvatarConversationDto> conversations = conversationService.getConversationAnalytics(avatarId, period);
            
            // Calcular estadísticas
            long totalMessages = conversations.size();
            long userMessages = conversations.stream()
                    .filter(conv -> "USER_MESSAGE".equals(conv.getMessageType()))
                    .count();
            long avatarResponses = conversations.stream()
                    .filter(conv -> "AVATAR_RESPONSE".equals(conv.getMessageType()))
                    .count();
            
            Map<String, Long> topicFrequency = conversations.stream()
                    .filter(conv -> conv.getCulturalTopic() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            AvatarConversationDto::getCulturalTopic,
                            java.util.stream.Collectors.counting()
                    ));
            
            Map<String, Long> responseTypeFrequency = conversations.stream()
                    .filter(conv -> conv.getResponseType() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                            AvatarConversationDto::getResponseType,
                            java.util.stream.Collectors.counting()
                    ));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalMessages", totalMessages,
                    "userMessages", userMessages,
                    "avatarResponses", avatarResponses,
                    "topicFrequency", topicFrequency,
                    "responseTypeFrequency", responseTypeFrequency
                ),
                "avatarId", avatarId,
                "period", period
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener analytics de conversaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "ANALYTICS_ERROR",
                "message", "Error interno al obtener analytics"
            ));
        }
    }
    
    /**
     * Obtiene estadísticas generales de conversaciones
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getConversationSummary() {
        try {
            log.info("📈 GET /api/v1/conversations/analytics/summary - Obteniendo resumen de conversaciones");
            
            Map<String, Object> summary = conversationService.getConversationSummary();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", summary
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener resumen de conversaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SUMMARY_ERROR",
                "message", "Error interno al obtener resumen"
            ));
        }
    }
}
