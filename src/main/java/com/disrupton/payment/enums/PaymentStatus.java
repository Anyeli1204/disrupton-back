package com.disrupton.payment.enums;

public enum PaymentStatus {
    PENDING("pending", "Pendiente"),
    APPROVED("approved", "Aprobado"),
    AUTHORIZED("authorized", "Autorizado"),
    IN_PROCESS("in_process", "En proceso"),
    IN_MEDIATION("in_mediation", "En mediación"),
    REJECTED("rejected", "Rechazado"),
    CANCELLED("cancelled", "Cancelado"),
    REFUNDED("refunded", "Reembolsado"),
    CHARGED_BACK("charged_back", "Contracargo");

    private final String mpStatus;
    private final String description;

    PaymentStatus(String mpStatus, String description) {
        this.mpStatus = mpStatus;
        this.description = description;
    }

    public String getMpStatus() {
        return mpStatus;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentStatus fromMpStatus(String mpStatus) {
        for (PaymentStatus status : values()) {
            if (status.mpStatus.equals(mpStatus)) {
                return status;
            }
        }
        return PENDING; // Default
    }

    public boolean isSuccess() {
        return this == APPROVED || this == AUTHORIZED;
    }

    public boolean isFailed() {
        return this == REJECTED || this == CANCELLED;
    }
}