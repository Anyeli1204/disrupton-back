package com.disrupton.store.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.store.dto.TourismServiceDto;
import com.disrupton.store.model.TourismService;
import com.disrupton.store.service.TourismServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
     * Obtener servicios destacados
     */
    @GetMapping("/destacados")
    public ResponseEntity<Map<String, Object>> getFeaturedServices() {
        try {
            log.info("⭐ Obteniendo servicios destacados");

            List<TourismServiceDto> services = tourismServiceService.getFeaturedServices();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al obtener servicios destacados: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener servicios destacados");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Crear un nuevo servicio turístico (solo GUIDE)
     */
    @PostMapping("/crear")
    @RequireRole({"GUIDE"})
    public ResponseEntity<Map<String, Object>> createService(
            @Valid @RequestBody TourismServiceDto serviceRequest,
            Authentication authentication) {
        try {
            String guideId = authentication.getName();
            log.info("🗺️ Creando servicio para guía: {}", guideId);

            TourismServiceDto createdService = tourismServiceService.createService(guideId, serviceRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdService);
            response.put("message", "Servicio creado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Límite excedido: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", "QUOTA_EXCEEDED");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Datos inválidos: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error creando servicio: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error interno al crear servicio");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Actualizar un servicio existente (solo el guía dueño)
     */
    @PutMapping("/{serviceId}")
    @RequireRole({"GUIDE"})
    public ResponseEntity<Map<String, Object>> updateService(
            @PathVariable String serviceId,
            @Valid @RequestBody TourismServiceDto serviceRequest,
            Authentication authentication) {
        try {
            String guideId = authentication.getName();
            log.info("🔄 Actualizando servicio {} por guía: {}", serviceId, guideId);

            TourismServiceDto updatedService = tourismServiceService.updateService(serviceId, guideId, serviceRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedService);
            response.put("message", "Servicio actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de autorización/datos: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error actualizando servicio: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error interno al actualizar servicio");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Eliminar un servicio (solo el guía dueño)
     */
    @DeleteMapping("/{serviceId}")
    @RequireRole({"GUIDE"})
    public ResponseEntity<Map<String, Object>> deleteService(
            @PathVariable String serviceId,
            Authentication authentication) {
        try {
            String guideId = authentication.getName();
            log.info("🗑️ Eliminando servicio {} por guía: {}", serviceId, guideId);

            boolean deleted = tourismServiceService.deleteService(serviceId, guideId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Servicio eliminado exitosamente" : "No se pudo eliminar el servicio");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de autorización: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error eliminando servicio: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error interno al eliminar servicio");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener mis servicios (solo para guías)
     */
    @GetMapping("/mis-servicios")
    @RequireRole({"GUIDE"})
    public ResponseEntity<Map<String, Object>> getMyServices(Authentication authentication) {
        try {
            String guideId = authentication.getName();
            log.info("🧭 Obteniendo servicios del guía: {}", guideId);

            List<TourismServiceDto> services = tourismServiceService.getServicesByGuide(guideId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", services);
            response.put("count", services.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error obteniendo mis servicios: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener tus servicios");

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
