package com.disrupton.service;

import com.disrupton.dto.AvatarDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Servicio simplificado para integrar con Gemini API
 * Los avatares no tienen conocimiento propio, todo viene de Gemini
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAvatarService {

    private final AvatarService avatarService;
    
    /**
     * Procesa un mensaje del usuario y obtiene respuesta de Gemini API
     */
    public Map<String, Object> processUserMessage(String avatarId, String userId, String userMessage) 
            throws ExecutionException, InterruptedException {
        
        // Obtener información del avatar para personalizar el contexto
        AvatarDto avatar = avatarService.getAvatarById(avatarId);
        
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar no encontrado con ID: " + avatarId);
        }
        
        // Aquí integrarías con Gemini API real
        String geminiResponse = generateSimulatedResponse(avatar.getType(), userMessage);
        
        // Retornar respuesta simple
        Map<String, Object> response = new HashMap<>();
        response.put("avatarId", avatarId);
        response.put("avatarType", avatar.getType());
        response.put("avatarName", avatar.getDisplayName());
        response.put("userMessage", userMessage);
        response.put("response", geminiResponse);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
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
}
