package com.disrupton.moderator.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
@Slf4j
@RequireRole({UserRole.MODERATOR, UserRole.ADMIN})
public class ModeratorController {

    /**
     * Dashboard de moderación
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getModeratorDashboard() {
        log.info("🛡️ Accediendo al dashboard de moderación");
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard de Moderación");
        dashboard.put("timestamp", System.currentTimeMillis());
        dashboard.put("moderator", true);
        dashboard.put("functions", new String[]{
            "Moderar comentarios",
            "Revisar contenido reportado",
            "Gestionar usuarios problemáticos",
            "Aprobar contenido cultural"
        });
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtener contenido pendiente de moderación
     */
    @GetMapping("/pending-content")
    public ResponseEntity<Map<String, Object>> getPendingContent() {
        log.info("📋 Moderador solicitando contenido pendiente");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido pendiente de moderación");
        response.put("pendingComments", 5);
        response.put("pendingCulturalObjects", 2);
        response.put("reportedContent", 3);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Aprobar contenido
     */
    @PostMapping("/approve/{contentId}")
    public ResponseEntity<Map<String, Object>> approveContent(
            @PathVariable String contentId,
            @RequestParam String contentType) {
        
        log.info("✅ Moderador aprobando contenido: {} de tipo {}", contentId, contentType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido aprobado exitosamente");
        response.put("contentId", contentId);
        response.put("contentType", contentType);
        response.put("approvedBy", "moderator");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Rechazar contenido
     */
    @PostMapping("/reject/{contentId}")
    public ResponseEntity<Map<String, Object>> rejectContent(
            @PathVariable String contentId,
            @RequestParam String contentType,
            @RequestParam String reason) {
        
        log.info("❌ Moderador rechazando contenido: {} de tipo {} - Razón: {}", 
                contentId, contentType, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido rechazado");
        response.put("contentId", contentId);
        response.put("contentType", contentType);
        response.put("reason", reason);
        response.put("rejectedBy", "moderator");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener contenido reportado
     */
    @GetMapping("/reported")
    public ResponseEntity<Map<String, Object>> getReportedContent() {
        log.info("🚨 Moderador solicitando contenido reportado");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido reportado");
        response.put("reportedComments", 3);
        response.put("reportedCulturalObjects", 1);
        response.put("reportedUsers", 2);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Advertir usuario
     */
    @PostMapping("/warn-user/{userId}")
    public ResponseEntity<Map<String, Object>> warnUser(
            @PathVariable String userId,
            @RequestParam String reason) {
        
        log.info("⚠️ Moderador advirtiendo usuario: {} - Razón: {}", userId, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario advertido");
        response.put("userId", userId);
        response.put("reason", reason);
        response.put("warnedBy", "moderator");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
