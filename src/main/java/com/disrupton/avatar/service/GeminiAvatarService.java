package com.disrupton.avatar.service;

import com.disrupton.avatar.model.Avatar;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAvatarService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.microservice.url:http://localhost:5001}")
    private String geminiServiceUrl;
    
   
    public String processUserMessage(Avatar avatar, String userMessage) {
        try {
            log.info("Procesando mensaje para avatar {}: {}", avatar.getType(), userMessage);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("avatarType", avatar.getType().toString());
            requestBody.put("message", userMessage);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String url = geminiServiceUrl + "/chat";
            log.debug("Enviando request al microservicio Gemini: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                if (jsonResponse.get("success").asBoolean()) {
                    String geminiResponse = jsonResponse.get("response").asText();
                    return geminiResponse;
                } else {
                    String error = jsonResponse.has("error") ? jsonResponse.get("error").asText() : "Error desconocido";
                    return generateFallbackResponse(avatar.getType().toString(), userMessage);
                }
            } else {
                return generateFallbackResponse(avatar.getType().toString(), userMessage);
            }
            
        } catch (Exception e) {
            return generateFallbackResponse(avatar.getType().toString(), userMessage);
        }
    }
    
    public boolean isServiceHealthy() {
        try {
            String url = geminiServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                boolean isHealthy = jsonResponse.get("status").asText().equals("healthy");
                log.info("Estado del microservicio Gemini: {}", isHealthy ? "Saludable" : "No saludable");
                return isHealthy;
            } else {
                log.warn("Microservicio Gemini no responde correctamente: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error verificando salud del microservicio Gemini: {}", e.getMessage());
            return false;
        }
    }
    
    private String generateFallbackResponse(String avatarType, String userMessage) {
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
