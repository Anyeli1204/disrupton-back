package com.disrupton.avatar.service;

import com.disrupton.avatar.model.Avatar;
import org.springframework.stereotype.Service;

/**
 * Servicio simplificado para integrar con Gemini API
 * Los avatares no tienen conocimiento propio, todo viene de Gemini
 */
@Service
public class GeminiAvatarService {
    
    /**
     * Procesa un mensaje del usuario y obtiene respuesta de Gemini API
     */
    public String processUserMessage(Avatar avatar, String userMessage) {
        
        // Aquí integrarías con Gemini API real
        String geminiResponse = generateSimulatedResponse(avatar.getType().toString(), userMessage);
        
        return geminiResponse;
    }
    
    /**
     * Simulación de respuesta de Gemini (reemplazar con integración real)
     */
    private String generateSimulatedResponse(String avatarType, String userMessage) {
        String baseResponse = "";
        
        switch (avatarType) {
            case "VICUNA":
                baseResponse = "Como una vicuña de los Andes peruanos, te puedo decir que ";
                break;
            case "PERUVIAN_DOG":
                baseResponse = "Como un perro peruano sin pelo, guardián ancestral, te explico que ";
                break;
            case "COCK_OF_THE_ROCK":
                baseResponse = "Como el gallito de las rocas, ave nacional del Perú, te comento que ";
                break;
            default:
                baseResponse = "Como representante de la cultura peruana, ";
        }
        
        // Respuesta genérica basada en palabras clave
        if (userMessage.toLowerCase().contains("historia")) {
            return baseResponse + "la historia del Perú es fascinante, con civilizaciones milenarias como los Incas.";
        } else if (userMessage.toLowerCase().contains("comida") || userMessage.toLowerCase().contains("gastronomía")) {
            return baseResponse + "la gastronomía peruana es una de las más reconocidas del mundo.";
        } else if (userMessage.toLowerCase().contains("cultura")) {
            return baseResponse + "la cultura peruana es muy rica y diversa, con tradiciones ancestrales.";
        } else {
            return baseResponse + "el Perú es un país increíble con mucha historia y cultura para compartir.";
        }
    }
    
    /**
     * Obtiene el historial de conversación
     */
    public String getConversationHistory(String avatarId, String userId, int limit) {
        // Implementación simplificada - en producción consultaría la base de datos
        return "Historial de conversación simulado para avatar: " + avatarId + " y usuario: " + userId;
    }
    
    /**
     * Obtiene el conteo de uso del avatar
     */
    public Long getAvatarUsageCount(String avatarId) {
        // Implementación simplificada - en producción consultaría métricas reales
        return 0L;
    }
}
