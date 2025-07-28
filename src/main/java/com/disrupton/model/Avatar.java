package com.disrupton.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Avatar - Representa un avatar cultural interactivo
 */
@Entity
@Table(name = "avatars")
@Data
public class Avatar {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Información básica del avatar
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvatarType type;
    
    // Información cultural
    @Column(length = 100)
    private String culturalRegion;
    
    @Column(length = 100)
    private String culturalPeriod;
    
    @Column(length = 200)
    private String expertise;
    
    @ElementCollection
    @CollectionTable(name = "avatar_languages", joinColumns = @JoinColumn(name = "avatar_id"))
    @Column(name = "language")
    private List<String> languages;
    
    // Personalidad y características
    @Enumerated(EnumType.STRING)
    private Personality personality;
    
    @Enumerated(EnumType.STRING)
    private VoiceType voiceType;
    
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;
    
    @Enumerated(EnumType.STRING)
    private CommunicationStyle communicationStyle;
    
    // Apariencia visual
    @Column(length = 500)
    private String avatarImageUrl;
    
    @Column(length = 500)
    private String avatar3DModelUrl;
    
    @Column(length = 200)
    private String traditionalDress;
    
    // Conocimiento cultural
    @ElementCollection
    @CollectionTable(name = "avatar_knowledge_areas", joinColumns = @JoinColumn(name = "avatar_id"))
    @Column(name = "knowledge_area")
    private List<String> knowledgeAreas;
    
    // Estado y configuración
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    private AvatarMood currentMood;
    
    @Column(nullable = false)
    private Integer interactionLevel = 1;
    
    // Contexto geográfico
    @Column(length = 100)
    private String campusZone;
    
    @Column
    private Double latitude;
    
    @Column
    private Double longitude;
    
    // Configuración de IA
    @Column(length = 100)
    private String aiModelType;
    
    @Column(nullable = false)
    private Boolean learningEnabled = false;
    
    // Capacidades conversacionales
    @Column(nullable = false)
    private Boolean canAnswerQuestions = true;
    
    @Column(nullable = false)
    private Boolean canTellStories = true;
    
    @Column(nullable = false)
    private Boolean canProvideDirections = false;
    
    @Column(nullable = false)
    private Boolean canRecommendContent = true;
    
    @Column(nullable = false)
    private Boolean canTranslate = false;
    
    // Metadatos
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvatarStatus status;
    
    // Estadísticas
    @Column(nullable = false)
    private Long totalInteractions = 0L;
    
    @Column
    private Double rating = 0.0;
    
    @Column(nullable = false)
    private Integer popularity = 0;
    
    // Relaciones
    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL)
    private List<AvatarConversation> conversations;
    
    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL)
    private List<AvatarSession> sessions;
    
    @OneToMany(mappedBy = "avatar", cascade = CascadeType.ALL)
    private List<AvatarKnowledge> knowledgeBase;
    
    // Enums
    public enum AvatarType {
        CULTURAL_GUIDE,
        HISTORICAL_FIGURE,
        MYTHOLOGICAL_CHARACTER,
        CONTEMPORARY_EXPERT,
        STORYTELLER,
        ACADEMIC_PROFESSOR,
        LOCAL_ARTIST,
        TRADITIONAL_HEALER
    }
    
    public enum Personality {
        FRIENDLY,
        WISE,
        ENTHUSIASTIC,
        SCHOLARLY,
        MYSTICAL,
        HUMOROUS,
        SERIOUS,
        INSPIRING
    }
    
    public enum VoiceType {
        MALE,
        FEMALE,
        NEUTRAL,
        CHILD,
        ELDER
    }
    
    public enum AgeGroup {
        YOUNG,
        ADULT,
        ELDER
    }
    
    public enum CommunicationStyle {
        FORMAL,
        CASUAL,
        STORYTELLING,
        ACADEMIC,
        CONVERSATIONAL,
        POETIC
    }
    
    public enum AvatarMood {
        HAPPY,
        CURIOUS,
        WISE,
        EXCITED,
        CALM,
        MYSTERIOUS,
        THOUGHTFUL
    }
    
    public enum AvatarStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        TRAINING,
        BETA,
        ARCHIVED
    }
    
    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = AvatarStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void incrementInteractions() {
        this.totalInteractions++;
    }
    
    public void updateRating(Double newRating) {
        // Lógica para calcular rating promedio
        // Esto se puede mejorar con un sistema más sofisticado
        if (this.rating == 0.0) {
            this.rating = newRating;
        } else {
            this.rating = (this.rating + newRating) / 2.0;
        }
    }
}
