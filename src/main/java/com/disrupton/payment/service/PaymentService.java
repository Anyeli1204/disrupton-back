package com.disrupton.payment.service;

import com.disrupton.payment.dto.PaymentRequest;
import com.disrupton.payment.dto.PaymentResponse;
import com.disrupton.payment.dto.WebhookRequest;
import com.disrupton.payment.enums.PaymentStatus;
import com.disrupton.payment.enums.PaymentType;
import com.disrupton.payment.model.Payment;
import com.disrupton.quota.service.QuotaService;
import com.disrupton.user.service.UserService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final Firestore firestore;
    private final UserService userService;
    private final QuotaService quotaService;
    private static final String PAYMENTS_COLLECTION = "payments";

    /**
     * Crea una preferencia de pago en MercadoPago
     */
    public PaymentResponse createPayment(String userId, PaymentRequest request) {
        try {
            log.info("💳 Creando pago para usuario: {} - Tipo: {}", userId, request.getPaymentType());

            // Validar usuario
            if (!userService.userExists(userId)) {
                return PaymentResponse.failure("Usuario no encontrado");
            }

            // Validar lógica de negocio específica
            String validationError = validatePaymentRequest(userId, request);
            if (validationError != null) {
                return PaymentResponse.failure(validationError);
            }

            // Crear el payment en base de datos
            Payment payment = createPaymentRecord(userId, request);

            // Crear preferencia en MercadoPago
            Preference preference = createMercadoPagoPreference(payment);

            // Actualizar payment con el ID de preferencia
            payment.setPreferenceId(preference.getId());
            savePayment(payment);

            log.info("✅ Pago creado exitosamente: {} - Preferencia: {}", payment.getId(), preference.getId());

            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .preferenceId(preference.getId())
                    .paymentType(request.getPaymentType())
                    .status(PaymentStatus.PENDING)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .description(request.getDescription())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .userId(userId)
                    .userEmail(request.getUserEmail())
                    .createdAt(Timestamp.now())
                    .successUrl(request.getSuccessUrl())
                    .failureUrl(request.getFailureUrl())
                    .pendingUrl(request.getPendingUrl())
                    .externalReference(payment.getExternalReference())
                    .success(true)
                    .message("Pago creado exitosamente")
                    .build();

        } catch (Exception e) {
            log.error("❌ Error creando pago para usuario {}: {}", userId, e.getMessage(), e);
            return PaymentResponse.failure("Error interno del servidor al crear el pago");
        }
    }

    /**
     * Procesa webhook de MercadoPago
     */
    public void processWebhook(WebhookRequest webhook) {
        try {
            log.info("🔄 Procesando webhook: {} - Tipo: {}", webhook.getId(), webhook.getTopic());

            if ("payment".equals(webhook.getTopic())) {
                processPaymentWebhook(webhook);
            } else {
                log.warn("⚠️ Tipo de webhook no soportado: {}", webhook.getTopic());
            }

        } catch (Exception e) {
            log.error("❌ Error procesando webhook {}: {}", webhook.getId(), e.getMessage(), e);
            throw new RuntimeException("Error procesando webhook", e);
        }
    }

    /**
     * Obtiene un pago por ID
     */
    public Payment getPaymentById(String paymentId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(PAYMENTS_COLLECTION).document(paymentId).get().get();

        if (!doc.exists()) {
            return null;
        }

        return doc.toObject(Payment.class);
    }

    /**
     * Obtiene pagos de un usuario
     */
    public List<Payment> getPaymentsByUser(String userId) throws ExecutionException, InterruptedException {
        return firestore.collection(PAYMENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                .get()
                .get()
                .toObjects(Payment.class);
    }

    // Métodos privados

    private String validatePaymentRequest(String userId, PaymentRequest request) {
        try {
            switch (request.getPaymentType()) {
                case PREMIUM_SUBSCRIPTION:
                case PREMIUM_MAX_SUBSCRIPTION:
                    return validateSubscriptionPayment(userId, request);

                case ARTISAN_PRODUCT_ADDITIONAL:
                    return validateArtisanProductPayment(userId, request);

                case GUIDE_SERVICE_ADDITIONAL:
                    return validateGuideServicePayment(userId, request);

                case FEATURED_PLACEMENT:
                    return validateFeaturedPlacementPayment(userId, request);

                case COLLABORATOR_CONTACT_ACCESS:
                    return validateCollaboratorAccessPayment(userId, request);

                default:
                    return "Tipo de pago no válido";
            }
        } catch (Exception e) {
            log.error("Error validando pago: {}", e.getMessage(), e);
            return "Error interno validando el pago";
        }
    }

    private String validateSubscriptionPayment(String userId, PaymentRequest request) throws ExecutionException, InterruptedException {
        // Verificar que el usuario no tenga ya una suscripción activa
        if (userService.hasActivePremiumAccess(userId)) {
            return "El usuario ya tiene una suscripción activa";
        }

        // Validar montos
        BigDecimal expectedAmount = request.getPaymentType() == PaymentType.PREMIUM_SUBSCRIPTION
            ? new BigDecimal("20.00") : new BigDecimal("30.00");

        if (request.getAmount().compareTo(expectedAmount) != 0) {
            return "Monto incorrecto para la suscripción";
        }

        return null;
    }

    private String validateArtisanProductPayment(String userId, PaymentRequest request) throws ExecutionException, InterruptedException {
        // Verificar que el usuario sea artesano
        var user = userService.getUserById(userId);
        if (user == null || !"ARTISAN".equals(user.getRole())) {
            return "Solo los artesanos pueden pagar por productos adicionales";
        }

        // Validar monto (1 sol por producto adicional)
        if (request.getAmount().compareTo(new BigDecimal("1.00")) != 0) {
            return "Monto incorrecto para producto adicional";
        }

        return null;
    }

    private String validateGuideServicePayment(String userId, PaymentRequest request) throws ExecutionException, InterruptedException {
        // Verificar que el usuario sea guía
        var user = userService.getUserById(userId);
        if (user == null || !"GUIDE".equals(user.getRole())) {
            return "Solo los guías pueden pagar por servicios adicionales";
        }

        // Validar monto (1 sol por servicio adicional)
        if (request.getAmount().compareTo(new BigDecimal("1.00")) != 0) {
            return "Monto incorrecto para servicio adicional";
        }

        return null;
    }

    private String validateFeaturedPlacementPayment(String userId, PaymentRequest request) throws ExecutionException, InterruptedException {
        // Verificar que el usuario sea artesano o guía
        var user = userService.getUserById(userId);
        if (user == null || (!("ARTISAN".equals(user.getRole()) || "GUIDE".equals(user.getRole())))) {
            return "Solo artesanos y guías pueden pagar por destacado";
        }

        // Validar monto (5 soles por 7 días)
        if (request.getAmount().compareTo(new BigDecimal("5.00")) != 0) {
            return "Monto incorrecto para destacado en tienda";
        }

        // Validar duración
        if (request.getDurationDays() == null || request.getDurationDays() != 7) {
            return "El destacado debe ser por 7 días";
        }

        return null;
    }

    private String validateCollaboratorAccessPayment(String userId, PaymentRequest request) {
        if (request.getCollaboratorId() == null || request.getCollaboratorId().trim().isEmpty()) {
            return "ID del colaborador es requerido";
        }

        // Validar monto (precio por acceso, típicamente 10 soles)
        if (request.getAmount().compareTo(new BigDecimal("10.00")) < 0) {
            return "Monto insuficiente para acceso a colaborador";
        }

        return null;
    }

    private Payment createPaymentRecord(String userId, PaymentRequest request) throws ExecutionException, InterruptedException {
        String paymentId = UUID.randomUUID().toString();
        String externalReference = "DPT_" + paymentId.substring(0, 8);

        var user = userService.getUserById(userId);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setUserId(userId);
        payment.setUserEmail(user != null ? user.getEmail() : request.getUserEmail());
        payment.setPaymentType(request.getPaymentType());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setDescription(request.getDescription());
        payment.setMetadata(request.getMetadata());
        payment.setSuccessUrl(request.getSuccessUrl());
        payment.setFailureUrl(request.getFailureUrl());
        payment.setPendingUrl(request.getPendingUrl());
        payment.setExternalReference(externalReference);
        payment.setCreatedAt(Timestamp.now());
        payment.setUpdatedAt(Timestamp.now());

        // Configurar expiración para suscripciones
        if (payment.isSubscription()) {
            long durationDays = request.getPaymentType() == PaymentType.PREMIUM_SUBSCRIPTION ? 30 : 30; // ambos por 30 días
            long expirationTime = System.currentTimeMillis() + (durationDays * 24L * 60L * 60L * 1000L);
            payment.setExpiresAt(Timestamp.of(new java.util.Date(expirationTime)));
        }

        return payment;
    }

    private Preference createMercadoPagoPreference(Payment payment) throws MPException, MPApiException {
        PreferenceClient client = new PreferenceClient();

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(payment.getDescription())
                .quantity(1)
                .unitPrice(payment.getAmount())
                .currencyId(payment.getCurrency())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(payment.getSuccessUrl())
                .failure(payment.getFailureUrl())
                .pending(payment.getPendingUrl())
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(payment.getExternalReference())
                .notificationUrl("https://tu-dominio.com/webhook/mercadopago") // Cambiar por tu URL
                .build();

        return client.create(preferenceRequest);
    }

    private void savePayment(Payment payment) throws ExecutionException, InterruptedException {
        firestore.collection(PAYMENTS_COLLECTION).document(payment.getId()).set(payment).get();
    }

    private void processPaymentWebhook(WebhookRequest webhook) throws Exception {
        String paymentIdStr = webhook.getId();

        // Obtener información del pago desde MercadoPago
        PaymentClient paymentClient = new PaymentClient();
        com.mercadopago.resources.payment.Payment mpPayment = paymentClient.get(Long.parseLong(paymentIdStr));

        // Buscar nuestro payment por external reference
        String externalReference = mpPayment.getExternalReference();

        var payments = firestore.collection(PAYMENTS_COLLECTION)
                .whereEqualTo("externalReference", externalReference)
                .limit(1)
                .get()
                .get()
                .toObjects(Payment.class);

        if (payments.isEmpty()) {
            log.warn("⚠️ No se encontró pago con external reference: {}", externalReference);
            return;
        }

        Payment payment = payments.get(0);
        PaymentStatus oldStatus = payment.getStatus();
        PaymentStatus newStatus = PaymentStatus.fromMpStatus(mpPayment.getStatus());

        // Actualizar payment
        payment.setStatus(newStatus);
        payment.setMercadoPagoPaymentId(paymentIdStr);
        payment.setPayerEmail(mpPayment.getPayer().getEmail());
        payment.setPaymentMethodId(mpPayment.getPaymentMethodId());
        payment.setPaymentTypeId(mpPayment.getPaymentTypeId());
        payment.setUpdatedAt(Timestamp.now());

        if (newStatus.isSuccess()) {
            payment.setProcessedAt(Timestamp.now());
        }

        savePayment(payment);

        // Procesar lógica de negocio solo si cambió a exitoso
        if (newStatus.isSuccess() && !oldStatus.isSuccess()) {
            processSuccessfulPayment(payment);
        }

        log.info("✅ Webhook procesado para pago: {} - Estado: {} -> {}",
                payment.getId(), oldStatus, newStatus);
    }

    private void processSuccessfulPayment(Payment payment) throws ExecutionException, InterruptedException {
        log.info("🎉 Procesando pago exitoso: {} - Tipo: {}", payment.getId(), payment.getPaymentType());

        switch (payment.getPaymentType()) {
            case PREMIUM_SUBSCRIPTION:
                userService.upgradeUserToPremium(payment.getUserId(), "PREMIUM", 30);
                break;

            case PREMIUM_MAX_SUBSCRIPTION:
                userService.upgradeUserToPremium(payment.getUserId(), "PREMIUM_MAX", 30);
                break;

            case ARTISAN_PRODUCT_ADDITIONAL:
                quotaService.addPaidProduct(payment.getUserId());
                log.info("✅ Producto adicional agregado para artesano: {}", payment.getUserId());
                break;

            case GUIDE_SERVICE_ADDITIONAL:
                quotaService.addPaidService(payment.getUserId());
                log.info("✅ Servicio adicional agregado para guía: {}", payment.getUserId());
                break;

            case FEATURED_PLACEMENT:
                quotaService.activateFeaturedPlacement(payment.getUserId(), 7);
                log.info("✅ Destacado activado para usuario: {} por 7 días", payment.getUserId());
                break;

            case COLLABORATOR_CONTACT_ACCESS:
                // Ya manejado por CollaboratorService
                break;

            default:
                log.warn("⚠️ Tipo de pago no reconocido para procesamiento: {}", payment.getPaymentType());
        }
    }
}