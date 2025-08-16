package com.disrupton.collaborator.dto;

public record UnlockRequestDto(
        String paymentToken // Ejemplo: un token de pago de Stripe, etc.
) {}
