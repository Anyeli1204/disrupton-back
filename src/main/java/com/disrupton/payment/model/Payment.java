package com.disrupton.payment.model;

import com.disrupton.payment.enums.PaymentStatus;
import com.disrupton.payment.enums.PaymentType;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @DocumentId
    private String id;

    // Información del usuario
    private String userId;
    private String userEmail;

    // Información del pago
    private PaymentType paymentType;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;

    // MercadoPago data
    private String mercadoPagoPaymentId;
    private String preferenceId;
    private String collectorId;
    private String payerEmail;
    private String paymentMethodId;
    private String paymentTypeId;

    // Metadata específica según el tipo de pago
    private Map<String, Object> metadata;

    // Fechas importantes
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp expiresAt; // Para suscripciones
    private Timestamp processedAt;

    // URLs de callback
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;

    // Información adicional
    private String description;
    private String externalReference;

    // Auditoría
    private String ipAddress;
    private String userAgent;

    // Métodos de conveniencia
    public boolean isSuccessful() {
        return status != null && status.isSuccess();
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.IN_PROCESS;
    }

    public boolean isFailed() {
        return status != null && status.isFailed();
    }

    public boolean isSubscription() {
        return paymentType == PaymentType.PREMIUM_SUBSCRIPTION ||
               paymentType == PaymentType.PREMIUM_MAX_SUBSCRIPTION;
    }

    public boolean isExpired() {
        if (expiresAt == null) return false;
        return Timestamp.now().compareTo(expiresAt) > 0;
    }
}