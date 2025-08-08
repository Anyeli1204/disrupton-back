package com.disrupton.avatar;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para registrar conversaciones entre usuarios y avatares
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarConversationLog {
    
    // Identificación
    private String id;
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
