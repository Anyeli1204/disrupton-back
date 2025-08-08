package com.disrupton.avatar.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.google.cloud.Timestamp;

/**
 * Entidad Avatar - Representa un avatar cultural simplificado
 * Adaptado para Firebase (NoSQL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avatar {
    
    // Identificador único del avatar (usado como document ID en Firebase)
    private String avatarId;
    
    // Tipo de avatar (solo 3 tipos disponibles)
    private AvatarType type;
    
    // Nombre para mostrar del avatar
    private String displayName;
    
    // URL del modelo 3D del avatar
    private String avatar3DModelUrl;
    
    // Metadatos
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
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
    
    /**
     * Método para establecer timestamps antes de guardar
     */
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Timestamp.now();
        }
        updatedAt = Timestamp.now();
    }
    
    /**
     * Método para actualizar timestamp de modificación
     */
    public void onUpdate() {
        updatedAt = Timestamp.now();
    }
}
