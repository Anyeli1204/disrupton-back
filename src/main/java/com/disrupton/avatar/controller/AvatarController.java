package com.disrupton.avatar.controller;

import com.disrupton.avatar.dto.AvatarDto;
import com.disrupton.avatar.model.Avatar;
import com.disrupton.avatar.service.AvatarService;
import com.disrupton.avatar.service.GeminiAvatarService;
import com.disrupton.avatar.service.AvatarStorageService;
import com.disrupton.storage.service.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AvatarController {

    private final AvatarService avatarService;
    private final GeminiAvatarService geminiAvatarService;
    private final FirebaseStorageService firebaseStorageService;
    private final AvatarStorageService avatarStorageService;

    /**
     * Obtener todos los avatares disponibles (solo 3 tipos)
     */
    @GetMapping
    public ResponseEntity<List<AvatarDto>> getAllAvatars() {
        try {
            List<AvatarDto> avatars = avatarService.getAllAvatars();
            return ResponseEntity.ok(avatars);
        } catch (Exception e) {
            log.error("Error getting all avatars: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener avatar específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AvatarDto> getAvatarById(@PathVariable String id) {
        try {
            AvatarDto avatar = avatarService.getAvatarById(id);
            if (avatar != null) {
                return ResponseEntity.ok(avatar);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting avatar by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Procesar mensaje del usuario con el avatar usando Gemini API
     */
    @PostMapping("/{id}/chat")
    public ResponseEntity<Map<String, Object>> chatWithAvatar(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "El mensaje no puede estar vacío"
                ));
            }

            AvatarDto avatarDto = avatarService.getAvatarById(id);
            if (avatarDto == null) {
                return ResponseEntity.notFound().build();
            }

            // Convert AvatarDto to Avatar for Gemini service
            Avatar avatar = new Avatar();
            avatar.setAvatarId(avatarDto.getAvatarId());
            avatar.setType(Avatar.AvatarType.valueOf(avatarDto.getType()));
            avatar.setAvatar3DModelUrl(avatarDto.getAvatar3DModelUrl());

            String response = geminiAvatarService.processUserMessage(avatar, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "avatarId", id,
                "avatarType", avatar.getType().toString(),
                "userMessage", message,
                "response", response,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error in avatar chat: {}", e.getMessage(), e);
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
            log.error("Error getting avatar types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener URLs de modelos 3D de ejemplo (para desarrollo)
     */
    @GetMapping("/3d-models/example-urls")
    public ResponseEntity<Map<String, Object>> getExample3DModelUrls() {
        try {
            log.info("Generando URLs de ejemplo para modelos 3D...");
            
            Map<String, String> exampleUrls = new HashMap<>();
            
            // URLs de modelos 3D de ejemplo (cubos básicos)
            exampleUrls.put("vicuna", "https://storage.googleapis.com/disrupton-new.firebasestorage.app/cultural-app-assets/avatars/3d/vicuna.glb");
            exampleUrls.put("peruvian_dog", "https://storage.googleapis.com/disrupton-new.firebasestorage.app/cultural-app-assets/avatars/3d/peruvian_dog.glb");
            exampleUrls.put("cock_of_the_rock", "https://storage.googleapis.com/disrupton-new.firebasestorage.app/cultural-app-assets/avatars/3d/cock_of_the_rock.glb");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "URLs de ejemplo para modelos 3D",
                "exampleUrls", exampleUrls,
                "note", "Para usar modelos 3D reales, sube archivos .glb a Firebase Storage en estas rutas"
            ));
            
        } catch (Exception e) {
            log.error("Error generando URLs de ejemplo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * Sube un archivo GLB para un avatar específico
     */
    @PostMapping("/{avatarId}/upload-glb")
    public ResponseEntity<Map<String, Object>> uploadAvatarGLB(
            @PathVariable String avatarId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            log.info("🎭 Subiendo archivo GLB para avatar: {} - Archivo: {}", avatarId, file.getOriginalFilename());
            
            // Obtener el avatar para saber su nombre
            AvatarDto avatar = avatarService.getAvatarById(avatarId);
            if (avatar == null) {
                return ResponseEntity.ok().body(Map.of(
                    "success", false,
                    "error", "Avatar no encontrado con ID: " + avatarId
                ));
            }
            
            // Subir el archivo GLB usando el nombre del avatar
            String downloadUrl = avatarStorageService.uploadAvatarGLB(file, avatar.getDisplayName().toLowerCase());
            
            // Actualizar el avatar con la nueva URL
            avatar.setAvatar3DModelUrl(downloadUrl);
            avatarService.updateAvatar(avatar);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Archivo GLB subido y avatar actualizado exitosamente",
                "avatarId", avatarId,
                "avatarName", avatar.getDisplayName(),
                "downloadUrl", downloadUrl,
                "fileName", file.getOriginalFilename(),
                "fileSize", file.getSize()
            ));
            
        } catch (Exception e) {
            log.error("❌ Error subiendo archivo GLB para avatar: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al subir archivo GLB: " + e.getMessage()
            ));
        }
    }

    /**
     * Sube un archivo GLB desde una ruta local (para migración)
     */
    @PostMapping("/{avatarId}/upload-glb-from-local")
    public ResponseEntity<Map<String, Object>> uploadAvatarGLBFromLocal(
            @PathVariable String avatarId,
            @RequestParam("localFilePath") String localFilePath) {
        
        try {
            log.info("🎭 Subiendo archivo GLB desde local para avatar: {} - Ruta: {}", avatarId, localFilePath);
            
            // Obtener el avatar para saber su nombre
            AvatarDto avatar = avatarService.getAvatarById(avatarId);
            if (avatar == null) {
                return ResponseEntity.ok().body(Map.of(
                    "success", false,
                    "error", "Avatar no encontrado con ID: " + avatarId
                ));
            }
            
            // Subir el archivo GLB desde la ruta local
            String downloadUrl = avatarStorageService.uploadAvatarFromLocalPath(localFilePath, avatar.getDisplayName().toLowerCase());
            
            // Actualizar el avatar con la nueva URL
            avatar.setAvatar3DModelUrl(downloadUrl);
            avatarService.updateAvatar(avatar);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Archivo GLB subido desde local y avatar actualizado exitosamente",
                "avatarId", avatarId,
                "avatarName", avatar.getDisplayName(),
                "downloadUrl", downloadUrl,
                "localFilePath", localFilePath
            ));
            
        } catch (Exception e) {
            log.error("❌ Error subiendo archivo GLB desde local para avatar: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Error al subir archivo GLB desde local: " + e.getMessage()
            ));
        }
    }

    /**
     * Lista todos los archivos GLB disponibles en Storage
     */
    @GetMapping("/glb-files/list")
    public ResponseEntity<Map<String, Object>> listAvailableGLBFiles() {
        try {
            log.info("📋 Listando archivos GLB disponibles en Storage");
            
            String[] avatarNames = avatarStorageService.listAvailableAvatars();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "totalFiles", avatarNames.length,
                "avatarNames", avatarNames,
                "message", "Archivos GLB disponibles en Firebase Storage"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error listando archivos GLB: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al listar archivos GLB: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtiene información de un archivo GLB específico
     */
    @GetMapping("/glb-files/info/{avatarName}")
    public ResponseEntity<Map<String, Object>> getGLBFileInfo(@PathVariable String avatarName) {
        try {
            log.info("ℹ️ Obteniendo información del archivo GLB: {}", avatarName);
            
            AvatarStorageService.AvatarInfo info = avatarStorageService.getAvatarInfo(avatarName);
            
            if (info != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "avatarInfo", info
                ));
            } else {
                return ResponseEntity.ok().body(Map.of(
                    "success", false,
                    "error", "Archivo GLB no encontrado: " + avatarName
                ));
            }
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo información del archivo GLB: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Error al obtener información del archivo GLB: " + e.getMessage()
            ));
        }
    }
}
