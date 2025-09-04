package com.disrupton.store.controller;

import com.disrupton.store.dto.TourismServiceDto;
import com.disrupton.store.model.TourismService;
import com.disrupton.store.service.TourismServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para servicios turísticos
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tienda/servicios")
@CrossOrigin(origins = "*")
public class TourismServiceController {

    private final TourismServiceService tourismServiceService;

    /**
     * Obtener todos los servicios turísticos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllServices() {
        try {
            log.info("🗺️ Obteniendo todos los servicios turísticos");
            
            List<TourismServiceDto> services = tourismServiceService.getAllServices();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener servicios: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener servicios turísticos");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener servicios por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Map<String, Object>> getServicesByCategory(@PathVariable String categoria) {
        try {
            log.info("🏛️ Obteniendo servicios de categoría: {}", categoria);
            
            TourismService.ServiceCategory categoryEnum = TourismService.ServiceCategory.valueOf(categoria.toUpperCase());
            List<TourismServiceDto> services = tourismServiceService.getServicesByCategory(categoryEnum);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());
            response.put("categoria", categoria);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Categoría no válida: {}", categoria);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Categoría no válida: " + categoria);
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener servicios por categoría: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener servicios por categoría");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Buscar servicios turísticos
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> searchServices(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String dificultad,
            @RequestParam(required = false) Integer duracionMin,
            @RequestParam(required = false) Integer duracionMax) {
        try {
            log.info("🔍 Buscando servicios con término: '{}', precio: {}-{}, departamento: '{}', dificultad: '{}', duración: {}-{}", 
                    termino, precioMin, precioMax, departamento, dificultad, duracionMin, duracionMax);
            
            TourismService.DifficultyLevel difficultyEnum = null;
            if (dificultad != null) {
                try {
                    difficultyEnum = TourismService.DifficultyLevel.valueOf(dificultad.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("⚠️ Nivel de dificultad no válido: {}", dificultad);
                }
            }
            
            List<TourismServiceDto> services = tourismServiceService.searchServices(
                    termino, precioMin, precioMax, departamento, difficultyEnum, duracionMin, duracionMax);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());
            response.put("filtros", Map.of(
                "termino", termino != null ? termino : "",
                "precioMin", precioMin,
                "precioMax", precioMax,
                "departamento", departamento != null ? departamento : "",
                "dificultad", dificultad != null ? dificultad : "",
                "duracionMin", duracionMin,
                "duracionMax", duracionMax
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en búsqueda de servicios: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error en la búsqueda de servicios");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener servicio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getServiceById(@PathVariable String id) {
        try {
            log.info("🎯 Obteniendo servicio por ID: {}", id);
            
            TourismServiceDto service = tourismServiceService.getServiceById(id);
            
            // Incrementar contador de visualizaciones
            tourismServiceService.incrementViewCount(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", service);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("⚠️ Servicio no encontrado: {}", id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Servicio no encontrado");
            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("❌ Error al obtener servicio: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener servicio");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener servicios por guía
     */
    @GetMapping("/guia/{guiaId}")
    public ResponseEntity<Map<String, Object>> getServicesByGuide(@PathVariable String guiaId) {
        try {
            log.info("🧭 Obteniendo servicios del guía: {}", guiaId);
            
            List<TourismServiceDto> services = tourismServiceService.getServicesByGuide(guiaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());
            response.put("guiaId", guiaId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener servicios del guía: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener servicios del guía");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener estadísticas de servicios
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getServiceStats() {
        try {
            log.info("📊 Obteniendo estadísticas de servicios");
            
            TourismServiceService.ServiceStatsDto stats = tourismServiceService.getServiceStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener estadísticas: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener estadísticas de servicios");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener categorías disponibles
     */
    @GetMapping("/categorias")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            log.info("📂 Obteniendo categorías de servicios");
            
            Map<String, String> categories = new HashMap<>();
            for (TourismService.ServiceCategory category : TourismService.ServiceCategory.values()) {
                categories.put(category.name(), category.getDisplayName());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            response.put("count", categories.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener categorías: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener categorías");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener niveles de dificultad disponibles
     */
    @GetMapping("/dificultades")
    public ResponseEntity<Map<String, Object>> getDifficultyLevels() {
        try {
            log.info("⛰️ Obteniendo niveles de dificultad");
            
            Map<String, String> difficulties = new HashMap<>();
            for (TourismService.DifficultyLevel difficulty : TourismService.DifficultyLevel.values()) {
                difficulties.put(difficulty.name(), difficulty.getDisplayName());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", difficulties);
            response.put("count", difficulties.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener dificultades: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener niveles de dificultad");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
