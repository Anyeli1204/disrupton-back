package com.disrupton.cultural.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CulturalObject {
    
    private String id; // ID del documento en Firestore
    
    // Información del objeto
    private String name;
    private String description;
    private String origin;
    private String culturalType; // Tipo de cultura representada
    private String localPhrases; // Frases locales relacionadas
    private String story; // Historia o anécdota
    
    // Información técnica
    private String kiriEngineSerial; // Serial del modelo 3D en KIRI Engine
    private String modelUrl; // URL del modelo 3D generado
    private String thumbnailUrl; // URL de la imagen miniatura
    private String fileFormat; // Formato del modelo (OBJ, FBX, etc.)
    
    // Metadatos
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status; // DRAFT, PENDING_REVIEW, APPROVED, REJECTED
    
    // Referencias a usuarios (IDs en lugar de objetos)
    private String contributorId; // ID del estudiante que contribuyó
    private String moderatorId; // ID del moderador que revisó
    
    // Información de captura
    private Integer numberOfImages;
    private String captureNotes; // Notas sobre la captura
    private String region; // Región de origen
    
    // Enums
    public enum Status {
        DRAFT, PENDING_REVIEW, APPROVED, REJECTED
    }
    
    public enum CulturalType {
        ARTESANIA, GASTRONOMIA, MUSICA, DANZA, TEXTIL, CERAMICA, 
        ARQUITECTURA, FESTIVAL, RITUAL, LENGUAJE, TRADICION, OTRO
    }
} 