package com.disrupton.cultural.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cultural_objects")
@Data
public class CulturalObject {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
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
    
    // Relaciones
    @ManyToOne
    @JoinColumn(name = "user_id")
    private com.disrupton.user.model.User contributor; // Estudiante que contribuyó
    
    @ManyToOne
    @JoinColumn(name = "moderator_id")
    private com.disrupton.user.model.User moderator; // Moderador que revisó
    
    @OneToMany(mappedBy = "culturalObject", cascade = CascadeType.ALL)
    private List<com.disrupton.comment.model.Comment> comments;
    
    @OneToMany(mappedBy = "culturalObject", cascade = CascadeType.ALL)
    private List<com.disrupton.reaction.model.Reaction> reactions;
    
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