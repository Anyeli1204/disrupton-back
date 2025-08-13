package com.disrupton.user.dto;

import lombok.Data;
import com.google.cloud.Timestamp;

/**
 * DTO para Avatar Cultural simplificado
 * Representa un avatar virtual (Vicuña, Perro Peruano o Gallito de las Rocas)
 */
@Data
public class AvatarDto {
    
    // Identificación del avatar
    private String avatarId;
    
    // Tipo de avatar
    private String type; // VICUNA, PERUVIAN_DOG, COCK_OF_THE_ROCK
    
    // Nombre de visualización
    private String displayName;
    
    // URL del modelo 3D del avatar
    private String avatar3DModelUrl;
    
    // Metadatos
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
