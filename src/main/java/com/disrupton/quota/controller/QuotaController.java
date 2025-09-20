package com.disrupton.quota.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.quota.dto.QuotaInfoDto;
import com.disrupton.quota.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quota")
@RequiredArgsConstructor
@Slf4j
public class QuotaController {

    private final QuotaService quotaService;

    /**
     * Obtener información de cuota del usuario actual
     */
    @GetMapping("/info")
    @RequireRole({"USER", "PREMIUM", "PREMIUM_MAX", "GUIDE", "ARTISAN", "AGENTE_CULTURAL"})
    public ResponseEntity<QuotaInfoDto> getMyQuotaInfo(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("📊 Obteniendo información de cuota para usuario: {}", userId);

            QuotaInfoDto quotaInfo = quotaService.getUserQuotaInfo(userId);
            return ResponseEntity.ok(quotaInfo);

        } catch (Exception e) {
            log.error("❌ Error obteniendo información de cuota: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verificar si puede subir un producto (solo ARTISAN)
     */
    @GetMapping("/products/can-upload")
    @RequireRole({"ARTISAN"})
    public ResponseEntity<Map<String, Object>> canUploadProduct(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("🔍 Verificando si usuario {} puede subir producto", userId);

            boolean canUpload = quotaService.canUploadProduct(userId);
            QuotaInfoDto quotaInfo = quotaService.getUserQuotaInfo(userId);

            Map<String, Object> response = Map.of(
                    "canUpload", canUpload,
                    "remainingProducts", quotaInfo.getRemainingProducts(),
                    "totalAllowed", quotaInfo.getMaxProductsAllowed(),
                    "currentUsed", quotaInfo.getProductsUploaded(),
                    "message", canUpload ? "Puedes subir más productos" : "Has alcanzado tu límite mensual"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error verificando upload de producto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Verificar si puede subir un servicio (solo GUIDE)
     */
    @GetMapping("/services/can-upload")
    @RequireRole({"GUIDE"})
    public ResponseEntity<Map<String, Object>> canUploadService(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("🔍 Verificando si usuario {} puede subir servicio", userId);

            boolean canUpload = quotaService.canUploadService(userId);
            QuotaInfoDto quotaInfo = quotaService.getUserQuotaInfo(userId);

            Map<String, Object> response = Map.of(
                    "canUpload", canUpload,
                    "remainingServices", quotaInfo.getRemainingServices(),
                    "totalAllowed", quotaInfo.getMaxServicesAllowed(),
                    "currentUsed", quotaInfo.getServicesUploaded(),
                    "message", canUpload ? "Puedes subir más servicios" : "Has alcanzado tu límite mensual"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error verificando upload de servicio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Verificar estado de destacado (ARTISAN y GUIDE)
     */
    @GetMapping("/featured/status")
    @RequireRole({"GUIDE", "ARTISAN"})
    public ResponseEntity<Map<String, Object>> getFeaturedStatus(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("⭐ Verificando estado de destacado para usuario: {}", userId);

            boolean hasFeatured = quotaService.hasFeaturedPlacement(userId);
            QuotaInfoDto quotaInfo = quotaService.getUserQuotaInfo(userId);

            Map<String, Object> response = Map.of(
                    "hasFeaturedPlacement", hasFeatured,
                    "featuredExpiresAt", quotaInfo.getFeaturedExpiresAt() != null ? quotaInfo.getFeaturedExpiresAt() : null,
                    "featuredPlacementsPaid", quotaInfo.getFeaturedPlacementsPaid() != null ? quotaInfo.getFeaturedPlacementsPaid() : 0,
                    "message", hasFeatured ? "Tienes destacado activo" : "No tienes destacado activo"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error verificando estado de destacado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Obtener resumen completo de cuotas (para dashboard)
     */
    @GetMapping("/summary")
    @RequireRole({"GUIDE", "ARTISAN"})
    public ResponseEntity<Map<String, Object>> getQuotaSummary(Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("📈 Obteniendo resumen de cuotas para usuario: {}", userId);

            QuotaInfoDto quotaInfo = quotaService.getUserQuotaInfo(userId);

            Map<String, Object> summary = Map.of(
                    "userId", userId,
                    "userRole", quotaInfo.getUserRole(),
                    "currentMonth", quotaInfo.getCurrentMonth(),
                    "currentYear", quotaInfo.getCurrentYear(),
                    "quotaInfo", quotaInfo,
                    "recommendations", generateRecommendations(quotaInfo)
            );

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("❌ Error obteniendo resumen de cuotas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Endpoint de administrador para obtener cuotas de cualquier usuario
     */
    @GetMapping("/user/{userId}")
    @RequireRole({"ADMIN"})
    public ResponseEntity<QuotaInfoDto> getUserQuotaInfo(@PathVariable String userId) {
        try {
            log.info("👨‍💼 Admin obteniendo información de cuota para usuario: {}", userId);

            QuotaInfoDto quotaInfo = quotaService.getUserQuotaInfo(userId);
            return ResponseEntity.ok(quotaInfo);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error obteniendo información de cuota para usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Métodos privados

    private Map<String, String> generateRecommendations(QuotaInfoDto quotaInfo) {
        if ("ARTISAN".equals(quotaInfo.getUserRole())) {
            if (quotaInfo.getRemainingProducts() != null && quotaInfo.getRemainingProducts() <= 1) {
                return Map.of(
                        "type", "warning",
                        "message", "Te quedan pocos productos disponibles este mes. Considera comprar productos adicionales.",
                        "action", "Comprar productos adicionales por S/1 cada uno"
                );
            } else if (!Boolean.TRUE.equals(quotaInfo.getHasFeaturedPlacement())) {
                return Map.of(
                        "type", "info",
                        "message", "Destaca tus productos por S/5 durante 7 días para aumentar ventas.",
                        "action", "Activar destacado en tienda"
                );
            }
        } else if ("GUIDE".equals(quotaInfo.getUserRole())) {
            if (quotaInfo.getRemainingServices() != null && quotaInfo.getRemainingServices() <= 1) {
                return Map.of(
                        "type", "warning",
                        "message", "Te quedan pocos servicios disponibles este mes. Considera comprar servicios adicionales.",
                        "action", "Comprar servicios adicionales por S/1 cada uno"
                );
            } else if (!Boolean.TRUE.equals(quotaInfo.getHasFeaturedPlacement())) {
                return Map.of(
                        "type", "info",
                        "message", "Destaca tus servicios por S/5 durante 7 días para aumentar reservas.",
                        "action", "Activar destacado en eventos"
                );
            }
        }

        return Map.of(
                "type", "success",
                "message", "Todo está en orden con tus cuotas.",
                "action", "Continúa creando contenido de calidad"
        );
    }
}