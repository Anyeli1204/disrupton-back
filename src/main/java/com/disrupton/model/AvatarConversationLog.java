package com.disrupton.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad AvatarConversationLog - Registro de conversaciones con Gemini API
 * Esta entidad simplificada reemplaza la anterior estructura compleja de conocimiento
 */
@Entity
@Table(name = "avatar_conversation_logs")
@Data
public class AvatarConversationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String conversationId;
    
    @Column(nullable = false)
    private String avatarId;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String geminiResponse;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
