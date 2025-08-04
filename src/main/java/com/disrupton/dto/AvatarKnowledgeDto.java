package com.disrupton.dto;

import lombok.Data;
import com.google.cloud.Timestamp;

/**
 * DTO simplificado para Conocimiento Cultural del Avatar usando Gemini API
 */
@Data
public class AvatarKnowledgeDto {
    
    // Identificación
    private String requestId;
    private String avatarId;
    
    // Consulta del usuario
    private String userQuery;
    
    // Respuesta de Gemini
    private String geminiResponse;
    
    // Metadatos
    private Timestamp timestamp;
}
}
