package com.disrupton.auth.enums;

public enum UserRole {
    USER("USER", "Usuario regular"),
    ADMIN("ADMIN", "Administrador del sistema"),
    MODERATOR("MODERATOR", "Moderador de contenido"),
    GUIDE("GUIDE", "Guía turístico"),
    ARTISAN("ARTISAN", "Artesano"),
    AGENTE_CULTURAL("AGENTE_CULTURAL", "Agente Cultural"),
    PREMIUM("PREMIUM", "Usuario premium");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER; // Default role
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isModerator() {
        return this == MODERATOR || this == ADMIN;
    }

    public boolean isGuide() {
        return this == GUIDE || this == ADMIN;
    }

    public boolean isArtisan() {
        return this == ARTISAN || this == ADMIN;
    }

    public boolean isCulturalAgent() {
        return this == AGENTE_CULTURAL || this == ADMIN;
    }

    public boolean isCollaborator() {
        return this == GUIDE || this == ARTISAN || this == AGENTE_CULTURAL || this == ADMIN;
    }

    public boolean isPremium() {
        return this == PREMIUM || this == ADMIN;
    }
}
