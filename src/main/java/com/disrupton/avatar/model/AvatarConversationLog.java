package com.disrupton.avatar.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Entidad AvatarConversationLog - Representa el historial de conversaciones con avatares
 * Adaptado para Firebase (NoSQL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarConversationLog {
    
    // Identificador único de la conversación (usado como document ID en Firebase)
    private String conversationId;
    
    // Referencias
    private String avatarId;
    private String userId;
    private String sessionId; // Para agrupar conversaciones de una sesión
    
    // Contenido de la conversación
    private String userMessage;
    private String avatarResponse;
    
    // Metadatos de Gemini
    private String geminiModel;
    private Double temperature;
    private Integer tokensUsed;
    private String finishReason;
    
    // Información contextual
    private String avatarType; // VICUNA, PERUVIAN_DOG, COCK_OF_THE_ROCK
    private String culturalContext; // Contexto cultural específico
    
    // Métricas
    private Long responseTimeMs;
    private Boolean isSuccessful;
    private String errorMessage;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Método para establecer timestamps antes de guardar
     */
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        
        if (isSuccessful == null) {
            isSuccessful = true;
        }
    }
    
    /**
     * Método para actualizar timestamp de modificación
     */
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca la conversación como fallida
     */
    public void markAsFailed(String errorMessage) {
        this.isSuccessful = false;
        this.errorMessage = errorMessage;
        onUpdate();
    }
}
