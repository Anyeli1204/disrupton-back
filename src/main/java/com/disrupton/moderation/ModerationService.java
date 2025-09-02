package com.disrupton.moderation;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
@Service
@Slf4j
public class ModerationService {
    @Value("${gemini.service.url:http://localhost:5001}")
    private String geminiServiceUrl;

    public boolean isCommentSafe(String comment) throws Exception {
        log.info("Moderando comentario con servicio local: {}", comment.substring(0, Math.min(comment.length(), 50)));
        
        try {
            String response = callLocalModerationService(comment);
            log.info("Respuesta de moderación: {}", response);
            
            // Parsear la respuesta JSON
            JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();
            if (responseObj.has("result")) {
                String resultStr = responseObj.get("result").getAsString();
                
                // Limpiar el markdown si está presente
                if (resultStr.contains("```json")) {
                    resultStr = resultStr.replaceAll("```json\\s*", "").replaceAll("\\s*```", "").trim();
                }
                
                JsonObject result = JsonParser.parseString(resultStr).getAsJsonObject();
                
                if (result.has("esSeguro")) {
                    boolean esSeguro = result.get("esSeguro").getAsBoolean();
                    log.info("Comentario es seguro: {}", esSeguro);
                    return esSeguro;
                }
            }
            
            // Si hay algún problema parseando, aprobar por defecto
            log.warn("No se pudo parsear respuesta de moderación, aprobando por defecto");
            return true;
            
        } catch (Exception e) {
            log.error("Error en moderación, aprobando por defecto: {}", e.getMessage());
            return true; // Aprobar en caso de error
        }
    }

    public String getReasonIfUnsafe(String comment) throws Exception {
        try {
            String response = callLocalModerationService(comment);
            
            JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();
            if (responseObj.has("result")) {
                String resultStr = responseObj.get("result").getAsString();
                
                // Limpiar el markdown si está presente
                if (resultStr.contains("```json")) {
                    resultStr = resultStr.replaceAll("```json\\s*", "").replaceAll("\\s*```", "").trim();
                }
                
                JsonObject result = JsonParser.parseString(resultStr).getAsJsonObject();
                
                if (result.has("motivo")) {
                    return result.get("motivo").getAsString();
                }
            }
            
            return "No se pudo determinar el motivo";
            
        } catch (Exception e) {
            log.error("Error obteniendo motivo de moderación: {}", e.getMessage());
            return "Error en moderación";
        }
    }

    private String callLocalModerationService(String comment) throws Exception {
        URL url = new URL(geminiServiceUrl + "/moderate-comment");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Configurar la conexión
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        // Crear el JSON para enviar
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("text", comment);
        
        // Enviar la request
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.toString().getBytes());
            os.flush();
        }
        
        // Leer la respuesta
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                return response.toString();
            }
        } else {
            log.error("Error en servicio de moderación. Código: {}", responseCode);
            throw new Exception("Error en servicio de moderación: " + responseCode);
        }
    }
}

