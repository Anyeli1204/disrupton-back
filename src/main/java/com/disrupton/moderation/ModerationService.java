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
    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public boolean isCommentSafe(String comment) throws Exception {
        String prompt = "RECHAZA si contiene: lenguaje ofensivo, palabras soeces, agresividad, contenido sexual/violento, " +
                "discriminación, odio cultural (ej: \"detesto\", \"horrible\", \"primitivo\"), spam o insultos. En caso contrario APRUEBALO." +
                "Responde solo con 'APROBADO' o 'RECHAZADO'.\n\nComentario: " + comment;
        String response = callGemini(prompt);
        return response.trim().toUpperCase().contains("APROBADO");
    }

    public String getReasonIfUnsafe(String comment) throws Exception {
        String prompt = "Indica por qué el siguiente comentario puede ser inapropiado. Si es seguro, responde 'APROBADO'.\n\nComentario: " + comment;
        return callGemini(prompt);
    }

    private String callGemini(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-goog-api-key", apiKey);

        String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"","\\\"") + "\"}]}]}";
        conn.getOutputStream().write(body.getBytes());

        try (Scanner sc = new Scanner(conn.getInputStream())) {
            StringBuilder resp = new StringBuilder();
            while (sc.hasNext()) resp.append(sc.nextLine());
            JsonObject obj = JsonParser.parseString(resp.toString()).getAsJsonObject();
            JsonArray cand = obj.getAsJsonArray("candidates");
            if (cand != null && cand.size() > 0) {
                return cand.get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            }
        }
        return "❌ Sin respuesta";
    }
}

