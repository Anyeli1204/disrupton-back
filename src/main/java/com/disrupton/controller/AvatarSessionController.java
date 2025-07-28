package com.disrupton.controller;

import com.disrupton.dto.AvatarSessionDto;
import com.disrupton.service.FirebaseAvatarSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de Sesiones de Avatar
 * Maneja el ciclo de vida completo de las sesiones de interacción
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
public class AvatarSessionController {
    
    private final FirebaseAvatarSessionService sessionService;
    
    // ===== OPERACIONES CRUD DE SESIONES =====
    
    /**
     * Crea una nueva sesión de avatar
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(@Valid @RequestBody AvatarSessionDto sessionData) {
        try {
            log.info("🚀 POST /api/v1/sessions - Creando nueva sesión para avatar: {}", sessionData.getAvatarId());
            
            AvatarSessionDto createdSession = sessionService.createSession(sessionData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Sesión creada exitosamente",
                "data", createdSession,
                "sessionId", createdSession.getSessionId()
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al crear sesión: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al crear sesión: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "CREATION_ERROR",
                "message", "Error interno al crear la sesión"
            ));
        }
    }
    
    /**
     * Obtiene una sesión por su ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionById(@PathVariable @NotBlank String sessionId) {
        try {
            log.info("📄 GET /api/v1/sessions/{} - Obteniendo sesión", sessionId);
            
            AvatarSessionDto session = sessionService.getSessionById(sessionId);
            
            if (session == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", session
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener la sesión"
            ));
        }
    }
    
    /**
     * Actualiza una sesión existente
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateSession(
            @PathVariable @NotBlank String sessionId,
            @Valid @RequestBody AvatarSessionDto sessionData) {
        try {
            log.info("✏️ PUT /api/v1/sessions/{} - Actualizando sesión", sessionId);
            
            sessionData.setSessionId(sessionId);
            AvatarSessionDto updatedSession = sessionService.updateSession(sessionData);
            
            if (updatedSession == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada para actualizar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Sesión actualizada exitosamente",
                "data", updatedSession
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al actualizar sesión: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", e.getMessage()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "UPDATE_ERROR",
                "message", "Error interno al actualizar la sesión"
            ));
        }
    }
    
    /**
     * Elimina una sesión
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable @NotBlank String sessionId) {
        try {
            log.info("🗑️ DELETE /api/v1/sessions/{} - Eliminando sesión", sessionId);
            
            boolean deleted = sessionService.deleteSession(sessionId);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada para eliminar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Sesión eliminada exitosamente"
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al eliminar sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "DELETION_ERROR",
                "message", "Error interno al eliminar la sesión"
            ));
        }
    }
    
    // ===== OPERACIONES DE GESTIÓN DE SESIONES =====
    
    /**
     * Finaliza una sesión activa
     */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endSession(
            @PathVariable @NotBlank String sessionId,
            @RequestParam(required = false) String endReason) {
        try {
            log.info("🔚 POST /api/v1/sessions/{}/end - Finalizando sesión", sessionId);
            
            boolean ended = sessionService.endSession(sessionId, endReason);
            
            if (!ended) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada para finalizar"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Sesión finalizada exitosamente",
                "sessionId", sessionId,
                "endReason", endReason != null ? endReason : "USER_REQUEST"
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al finalizar sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "END_SESSION_ERROR",
                "message", "Error interno al finalizar la sesión"
            ));
        }
    }
    
    /**
     * Actualiza estadísticas de una sesión
     */
    @PutMapping("/{sessionId}/stats")
    public ResponseEntity<Map<String, Object>> updateSessionStats(
            @PathVariable @NotBlank String sessionId,
            @RequestParam int messageCount,
            @RequestParam int questionCount,
            @RequestParam int errorCount) {
        try {
            log.info("📊 PUT /api/v1/sessions/{}/stats - Actualizando estadísticas", sessionId);
            
            boolean updated = sessionService.updateSessionStats(sessionId, messageCount, questionCount, errorCount);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada para actualizar estadísticas"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estadísticas actualizadas exitosamente",
                "sessionId", sessionId,
                "stats", Map.of(
                    "messageCount", messageCount,
                    "questionCount", questionCount,
                    "errorCount", errorCount
                )
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar estadísticas de sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "STATS_UPDATE_ERROR",
                "message", "Error interno al actualizar estadísticas"
            ));
        }
    }
    
    /**
     * Actualiza satisfacción del usuario para una sesión
     */
    @PutMapping("/{sessionId}/satisfaction")
    public ResponseEntity<Map<String, Object>> updateSessionSatisfaction(
            @PathVariable @NotBlank String sessionId,
            @RequestParam Double satisfaction,
            @RequestParam(required = false) String feedback) {
        try {
            log.info("⭐ PUT /api/v1/sessions/{}/satisfaction - Actualizando satisfacción: {}", sessionId, satisfaction);
            
            if (satisfaction < 1.0 || satisfaction > 5.0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "INVALID_SATISFACTION",
                    "message", "La satisfacción debe estar entre 1.0 y 5.0"
                ));
            }
            
            boolean updated = sessionService.updateSessionSatisfaction(sessionId, satisfaction, feedback);
            
            if (!updated) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada para actualizar satisfacción"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Satisfacción actualizada exitosamente",
                "sessionId", sessionId,
                "satisfaction", satisfaction,
                "feedback", feedback
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al actualizar satisfacción de sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "SATISFACTION_UPDATE_ERROR",
                "message", "Error interno al actualizar satisfacción"
            ));
        }
    }
    
    // ===== OPERACIONES DE CONSULTA Y BÚSQUEDA =====
    
    /**
     * Obtiene todas las sesiones activas
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        try {
            log.info("🔄 GET /api/v1/sessions/active - Obteniendo sesiones activas");
            
            List<AvatarSessionDto> activeSessions = sessionService.getActiveSessions();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", activeSessions,
                "count", activeSessions.size()
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener sesiones activas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener sesiones activas"
            ));
        }
    }
    
    /**
     * Obtiene sesiones por avatar
     */
    @GetMapping("/avatar/{avatarId}")
    public ResponseEntity<Map<String, Object>> getSessionsByAvatar(
            @PathVariable @NotBlank String avatarId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        try {
            log.info("🎭 GET /api/v1/sessions/avatar/{} - Obteniendo sesiones del avatar", avatarId);
            
            List<AvatarSessionDto> sessions = sessionService.getSessionsByAvatarId(avatarId);
            
            // Implementar paginación simple
            int start = page * size;
            int end = Math.min(start + size, sessions.size());
            
            List<AvatarSessionDto> paginatedSessions = sessions.subList(
                Math.min(start, sessions.size()), 
                Math.min(end, sessions.size())
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedSessions,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", sessions.size(),
                    "totalPages", (int) Math.ceil((double) sessions.size() / size)
                ),
                "avatarId", avatarId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener sesiones del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener sesiones del avatar"
            ));
        }
    }
    
    /**
     * Obtiene sesiones por usuario
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSessionsByUser(
            @PathVariable @NotBlank String userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        try {
            log.info("👤 GET /api/v1/sessions/user/{} - Obteniendo sesiones del usuario", userId);
            
            List<AvatarSessionDto> sessions = sessionService.getSessionsByUserId(userId);
            
            // Implementar paginación simple
            int start = page * size;
            int end = Math.min(start + size, sessions.size());
            
            List<AvatarSessionDto> paginatedSessions = sessions.subList(
                Math.min(start, sessions.size()), 
                Math.min(end, sessions.size())
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedSessions,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", sessions.size(),
                    "totalPages", (int) Math.ceil((double) sessions.size() / size)
                ),
                "userId", userId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener sesiones del usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener sesiones del usuario"
            ));
        }
    }
    
    /**
     * Obtiene sesiones por período de tiempo
     */
    @GetMapping("/period/{period}")
    public ResponseEntity<Map<String, Object>> getSessionsByPeriod(@PathVariable @NotBlank String period) {
        try {
            log.info("📅 GET /api/v1/sessions/period/{} - Obteniendo sesiones por período", period);
            
            List<AvatarSessionDto> sessions = sessionService.getSessionsByPeriod(period);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", sessions,
                "count", sessions.size(),
                "period", period
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener sesiones por período {}: {}", period, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener sesiones por período"
            ));
        }
    }
    
    /**
     * Obtiene sesiones por zona del campus
     */
    @GetMapping("/campus-zone/{campusZone}")
    public ResponseEntity<Map<String, Object>> getSessionsByCampusZone(@PathVariable @NotBlank String campusZone) {
        try {
            log.info("🏛️ GET /api/v1/sessions/campus-zone/{} - Obteniendo sesiones por zona del campus", campusZone);
            
            List<AvatarSessionDto> sessions = sessionService.getSessionsByCampusZone(campusZone);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", sessions,
                "count", sessions.size(),
                "campusZone", campusZone
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener sesiones por zona del campus {}: {}", campusZone, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "RETRIEVAL_ERROR",
                "message", "Error interno al obtener sesiones por zona del campus"
            ));
        }
    }
    
    // ===== OPERACIONES DE ANÁLISIS Y ESTADÍSTICAS =====
    
    /**
     * Obtiene estadísticas de sesiones
     */
    @GetMapping("/analytics/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats(
            @RequestParam(defaultValue = "30d") String period) {
        try {
            log.info("📊 GET /api/v1/sessions/analytics/stats - Obteniendo estadísticas (período: {})", period);
            
            List<AvatarSessionDto> sessions = sessionService.getSessionsByPeriod(period);
            
            // Calcular estadísticas
            long totalSessions = sessions.size();
            long completedSessions = sessions.stream()
                    .filter(session -> Boolean.TRUE.equals(session.getSessionCompleted()))
                    .count();
            
            double completionRate = totalSessions > 0 ? (double) completedSessions / totalSessions * 100 : 0.0;
            
            double averageDuration = sessions.stream()
                    .filter(session -> session.getDurationSeconds() != null)
                    .mapToLong(AvatarSessionDto::getDurationSeconds)
                    .average()
                    .orElse(0.0);
            
            double averageSatisfaction = sessions.stream()
                    .filter(session -> session.getUserSatisfactionScore() != null)
                    .mapToDouble(AvatarSessionDto::getUserSatisfactionScore)
                    .average()
                    .orElse(0.0);
            
            Map<String, Long> deviceTypeDistribution = sessions.stream()
                    .filter(session -> session.getDeviceType() != null)
                    .collect(Collectors.groupingBy(
                            AvatarSessionDto::getDeviceType,
                            Collectors.counting()
                    ));
            
            Map<String, Long> campusZoneDistribution = sessions.stream()
                    .filter(session -> session.getCampusZone() != null)
                    .collect(Collectors.groupingBy(
                            AvatarSessionDto::getCampusZone,
                            Collectors.counting()
                    ));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalSessions", totalSessions,
                    "completedSessions", completedSessions,
                    "completionRate", completionRate,
                    "averageDurationSeconds", averageDuration,
                    "averageSatisfaction", averageSatisfaction,
                    "deviceTypeDistribution", deviceTypeDistribution,
                    "campusZoneDistribution", campusZoneDistribution
                ),
                "period", period
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener estadísticas de sesiones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "STATS_ERROR",
                "message", "Error interno al obtener estadísticas"
            ));
        }
    }
    
    /**
     * Obtiene métricas detalladas de una sesión específica
     */
    @GetMapping("/{sessionId}/metrics")
    public ResponseEntity<Map<String, Object>> getSessionMetrics(@PathVariable @NotBlank String sessionId) {
        try {
            log.info("📈 GET /api/v1/sessions/{}/metrics - Obteniendo métricas de sesión", sessionId);
            
            AvatarSessionDto session = sessionService.getSessionById(sessionId);
            
            if (session == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", "SESSION_NOT_FOUND",
                    "message", "Sesión no encontrada"
                ));
            }
            
            // Calcular métricas adicionales
            double engagementScore = calculateEngagementScore(session);
            String qualityLevel = determineQualityLevel(session);
            Map<String, Object> performanceMetrics = calculatePerformanceMetrics(session);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "sessionInfo", session,
                    "engagementScore", engagementScore,
                    "qualityLevel", qualityLevel,
                    "performanceMetrics", performanceMetrics
                ),
                "sessionId", sessionId
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener métricas de sesión {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "METRICS_ERROR",
                "message", "Error interno al obtener métricas"
            ));
        }
    }
    
    /**
     * Obtiene resumen de rendimiento por avatar
     */
    @GetMapping("/analytics/performance/avatar/{avatarId}")
    public ResponseEntity<Map<String, Object>> getAvatarPerformance(
            @PathVariable @NotBlank String avatarId,
            @RequestParam(defaultValue = "30d") String period) {
        try {
            log.info("🎭 GET /api/v1/sessions/analytics/performance/avatar/{} - Rendimiento del avatar", avatarId);
            
            List<AvatarSessionDto> avatarSessions = sessionService.getSessionsByAvatarId(avatarId);
            
            // Filtrar por período si es necesario
            // (aquí se podría implementar filtrado por fechas)
            
            // Calcular métricas de rendimiento
            long totalSessions = avatarSessions.size();
            double averageSatisfaction = avatarSessions.stream()
                    .filter(session -> session.getUserSatisfactionScore() != null)
                    .mapToDouble(AvatarSessionDto::getUserSatisfactionScore)
                    .average()
                    .orElse(0.0);
            
            double averageDuration = avatarSessions.stream()
                    .filter(session -> session.getDurationSeconds() != null)
                    .mapToLong(AvatarSessionDto::getDurationSeconds)
                    .average()
                    .orElse(0.0);
            
            long successfulSessions = avatarSessions.stream()
                    .filter(session -> Boolean.TRUE.equals(session.getSessionCompleted()) && 
                                     session.getUserSatisfactionScore() != null && 
                                     session.getUserSatisfactionScore() >= 4.0)
                    .count();
            
            double successRate = totalSessions > 0 ? (double) successfulSessions / totalSessions * 100 : 0.0;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "avatarId", avatarId,
                    "totalSessions", totalSessions,
                    "averageSatisfaction", averageSatisfaction,
                    "averageDurationSeconds", averageDuration,
                    "successfulSessions", successfulSessions,
                    "successRate", successRate
                ),
                "period", period
            ));
            
        } catch (ExecutionException | InterruptedException e) {
            log.error("❌ Error al obtener rendimiento del avatar {}: {}", avatarId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "PERFORMANCE_ERROR",
                "message", "Error interno al obtener rendimiento del avatar"
            ));
        }
    }
    
    // ===== MÉTODOS AUXILIARES PRIVADOS =====
    
    private double calculateEngagementScore(AvatarSessionDto session) {
        double score = 0.0;
        
        // Factor de duración (30% del score)
        if (session.getDurationSeconds() != null) {
            double durationMinutes = session.getDurationSeconds() / 60.0;
            if (durationMinutes >= 2) score += 30;
            else score += (durationMinutes / 2.0) * 30;
        }
        
        // Factor de mensajes (40% del score)
        if (session.getMessageCount() != null) {
            if (session.getMessageCount() >= 5) score += 40;
            else score += (session.getMessageCount() / 5.0) * 40;
        }
        
        // Factor de satisfacción (30% del score)
        if (session.getUserSatisfactionScore() != null) {
            score += (session.getUserSatisfactionScore() / 5.0) * 30;
        }
        
        return Math.min(100.0, score);
    }
    
    private String determineQualityLevel(AvatarSessionDto session) {
        if (session.getUserSatisfactionScore() == null) return "UNKNOWN";
        
        double satisfaction = session.getUserSatisfactionScore();
        if (satisfaction >= 4.5) return "EXCELLENT";
        if (satisfaction >= 4.0) return "GOOD";
        if (satisfaction >= 3.0) return "AVERAGE";
        if (satisfaction >= 2.0) return "POOR";
        return "VERY_POOR";
    }
    
    private Map<String, Object> calculatePerformanceMetrics(AvatarSessionDto session) {
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        // Métricas de interacción
        metrics.put("interactionDensity", session.getMessageCount() != null && session.getDurationSeconds() != null
                ? (double) session.getMessageCount() / (session.getDurationSeconds() / 60.0) : 0.0);
        
        // Métricas de calidad
        metrics.put("errorRate", session.getMessageCount() != null && session.getMessageCount() > 0 && session.getErrorCount() != null
                ? (double) session.getErrorCount() / session.getMessageCount() * 100 : 0.0);
        
        // Métricas de compromiso
        metrics.put("engagementLevel", calculateEngagementScore(session));
        
        return metrics;
    }
}
