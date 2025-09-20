package com.disrupton.payment.dto;

import com.disrupton.payment.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "El tipo de pago es obligatorio")
    private PaymentType paymentType;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    private String currency = "PEN";

    private String description;

    // URLs de callback
    private String successUrl;
    private String failureUrl;
    private String pendingUrl;

    // Metadatos específicos según el tipo de pago
    private Map<String, Object> metadata;

    // Para pagos de productos o servicios específicos
    private String itemId; // ID del producto, servicio, etc.
    private String collaboratorId; // Para acceso a colaboradores

    // Para featured placement
    private Integer durationDays; // Para featured placement (7 días)

    // Información del usuario para tracking
    private String userEmail;
    private String userName;
}