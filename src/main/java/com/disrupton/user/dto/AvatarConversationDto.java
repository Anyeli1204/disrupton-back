package com.disrupton.dto;

import lombok.Data;
import com.google.cloud.Timestamp;

/**
 * DTO simplificado para Conversaciones con Avatar usando Gemini API
 */
@Data
public class AvatarConversationDto {
    
    // Identificación
    private String conversationId;
    private String avatarId;
    private String userId;
    
    // Contenido de la conversación
    private String messageType; // USER_MESSAGE, AVATAR_RESPONSE
    private String userMessage;
    private String geminiResponse;
    
    // Metadatos
    private Timestamp timestamp;
}
