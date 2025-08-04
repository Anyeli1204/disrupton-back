package com.disrupton.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad Avatar - Representa un avatar cultural simplificado
 */
@Entity
@Table(name = "avatars")
@Data
public class Avatar {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Identificador único del avatar
    @Column(unique = true, nullable = false)
    private String avatarId;
    
    // Tipo de avatar (solo 3 tipos disponibles)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvatarType type;
    
    // URL del modelo 3D del avatar
    @Column(length = 500)
    private String avatar3DModelUrl;
    
    // Metadatos
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    // Enums
    public enum AvatarType {
        VICUNA("Vicuña"),
        PERUVIAN_DOG("Perro Peruano"),
        COCK_OF_THE_ROCK("Gallito de las Rocas");
        
        private final String displayName;
        
        AvatarType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
