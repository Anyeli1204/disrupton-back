package com.disrupton.payment.dto;

import com.disrupton.payment.enums.PaymentStatus;
import com.disrupton.payment.enums.PaymentType;
import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String preferenceId;
    private PaymentType paymentType;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String description;

    // URLs de MercadoPago
    private String initPoint;
    private String sandboxInitPoint;

    // Información del usuario
    private String userId;
    private String userEmail;

    // Fechas
    private Timestamp createdAt;
    private Timestamp expiresAt;

    // URLs de callback
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;

    // Mensaje para el usuario
    private String message;
    private boolean success;

    // Información de MercadoPago
    private String mercadoPagoPaymentId;
    private String externalReference;

    public static PaymentResponse success(String paymentId, String preferenceId, String initPoint) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .preferenceId(preferenceId)
                .initPoint(initPoint)
                .success(true)
                .message("Pago creado exitosamente")
                .createdAt(Timestamp.now())
                .build();
    }

    public static PaymentResponse failure(String message) {
        return PaymentResponse.builder()
                .success(false)
                .message(message)
                .createdAt(Timestamp.now())
                .build();
    }
}