package com.disrupton.collaborator.dto;

public record UnlockResponseDto(
        boolean success,
        String message,
        String paymentId,
        boolean accessGranted
) {}