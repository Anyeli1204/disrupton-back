package com.disrupton.culturalAgent.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cultural-agent")
@RequiredArgsConstructor
@Slf4j
@RequireRole({UserRole.AGENTE_CULTURAL, UserRole.ADMIN}) // Accessible by AGENTE_CULTURAL or ADMIN
public class CulturalAgentController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCulturalAgentDashboard() {
        log.info("🏛️ Accediendo al dashboard de agente cultural");
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard de Agente Cultural");
        dashboard.put("culturalAgent", true);
        dashboard.put("features", new String[]{
            "Gestión de eventos culturales",
            "Programación cultural",
            "Coordinación de artistas",
            "Promoción cultural",
            "Gestión de espacios culturales"
        });
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> createCulturalEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String location,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam int expectedAttendance) {
        log.info("🏛️ Creando evento cultural: {}", title);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Evento cultural creado exitosamente");
        response.put("eventId", "evt_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("category", category);
        response.put("location", location);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("expectedAttendance", expectedAttendance);
        response.put("createdBy", "cultural_agent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getMyEvents() {
        log.info("🏛️ Obteniendo eventos del agente cultural");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Eventos del agente cultural");
        response.put("events", new Object[]{
            Map.of("id", "evt_1", "title", "Festival de Arte Tradicional", "category", "Festival", "status", "active", "attendance", 150),
            Map.of("id", "evt_2", "title", "Exposición de Fotografía", "category", "Exposición", "status", "upcoming", "attendance", 80),
            Map.of("id", "evt_3", "title", "Concierto de Música Folclórica", "category", "Concierto", "status", "completed", "attendance", 200)
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/artists")
    public ResponseEntity<Map<String, Object>> registerArtist(
            @RequestParam String name,
            @RequestParam String specialty,
            @RequestParam String contactInfo,
            @RequestParam String portfolio) {
        log.info("🏛️ Registrando artista: {}", name);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Artista registrado exitosamente");
        response.put("artistId", "art_" + System.currentTimeMillis());
        response.put("name", name);
        response.put("specialty", specialty);
        response.put("contactInfo", contactInfo);
        response.put("portfolio", portfolio);
        response.put("registeredBy", "cultural_agent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/artists")
    public ResponseEntity<Map<String, Object>> getRegisteredArtists() {
        log.info("🏛️ Obteniendo artistas registrados");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Artistas registrados");
        response.put("artists", new Object[]{
            Map.of("id", "art_1", "name", "María González", "specialty", "Pintura", "status", "active"),
            Map.of("id", "art_2", "name", "Carlos Rodríguez", "specialty", "Escultura", "status", "active"),
            Map.of("id", "art_3", "name", "Ana Martínez", "specialty", "Danza", "status", "inactive")
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/spaces")
    public ResponseEntity<Map<String, Object>> manageCulturalSpace(
            @RequestParam String name,
            @RequestParam String type,
            @RequestParam String location,
            @RequestParam int capacity,
            @RequestParam String facilities) {
        log.info("🏛️ Gestionando espacio cultural: {}", name);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Espacio cultural gestionado exitosamente");
        response.put("spaceId", "spc_" + System.currentTimeMillis());
        response.put("name", name);
        response.put("type", type);
        response.put("location", location);
        response.put("capacity", capacity);
        response.put("facilities", facilities);
        response.put("managedBy", "cultural_agent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spaces")
    public ResponseEntity<Map<String, Object>> getManagedSpaces() {
        log.info("🏛️ Obteniendo espacios gestionados");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Espacios culturales gestionados");
        response.put("spaces", new Object[]{
            Map.of("id", "spc_1", "name", "Centro Cultural Municipal", "type", "Centro Cultural", "capacity", 500, "status", "active"),
            Map.of("id", "spc_2", "name", "Teatro Comunitario", "type", "Teatro", "capacity", 200, "status", "active"),
            Map.of("id", "spc_3", "name", "Galería de Arte", "type", "Galería", "capacity", 100, "status", "maintenance")
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/programs")
    public ResponseEntity<Map<String, Object>> createCulturalProgram(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String targetAudience,
            @RequestParam String duration,
            @RequestParam double budget) {
        log.info("🏛️ Creando programa cultural: {}", title);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Programa cultural creado exitosamente");
        response.put("programId", "prg_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("targetAudience", targetAudience);
        response.put("duration", duration);
        response.put("budget", budget);
        response.put("createdBy", "cultural_agent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/programs")
    public ResponseEntity<Map<String, Object>> getCulturalPrograms() {
        log.info("🏛️ Obteniendo programas culturales");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Programas culturales");
        response.put("programs", new Object[]{
            Map.of("id", "prg_1", "title", "Arte en las Escuelas", "targetAudience", "Estudiantes", "status", "active"),
            Map.of("id", "prg_2", "title", "Cultura para Adultos Mayores", "targetAudience", "Adultos Mayores", "status", "active"),
            Map.of("id", "prg_3", "title", "Festival Intercultural", "targetAudience", "Público General", "status", "planning")
        });
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promotion")
    public ResponseEntity<Map<String, Object>> getPromotionStats() {
        log.info("🏛️ Obteniendo estadísticas de promoción");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas de promoción cultural");
        response.put("totalEvents", 25);
        response.put("totalAttendance", 3500);
        response.put("socialMediaReach", 15000);
        response.put("mediaCoverage", 8);
        response.put("partnerships", 12);
        response.put("topEvents", new Object[]{
            Map.of("event", "Festival de Arte Tradicional", "attendance", 500, "reach", 3000),
            Map.of("event", "Exposición de Fotografía", "attendance", 200, "reach", 1500)
        });
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCulturalAgentStats() {
        log.info("🏛️ Obteniendo estadísticas del agente cultural");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas del agente cultural");
        response.put("totalEvents", 25);
        response.put("activeArtists", 15);
        response.put("managedSpaces", 5);
        response.put("culturalPrograms", 8);
        response.put("totalAttendance", 3500);
        response.put("averageRating", 4.6);
        response.put("categories", new String[]{"Festival", "Exposición", "Concierto", "Teatro", "Danza", "Literatura"});
        return ResponseEntity.ok(response);
    }
}
