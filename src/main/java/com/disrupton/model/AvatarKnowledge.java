package com.disrupton.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad AvatarKnowledge - Base de conocimientos culturales de un avatar
 */
@Entity
@Table(name = "avatar_knowledge")
@Data
public class AvatarKnowledge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación con avatar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;
    
    // Categorización del conocimiento
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KnowledgeCategory category;
    
    @Column(length = 200)
    private String topic;
    
    @Column(length = 300, nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 500)
    private String summary;
    
    // Contexto cultural
    @Column(length = 100)
    private String culturalRegion;
    
    @Column(length = 100)
    private String culturalPeriod;
    
    @Column(length = 100)
    private String ethnicity;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_related_cultures", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "culture")
    private List<String> relatedCultures;
    
    // Información académica
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_keywords", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "keyword")
    private List<String> keywords;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_related_topics", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "topic")
    private List<String> relatedTopics;
    
    @Column(length = 500)
    private String academicSource;
    
    @Column(columnDefinition = "TEXT")
    private String bibliography;
    
    // Contenido multimedia
    @ElementCollection
    @CollectionTable(name = "knowledge_images", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_videos", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "video_url")
    private List<String> videoUrls;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_audios", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "audio_url")
    private List<String> audioUrls;
    
    @Column(length = 500)
    private String threeDModelUrl;
    
    // Metadatos narrativos
    @Enumerated(EnumType.STRING)
    private StorytellingStyle storytellingStyle;
    
    @Enumerated(EnumType.STRING)
    private TargetAudience targetAudience;
    
    @Column
    private Integer estimatedReadingTime;
    
    @Enumerated(EnumType.STRING)
    private EmotionalTone emotionalTone;
    
    // Interactividad
    @ElementCollection
    @CollectionTable(name = "knowledge_questions", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "question", columnDefinition = "TEXT")
    private List<String> relatedQuestions;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_followup_topics", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "topic")
    private List<String> followUpTopics;
    
    @Column(nullable = false)
    private Boolean hasQuiz = false;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_activities", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "activity", columnDefinition = "TEXT")
    private List<String> practicalActivities;
    
    // Localización
    @Column(length = 10, nullable = false)
    private String originalLanguage = "es";
    
    @ElementCollection
    @CollectionTable(name = "knowledge_languages", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "language")
    private List<String> availableLanguages;
    
    // Validación y calidad
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;
    
    @Column(length = 100)
    private String verifiedBy;
    
    @Column
    private LocalDateTime verificationDate;
    
    @Column
    private Double accuracyScore;
    
    @Enumerated(EnumType.STRING)
    private SourceReliability sourceReliability;
    
    // Estadísticas de uso
    @Column(nullable = false)
    private Long timesAccessed = 0L;
    
    @Column
    private Double averageRating = 0.0;
    
    @Column(nullable = false)
    private Integer totalRatings = 0;
    
    @Column(nullable = false)
    private Long shareCount = 0L;
    
    @Column
    private LocalDateTime lastAccessed;
    
    // Conexiones
    @ManyToMany
    @JoinTable(
        name = "knowledge_cultural_objects",
        joinColumns = @JoinColumn(name = "knowledge_id"),
        inverseJoinColumns = @JoinColumn(name = "cultural_object_id")
    )
    private List<CulturalObject> relatedCulturalObjects;
    
    @Column(length = 100)
    private String primaryCulturalObjectId;
    
    // Configuración de IA
    @Column(length = 200)
    private String intentCategory;
    
    @ElementCollection
    @CollectionTable(name = "knowledge_trigger_phrases", joinColumns = @JoinColumn(name = "knowledge_id"))
    @Column(name = "phrase")
    private List<String> triggerPhrases;
    
    @Column
    private Double relevanceScore = 1.0;
    
    // Metadatos del sistema
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KnowledgeStatus status;
    
    @Column(length = 50)
    private String version = "1.0";
    
    @Column(nullable = false)
    private Boolean isPublic = true;
    
    // Enums
    public enum KnowledgeCategory {
        HISTORY,
        TRADITIONS,
        ART,
        ARCHITECTURE,
        MUSIC,
        CUISINE,
        LANGUAGE,
        MYTHOLOGY,
        CEREMONIES,
        CRAFTS,
        TEXTILES,
        POTTERY,
        AGRICULTURE,
        MEDICINE,
        PHILOSOPHY
    }
    
    public enum DifficultyLevel {
        BASIC,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
    
    public enum StorytellingStyle {
        LEGEND,
        HISTORICAL_FACT,
        PERSONAL_STORY,
        MYTH,
        ORAL_TRADITION,
        ACADEMIC,
        NARRATIVE,
        DIALOGUE
    }
    
    public enum TargetAudience {
        CHILDREN,
        TEENS,
        ADULTS,
        SCHOLARS,
        TOURISTS,
        STUDENTS,
        GENERAL_PUBLIC
    }
    
    public enum EmotionalTone {
        INSPIRING,
        EDUCATIONAL,
        MYSTERIOUS,
        JOYFUL,
        SOLEMN,
        CONTEMPLATIVE,
        EXCITING,
        PEACEFUL
    }
    
    public enum VerificationStatus {
        VERIFIED,
        PENDING,
        REJECTED,
        NEEDS_REVIEW,
        DRAFT
    }
    
    public enum SourceReliability {
        HIGH,
        MEDIUM,
        LOW,
        UNKNOWN
    }
    
    public enum KnowledgeStatus {
        ACTIVE,
        INACTIVE,
        ARCHIVED,
        DRAFT,
        UNDER_REVIEW
    }
    
    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = KnowledgeStatus.DRAFT;
        }
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void incrementAccess() {
        this.timesAccessed++;
        this.lastAccessed = LocalDateTime.now();
    }
    
    public void addRating(Double rating) {
        if (this.totalRatings == 0) {
            this.averageRating = rating;
        } else {
            this.averageRating = ((this.averageRating * this.totalRatings) + rating) / (this.totalRatings + 1);
        }
        this.totalRatings++;
    }
    
    public void incrementShares() {
        this.shareCount++;
    }
    
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }
    
    public boolean isActive() {
        return status == KnowledgeStatus.ACTIVE;
    }
}
