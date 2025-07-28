package com.disrupton.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad AvatarConversation - Representa una conversación entre usuario y avatar
 */
@Entity
@Table(name = "avatar_conversations")
@Data
public class AvatarConversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private AvatarSession session;
    
    // Contenido del mensaje
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(length = 10)
    private String language = "es";
    
    @Column(columnDefinition = "TEXT")
    private String translatedContent;
    
    // Contexto cultural
    @Column(length = 200)
    private String culturalTopic;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultural_object_id")
    private CulturalObject culturalObject;
    
    @Column(length = 100)
    private String culturalRegion;
    
    // Metadatos de interacción
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private MessageSource messageSource;
    
    @Enumerated(EnumType.STRING)
    private ResponseType responseType;
    
    @Column(length = 50)
    private String sentimentScore; // POSITIVE, NEGATIVE, NEUTRAL
    
    // Multimedia
    @Column(length = 500)
    private String audioUrl;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(length = 500)
    private String videoUrl;
    
    @Column(length = 100)
    private String attachmentType;
    
    // Ubicación
    @Column(length = 100)
    private String campusLocation;
    
    @Column
    private Double latitude;
    
    @Column
    private Double longitude;
    
    // Estado del mensaje
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private Boolean isArchived = false;
    
    @Column(nullable = false)
    private Integer messageOrder = 0;
    
    // Calidad de interacción
    @Column
    private Double userSatisfaction;
    
    @Column
    private Boolean wasHelpful;
    
    @Column(length = 500)
    private String feedback;
    
    // Análisis de IA
    @Column(length = 200)
    private String intentDetected;
    
    @Column
    private Double confidenceScore;
    
    @Column(length = 500)
    private String entityExtracted;
    
    @Column(length = 200)
    private String nextSuggestedAction;
    
    // Enums
    public enum MessageType {
        USER_MESSAGE,
        AVATAR_RESPONSE,
        SYSTEM_MESSAGE,
        AUTOMATED_RESPONSE,
        FALLBACK_RESPONSE
    }
    
    public enum MessageSource {
        TEXT,
        VOICE,
        GESTURE,
        SELECTION,
        QUICK_REPLY
    }
    
    public enum ResponseType {
        INFORMATIVE,
        STORYTELLING,
        QUESTION,
        RECOMMENDATION,
        GREETING,
        FAREWELL,
        CLARIFICATION,
        EDUCATIONAL
    }
    
    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (language == null) {
            language = "es";
        }
    }
    
    public boolean isFromUser() {
        return messageType == MessageType.USER_MESSAGE;
    }
    
    public boolean isFromAvatar() {
        return messageType == MessageType.AVATAR_RESPONSE;
    }
    
    public boolean hasMultimedia() {
        return audioUrl != null || imageUrl != null || videoUrl != null;
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
    
    public void archive() {
        this.isArchived = true;
    }
}
