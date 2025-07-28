package com.disrupton.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad AvatarSession - Representa una sesión de interacción con un avatar
 */
@Entity
@Table(name = "avatar_sessions")
@Data
public class AvatarSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String sessionId;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Información temporal
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column
    private LocalDateTime endTime;
    
    @Column
    private Long durationSeconds;
    
    // Contexto de la sesión
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType;
    
    @Column(length = 100)
    private String campusZone;
    
    @Column(length = 200)
    private String culturalTheme;
    
    @ElementCollection
    @CollectionTable(name = "session_cultural_objects", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "cultural_object_id")
    private List<String> culturalObjectsExplored;
    
    // Información del dispositivo
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
    
    @Column(length = 100)
    private String appVersion;
    
    @Column(length = 100)
    private String deviceModel;
    
    @Column(length = 100)
    private String operatingSystem;
    
    // Ubicación geográfica
    @Column
    private Double startLatitude;
    
    @Column
    private Double startLongitude;
    
    @Column
    private Double endLatitude;
    
    @Column
    private Double endLongitude;
    
    // Estadísticas de interacción
    @Column(nullable = false)
    private Integer totalMessages = 0;
    
    @Column(nullable = false)
    private Integer userQuestions = 0;
    
    @Column(nullable = false)
    private Integer avatarResponses = 0;
    
    @Column(nullable = false)
    private Integer storiesTold = 0;
    
    @Column(nullable = false)
    private Integer recommendationsMade = 0;
    
    // Contenido multimedia
    @Column(nullable = false)
    private Integer imagesShown = 0;
    
    @Column(nullable = false)
    private Integer videosPlayed = 0;
    
    @Column(nullable = false)
    private Integer audioClipsPlayed = 0;
    
    @Column(nullable = false)
    private Integer model3DViewed = 0;
    
    // Idiomas
    @ElementCollection
    @CollectionTable(name = "session_languages", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "language")
    private List<String> languagesUsed;
    
    @Column(nullable = false)
    private Integer translationsPerformed = 0;
    
    @Column(length = 10)
    private String primaryLanguage = "es";
    
    // Evaluación
    @Column
    private Double userSatisfactionScore;
    
    @Enumerated(EnumType.STRING)
    private SessionRating sessionRating;
    
    @Column(length = 1000)
    private String userFeedback;
    
    @Column(nullable = false)
    private Boolean sessionCompleted = false;
    
    // Aprendizaje
    @ElementCollection
    @CollectionTable(name = "session_learning_objectives", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "objective")
    private List<String> learningObjectivesAchieved;
    
    @ElementCollection
    @CollectionTable(name = "session_cultural_concepts", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "concept")
    private List<String> culturalConceptsLearned;
    
    @Column
    private Integer knowledgeLevelBefore;
    
    @Column
    private Integer knowledgeLevelAfter;
    
    // Modo de realidad
    @Column(nullable = false)
    private Boolean arModeUsed = false;
    
    @Column(nullable = false)
    private Boolean vrModeUsed = false;
    
    // Interacciones sociales
    @Column(nullable = false)
    private Boolean sharedContent = false;
    
    @Column(nullable = false)
    private Integer likesGiven = 0;
    
    @Column(nullable = false)
    private Integer commentsPosted = 0;
    
    // Estado y problemas
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus sessionStatus;
    
    @Column
    private LocalDateTime lastActivityTime;
    
    @Column(length = 200)
    private String endReason;
    
    @Column(nullable = false)
    private Boolean connectionIssues = false;
    
    @Column(nullable = false)
    private Boolean audioIssues = false;
    
    @Column(nullable = false)
    private Boolean visualIssues = false;
    
    @Column(nullable = false)
    private Boolean dataProcessed = false;
    
    // Relaciones
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<AvatarConversation> conversations;
    
    // Enums
    public enum SessionType {
        CULTURAL_TOUR,
        Q_A_SESSION,
        STORYTELLING,
        OBJECT_EXPLORATION,
        LEARNING_MODULE,
        FREE_CONVERSATION,
        GUIDED_EXPERIENCE,
        QUIZ_SESSION
    }
    
    public enum DeviceType {
        MOBILE,
        TABLET,
        WEB,
        AR_GLASSES,
        VR_HEADSET,
        SMART_TV,
        KIOSK
    }
    
    public enum SessionRating {
        EXCELLENT,
        GOOD,
        AVERAGE,
        POOR,
        TERRIBLE
    }
    
    public enum SessionStatus {
        ACTIVE,
        COMPLETED,
        INTERRUPTED,
        ERROR,
        PAUSED,
        TIMEOUT
    }
    
    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (sessionStatus == null) {
            sessionStatus = SessionStatus.ACTIVE;
        }
        lastActivityTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastActivityTime = LocalDateTime.now();
        if (endTime != null && durationSeconds == null) {
            calculateDuration();
        }
    }
    
    public void endSession(String reason) {
        this.endTime = LocalDateTime.now();
        this.endReason = reason;
        this.sessionStatus = SessionStatus.COMPLETED;
        calculateDuration();
    }
    
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }
    
    public void updateActivity() {
        this.lastActivityTime = LocalDateTime.now();
    }
    
    public void incrementMessages() {
        this.totalMessages++;
    }
    
    public void incrementUserQuestions() {
        this.userQuestions++;
    }
    
    public void incrementAvatarResponses() {
        this.avatarResponses++;
    }
}
