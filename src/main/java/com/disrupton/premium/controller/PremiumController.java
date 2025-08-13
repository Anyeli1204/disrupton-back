package com.disrupton.premium.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
@Slf4j
@RequireRole({UserRole.PREMIUM, UserRole.ADMIN})
public class PremiumController {

    /**
     * Dashboard premium
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getPremiumDashboard() {
        log.info("⭐ Accediendo al dashboard premium");
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard Premium");
        dashboard.put("timestamp", System.currentTimeMillis());
        dashboard.put("premium", true);
        dashboard.put("functions", new String[]{
            "Acceso a tours exclusivos",
            "Contenido cultural premium",
            "Reservas prioritarias",
            "Soporte VIP",
            "Experiencias AR avanzadas",
            "Descargas ilimitadas"
        });
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtener tours exclusivos
     */
    @GetMapping("/exclusive-tours")
    public ResponseEntity<Map<String, Object>> getExclusiveTours() {
        log.info("⭐ Usuario premium solicitando tours exclusivos");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tours exclusivos para usuarios premium");
        response.put("totalTours", 8);
        response.put("tours", new Object[]{
            Map.of("id", "exclusive_1", "title", "Tour VIP Palacio de Gobierno", "duration", "3 horas", "price", "150.00"),
            Map.of("id", "exclusive_2", "title", "Experiencia Gastronómica Privada", "duration", "4 horas", "price", "200.00"),
            Map.of("id", "exclusive_3", "title", "Tour Nocturno Especial", "duration", "2 horas", "price", "120.00"),
            Map.of("id", "exclusive_4", "title", "Acceso a Zonas Restringidas", "duration", "5 horas", "price", "300.00")
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Reservar tour exclusivo
     */
    @PostMapping("/book-exclusive/{tourId}")
    public ResponseEntity<Map<String, Object>> bookExclusiveTour(
            @PathVariable String tourId,
            @RequestParam String date,
            @RequestParam int visitors) {
        
        log.info("⭐ Usuario premium reservando tour exclusivo: {} para {} visitantes", tourId, visitors);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reserva premium confirmada");
        response.put("bookingId", "premium_booking_" + System.currentTimeMillis());
        response.put("tourId", tourId);
        response.put("date", date);
        response.put("visitors", visitors);
        response.put("priority", "VIP");
        response.put("confirmed", true);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener contenido cultural premium
     */
    @GetMapping("/cultural-content")
    public ResponseEntity<Map<String, Object>> getPremiumCulturalContent() {
        log.info("⭐ Usuario premium solicitando contenido cultural exclusivo");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contenido cultural premium");
        response.put("totalContent", 25);
        response.put("content", new Object[]{
            Map.of("id", "premium_1", "title", "Documental Exclusivo: Historia Secreta", "type", "video", "duration", "45 min"),
            Map.of("id", "premium_2", "title", "Galería de Fotos HD", "type", "gallery", "photos", 150),
            Map.of("id", "premium_3", "title", "Audio Tour Narrado por Expertos", "type", "audio", "duration", "60 min"),
            Map.of("id", "premium_4", "title", "Experiencia VR Inmersiva", "type", "vr", "duration", "30 min")
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Acceso a experiencias AR avanzadas
     */
    @GetMapping("/ar-experiences")
    public ResponseEntity<Map<String, Object>> getPremiumARExperiences() {
        log.info("⭐ Usuario premium solicitando experiencias AR avanzadas");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Experiencias AR premium");
        response.put("totalExperiences", 12);
        response.put("experiences", new Object[]{
            Map.of("id", "ar_1", "title", "Reconstrucción Histórica 3D", "type", "reconstruction", "interactive", true),
            Map.of("id", "ar_2", "title", "Guía Virtual Personalizada", "type", "guide", "ai_powered", true),
            Map.of("id", "ar_3", "title", "Experiencia Multisensorial", "type", "multisensory", "haptic", true),
            Map.of("id", "ar_4", "title", "Tour Holográfico", "type", "hologram", "realistic", true)
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Soporte VIP
     */
    @PostMapping("/vip-support")
    public ResponseEntity<Map<String, Object>> requestVIPSupport(
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam String priority) {
        
        log.info("⭐ Usuario premium solicitando soporte VIP: {} - Prioridad: {}", subject, priority);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Solicitud de soporte VIP recibida");
        response.put("ticketId", "vip_" + System.currentTimeMillis());
        response.put("subject", subject);
        response.put("priority", priority);
        response.put("estimatedResponse", "2 horas");
        response.put("assignedTo", "VIP Support Team");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Estadísticas premium del usuario
     */
    @GetMapping("/my-stats")
    public ResponseEntity<Map<String, Object>> getPremiumUserStats() {
        log.info("⭐ Usuario premium solicitando sus estadísticas");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas premium del usuario");
        response.put("premiumSince", "2024-12-01");
        response.put("totalExclusiveTours", 15);
        response.put("totalPremiumContent", 45);
        response.put("arExperiencesUsed", 23);
        response.put("vipSupportRequests", 3);
        response.put("savings", 450.00);
        response.put("premiumLevel", "Gold");
        response.put("nextReward", "Tour Privado Gratuito");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Descargar contenido premium
     */
    @PostMapping("/download/{contentId}")
    public ResponseEntity<Map<String, Object>> downloadPremiumContent(
            @PathVariable String contentId,
            @RequestParam String format) {
        
        log.info("⭐ Usuario premium descargando contenido: {} en formato {}", contentId, format);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Descarga premium iniciada");
        response.put("contentId", contentId);
        response.put("format", format);
        response.put("downloadUrl", "https://premium.disrupton.com/download/" + contentId + "." + format);
        response.put("expiresIn", "24 horas");
        response.put("unlimited", true);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
