package com.disrupton.artisan.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/artisan")
@RequiredArgsConstructor
@Slf4j
@RequireRole({UserRole.ARTISAN, UserRole.ADMIN}) // Accessible by ARTISAN or ADMIN
public class ArtisanController {

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getArtisanDashboard() {
        log.info("🎨 Accediendo al dashboard de artesano");
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard de Artesano");
        dashboard.put("artisan", true);
        dashboard.put("features", new String[]{
            "Gestión de obras artesanales",
            "Exposiciones virtuales",
            "Técnicas tradicionales",
            "Ventas de artesanías",
            "Talleres y cursos"
        });
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/artworks")
    public ResponseEntity<Map<String, Object>> createArtwork(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String technique,
            @RequestParam String category) {
        log.info("🎨 Creando obra artesanal: {}", title);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Obra artesanal creada exitosamente");
        response.put("artworkId", "art_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("technique", technique);
        response.put("category", category);
        response.put("createdBy", "artisan");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/artworks")
    public ResponseEntity<Map<String, Object>> getMyArtworks() {
        log.info("🎨 Obteniendo obras del artesano");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Obras del artesano");
        response.put("artworks", new Object[]{
            Map.of("id", "art_1", "title", "Vasija Tradicional", "technique", "Cerámica", "status", "active"),
            Map.of("id", "art_2", "title", "Tejido Ancestral", "technique", "Textil", "status", "active"),
            Map.of("id", "art_3", "title", "Escultura en Madera", "technique", "Tallado", "status", "draft")
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exhibitions")
    public ResponseEntity<Map<String, Object>> createExhibition(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("🎨 Creando exposición: {}", title);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Exposición creada exitosamente");
        response.put("exhibitionId", "exh_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("location", location);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("createdBy", "artisan");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exhibitions")
    public ResponseEntity<Map<String, Object>> getMyExhibitions() {
        log.info("🎨 Obteniendo exposiciones del artesano");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Exposiciones del artesano");
        response.put("exhibitions", new Object[]{
            Map.of("id", "exh_1", "title", "Artesanías Tradicionales", "location", "Centro Cultural", "status", "active"),
            Map.of("id", "exh_2", "title", "Técnicas Ancestrales", "location", "Museo Local", "status", "upcoming")
        });
        return ResponseEntity.ok(response);
    }

    @PostMapping("/workshops")
    public ResponseEntity<Map<String, Object>> createWorkshop(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String technique,
            @RequestParam int maxParticipants,
            @RequestParam double price) {
        log.info("🎨 Creando taller: {}", title);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Taller creado exitosamente");
        response.put("workshopId", "ws_" + System.currentTimeMillis());
        response.put("title", title);
        response.put("technique", technique);
        response.put("maxParticipants", maxParticipants);
        response.put("price", price);
        response.put("createdBy", "artisan");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workshops")
    public ResponseEntity<Map<String, Object>> getMyWorkshops() {
        log.info("🎨 Obteniendo talleres del artesano");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Talleres del artesano");
        response.put("workshops", new Object[]{
            Map.of("id", "ws_1", "title", "Introducción a la Cerámica", "technique", "Cerámica", "participants", 8, "maxParticipants", 12),
            Map.of("id", "ws_2", "title", "Tejido Tradicional", "technique", "Textil", "participants", 15, "maxParticipants", 15)
        });
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesStats() {
        log.info("🎨 Obteniendo estadísticas de ventas");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas de ventas");
        response.put("totalSales", 12500.0);
        response.put("totalOrders", 45);
        response.put("averageOrderValue", 277.78);
        response.put("topSellingItems", new Object[]{
            Map.of("item", "Vasija Tradicional", "sales", 12, "revenue", 3600.0),
            Map.of("item", "Tejido Ancestral", "sales", 8, "revenue", 2400.0)
        });
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getArtisanStats() {
        log.info("🎨 Obteniendo estadísticas del artesano");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Estadísticas del artesano");
        response.put("totalArtworks", 15);
        response.put("activeExhibitions", 2);
        response.put("completedWorkshops", 8);
        response.put("totalStudents", 120);
        response.put("averageRating", 4.8);
        response.put("techniques", new String[]{"Cerámica", "Textil", "Tallado", "Metalurgia"});
        return ResponseEntity.ok(response);
    }
}
