package com.disrupton.payment.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.payment.dto.PaymentRequest;
import com.disrupton.payment.dto.PaymentResponse;
import com.disrupton.payment.dto.WebhookRequest;
import com.disrupton.payment.model.Payment;
import com.disrupton.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Crear un nuevo pago
     */
    @PostMapping("/create")
    @RequireRole({"USER", "PREMIUM", "PREMIUM_MAX", "GUIDE", "ARTISAN", "AGENTE_CULTURAL"})
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            log.info("💳 Solicitud de pago recibida para usuario: {} - Tipo: {}", userId, request.getPaymentType());

            PaymentResponse response = paymentService.createPayment(userId, request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("❌ Error creando pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("Error interno del servidor"));
        }
    }

    /**
     * Webhook de MercadoPago
     */
    @PostMapping("/webhook/mercadopago")
    public ResponseEntity<Map<String, String>> handleWebhook(@RequestBody WebhookRequest webhook) {
        try {
            log.info("🔔 Webhook recibido: {}", webhook.getId());

            paymentService.processWebhook(webhook);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Webhook procesado correctamente"
            ));

        } catch (Exception e) {
            log.error("❌ Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Error procesando webhook"
            ));
        }
    }

    /**
     * Obtener detalles de un pago
     */
    @GetMapping("/{paymentId}")
    @RequireRole({"USER", "PREMIUM", "PREMIUM_MAX", "GUIDE", "ARTISAN", "AGENTE_CULTURAL", "ADMIN"})
    public ResponseEntity<Payment> getPayment(
            @PathVariable String paymentId,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            Payment payment = paymentService.getPaymentById(paymentId);

            if (payment == null) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que el usuario sea el dueño del pago o sea admin
            // TODO: Implementar verificación de admin role
            if (!payment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(payment);

        } catch (Exception e) {
            log.error("❌ Error obteniendo pago {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener historial de pagos del usuario
     */
    @GetMapping("/history")
    @RequireRole({"USER", "PREMIUM", "PREMIUM_MAX", "GUIDE", "ARTISAN", "AGENTE_CULTURAL"})
    public ResponseEntity<List<Payment>> getPaymentHistory(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<Payment> payments = paymentService.getPaymentsByUser(userId);

            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            log.error("❌ Error obteniendo historial de pagos para usuario {}: {}",
                    authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para crear pago de suscripción premium
     */
    @PostMapping("/subscription/premium")
    @RequireRole({"USER"})
    public ResponseEntity<PaymentResponse> createPremiumSubscription(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentType(com.disrupton.payment.enums.PaymentType.PREMIUM_SUBSCRIPTION);
            paymentRequest.setAmount(new java.math.BigDecimal("10.00"));
            paymentRequest.setCurrency("PEN");
            paymentRequest.setDescription("Suscripción Premium - 30 días");
            paymentRequest.setSuccessUrl(request.get("successUrl"));
            paymentRequest.setFailureUrl(request.get("failureUrl"));
            paymentRequest.setPendingUrl(request.get("pendingUrl"));

            return createPayment(paymentRequest, authentication);

        } catch (Exception e) {
            log.error("❌ Error creando suscripción premium: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("Error interno del servidor"));
        }
    }

    /**
     * Endpoint para crear pago de suscripción premium max
     */
    @PostMapping("/subscription/premium-max")
    @RequireRole({"USER"})
    public ResponseEntity<PaymentResponse> createPremiumMaxSubscription(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentType(com.disrupton.payment.enums.PaymentType.PREMIUM_MAX_SUBSCRIPTION);
            paymentRequest.setAmount(new java.math.BigDecimal("30.00"));
            paymentRequest.setCurrency("PEN");
            paymentRequest.setDescription("Suscripción Premium Max - 30 días");
            paymentRequest.setSuccessUrl(request.get("successUrl"));
            paymentRequest.setFailureUrl(request.get("failureUrl"));
            paymentRequest.setPendingUrl(request.get("pendingUrl"));

            return createPayment(paymentRequest, authentication);

        } catch (Exception e) {
            log.error("❌ Error creando suscripción premium max: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("Error interno del servidor"));
        }
    }

    /**
     * Endpoint para producto adicional de artesano
     */
    @PostMapping("/artisan/product-additional")
    @RequireRole({"ARTISAN"})
    public ResponseEntity<PaymentResponse> createArtisanProductPayment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentType(com.disrupton.payment.enums.PaymentType.ARTISAN_PRODUCT_ADDITIONAL);
            paymentRequest.setAmount(new java.math.BigDecimal("1.00"));
            paymentRequest.setCurrency("PEN");
            paymentRequest.setDescription("Producto adicional - Artesano");
            paymentRequest.setSuccessUrl(request.get("successUrl"));
            paymentRequest.setFailureUrl(request.get("failureUrl"));
            paymentRequest.setPendingUrl(request.get("pendingUrl"));

            return createPayment(paymentRequest, authentication);

        } catch (Exception e) {
            log.error("❌ Error creando pago producto adicional: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("Error interno del servidor"));
        }
    }

    /**
     * Endpoint para servicio adicional de guía
     */
    @PostMapping("/guide/service-additional")
    @RequireRole({"GUIDE"})
    public ResponseEntity<PaymentResponse> createGuideServicePayment(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentType(com.disrupton.payment.enums.PaymentType.GUIDE_SERVICE_ADDITIONAL);
            paymentRequest.setAmount(new java.math.BigDecimal("1.00"));
            paymentRequest.setCurrency("PEN");
            paymentRequest.setDescription("Servicio adicional - Guía Turístico");
            paymentRequest.setSuccessUrl(request.get("successUrl"));
            paymentRequest.setFailureUrl(request.get("failureUrl"));
            paymentRequest.setPendingUrl(request.get("pendingUrl"));

            return createPayment(paymentRequest, authentication);

        } catch (Exception e) {
            log.error("❌ Error creando pago servicio adicional: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("Error interno del servidor"));
        }
    }

    /**
     * Endpoint para destacado en tienda
     */
    @PostMapping("/featured-placement")
    @RequireRole({"GUIDE", "ARTISAN"})
    public ResponseEntity<PaymentResponse> createFeaturedPlacementPayment(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentType(com.disrupton.payment.enums.PaymentType.FEATURED_PLACEMENT);
            paymentRequest.setAmount(new java.math.BigDecimal("5.00"));
            paymentRequest.setCurrency("PEN");
            paymentRequest.setDescription("Destacado en tienda - 7 días");
            paymentRequest.setDurationDays(7);
            paymentRequest.setItemId((String) request.get("itemId")); // ID del producto/servicio
            paymentRequest.setSuccessUrl((String) request.get("successUrl"));
            paymentRequest.setFailureUrl((String) request.get("failureUrl"));
            paymentRequest.setPendingUrl((String) request.get("pendingUrl"));

            return createPayment(paymentRequest, authentication);

        } catch (Exception e) {
            log.error("❌ Error creando pago destacado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("Error interno del servidor"));
        }
    }
}