package com.disrupton.culturalAgent.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import com.disrupton.culturalAgent.dto.AgenteCulturalDto;
import com.disrupton.culturalAgent.model.AgenteCultural;
import com.disrupton.culturalAgent.service.AgenteCulturalService;
import com.disrupton.culturalAgent.util.AgenteCulturalDataSeeder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agentes-culturales")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CulturalAgentController {

    private final AgenteCulturalService agenteCulturalService;
    private final AgenteCulturalDataSeeder dataSeeder;

    /**
     * Inicializa datos de prueba (solo para desarrollo)
     */
    @PostMapping("/init-data")
    @RequireRole({UserRole.ADMIN})
    public ResponseEntity<Map<String, Object>> inicializarDatos() {
        try {
            log.info("🌱 Inicializando datos de prueba para agentes culturales");
            
            dataSeeder.seedData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Datos de prueba inicializados correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al inicializar datos: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al inicializar datos de prueba");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene todos los agentes culturales
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodosLosAgentes() {
        try {
            log.info("📋 Obteniendo todos los agentes culturales");
            
            List<AgenteCulturalDto> artesanosDto = agenteCulturalService.obtenerAgentesPorTipo(AgenteCultural.AgentType.ARTISAN);
            List<AgenteCulturalDto> guiasDto = agenteCulturalService.obtenerAgentesPorTipo(AgenteCultural.AgentType.TOURIST_GUIDE);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("artesanos", artesanosDto);
            response.put("guias", guiasDto);
            response.put("total", artesanosDto.size() + guiasDto.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener agentes: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener agentes culturales");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene artesanos
     */
    @GetMapping("/artesanos")
    public ResponseEntity<Map<String, Object>> obtenerArtesanos() {
        try {
            log.info("🎨 Obteniendo artesanos");
            
            List<AgenteCulturalDto> artesanosDto = agenteCulturalService.obtenerAgentesPorTipo(AgenteCultural.AgentType.ARTISAN);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", artesanosDto);
            response.put("count", artesanosDto.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener artesanos: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener artesanos");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene guías turísticos
     */
    @GetMapping("/guias")
    public ResponseEntity<Map<String, Object>> obtenerGuias() {
        try {
            log.info("🗺️ Obteniendo guías turísticos");
            
            List<AgenteCulturalDto> guiasDto = agenteCulturalService.obtenerAgentesPorTipo(AgenteCultural.AgentType.TOURIST_GUIDE);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", guiasDto);
            response.put("count", guiasDto.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener guías: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener guías turísticos");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Busca agentes culturales por término
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarAgentes(
            @RequestParam String termino,
            @RequestParam(required = false) String tipo) {
        try {
            log.info("🔍 Buscando agentes con término: '{}', tipo: '{}'", termino, tipo);
            
            AgenteCultural.AgentType tipoEnum = null;
            if (tipo != null && !tipo.trim().isEmpty()) {
                try {
                    tipoEnum = AgenteCultural.AgentType.valueOf(tipo.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Tipo de agente no válido: {}", tipo);
                }
            }
            
            List<AgenteCulturalDto> agentesDto = agenteCulturalService.buscarAgentes(termino, tipoEnum);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", agentesDto);
            response.put("count", agentesDto.size());
            response.put("termino", termino);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en búsqueda: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error en la búsqueda de agentes");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene agentes por departamento/ubicación
     */
    @GetMapping("/ubicacion/{departamento}")
    public ResponseEntity<Map<String, Object>> obtenerPorUbicacion(@PathVariable String departamento) {
        try {
            log.info("📍 Obteniendo agentes en: {}", departamento);
            
            List<AgenteCulturalDto> agentesDto = agenteCulturalService.obtenerAgentesPorUbicacion(departamento);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", agentesDto);
            response.put("count", agentesDto.size());
            response.put("departamento", departamento);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener agentes por ubicación: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener agentes por ubicación");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene estadísticas generales
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            log.info("📊 Obteniendo estadísticas de agentes culturales");
            
            List<AgenteCulturalDto> artesanos = agenteCulturalService.obtenerAgentesPorTipo(AgenteCultural.AgentType.ARTISAN);
            List<AgenteCulturalDto> guias = agenteCulturalService.obtenerAgentesPorTipo(AgenteCultural.AgentType.TOURIST_GUIDE);
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalArtesanos", artesanos.size());
            estadisticas.put("totalGuias", guias.size());
            estadisticas.put("totalAgentes", artesanos.size() + guias.size());
            
            // Estadísticas por departamento
            Map<String, Integer> porDepartamento = new HashMap<>();
            artesanos.forEach(agente -> {
                String dept = agente.getDepartment();
                if (dept != null) {
                    porDepartamento.put(dept, porDepartamento.getOrDefault(dept, 0) + 1);
                }
            });
            guias.forEach(agente -> {
                String dept = agente.getDepartment();
                if (dept != null) {
                    porDepartamento.put(dept, porDepartamento.getOrDefault(dept, 0) + 1);
                }
            });
            
            estadisticas.put("porDepartamento", porDepartamento);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("estadisticas", estadisticas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener estadísticas: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener estadísticas");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Endpoint temporal sin autenticación para inicializar datos
     */
    @GetMapping("/init-data-public")
    public ResponseEntity<Map<String, Object>> inicializarDatosPublico() {
        try {
            log.info("🚀 Inicializando datos de agentes culturales (público)");
            
            dataSeeder.seedData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Datos inicializados correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al inicializar datos: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al inicializar datos: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
