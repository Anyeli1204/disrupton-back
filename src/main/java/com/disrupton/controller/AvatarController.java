package com.disrupton.controller;

import com.disrupton.dto.AvatarDto;
import com.disrupton.model.Avatar;
import com.disrupton.service.AvatarService;
import com.disrupton.service.GeminiAvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/avatars")
@CrossOrigin(origins = "*")
public class AvatarController {

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private GeminiAvatarService geminiAvatarService;

    /**
     * Obtener todos los avatares disponibles (solo 3 tipos)
     */
    @GetMapping
    public ResponseEntity<List<AvatarDto>> getAllAvatars() {
        try {
            List<Avatar> avatars = avatarService.getAllAvatars();
            List<AvatarDto> avatarDtos = avatars.stream()
                .map(this::convertToDto)
                .toList();
            return ResponseEntity.ok(avatarDtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener avatar específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AvatarDto> getAvatarById(@PathVariable Long id) {
        try {
            Optional<Avatar> avatar = avatarService.getAvatarById(id);
            return avatar.map(a -> ResponseEntity.ok(convertToDto(a)))
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Procesar mensaje del usuario con el avatar usando Gemini API
     */
    @PostMapping("/{id}/chat")
    public ResponseEntity<Map<String, Object>> chatWithAvatar(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "El mensaje no puede estar vacío"
                ));
            }

            Optional<Avatar> avatar = avatarService.getAvatarById(id);
            if (avatar.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            String response = geminiAvatarService.processUserMessage(avatar.get(), message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "avatarId", id,
                "avatarType", avatar.get().getType().toString(),
                "userMessage", message,
                "response", response,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtener información básica de todos los tipos de avatares disponibles
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getAvatarTypes() {
        try {
            List<Map<String, Object>> types = List.of(
                Map.of(
                    "type", "VICUNA",
                    "name", "Vicuña Andina",
                    "description", "Representante de la fauna andina peruana, símbolo de elegancia y resistencia."
                ),
                Map.of(
                    "type", "PERUVIAN_DOG", 
                    "name", "Perro Peruano",
                    "description", "Guardián ancestral sin pelo, compañero leal de las culturas preincaicas."
                ),
                Map.of(
                    "type", "COCK_OF_THE_ROCK",
                    "name", "Gallito de las Rocas", 
                    "description", "Ave nacional del Perú, símbolo de belleza y orgullo patrio."
                )
            );
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Convierte una entidad Avatar a DTO
     */
    private AvatarDto convertToDto(Avatar avatar) {
        AvatarDto dto = new AvatarDto();
        dto.setId(avatar.getId());
        dto.setType(avatar.getType());
        dto.setModel3dUrl(avatar.getModel3dUrl());
        return dto;
    }
}
