package com.disrupton.payment.enums;

public enum PaymentType {
    PREMIUM_SUBSCRIPTION("PREMIUM_SUBSCRIPTION", "Suscripción Premium"),
    PREMIUM_MAX_SUBSCRIPTION("PREMIUM_MAX_SUBSCRIPTION", "Suscripción Premium Max"),
    ARTISAN_PRODUCT_ADDITIONAL("ARTISAN_PRODUCT_ADDITIONAL", "Producto adicional de artesano"),
    GUIDE_SERVICE_ADDITIONAL("GUIDE_SERVICE_ADDITIONAL", "Servicio adicional de guía"),
    FEATURED_PLACEMENT("FEATURED_PLACEMENT", "Destacado en tienda"),
    COLLABORATOR_CONTACT_ACCESS("COLLABORATOR_CONTACT_ACCESS", "Acceso a contacto de colaborador");

    private final String code;
    private final String description;

    PaymentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentType fromCode(String code) {
        for (PaymentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de pago inválido: " + code);
    }
}