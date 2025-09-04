package com.disrupton.socialPost.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import com.disrupton.socialPost.service.SocialPostImageService;
import com.disrupton.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social/upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SocialPostImageController {
    
    private final SocialPostImageService socialPostImageService;
    
    /**
     * Subir imágenes para posts sociales
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en uploadImages");
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no autenticado"
            ));
        }
        
        log.info("📸 Uploading {} images for user: {}", images.size(), currentUser.getUserId());
        
        try {
            // Validar número máximo de imágenes (máximo 10)
            if (images.size() > 10) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No se pueden subir más de 10 imágenes por publicación",
                        "maxImages", 10
                ));
            }
            
            // Validar que se envíen imágenes
            if (images.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Debe seleccionar al menos una imagen"
                ));
            }
            
            List<String> imageUrls = socialPostImageService.uploadPostImages(images, currentUser.getUserId());
            
            log.info("✅ Successfully uploaded {} images for user: {}", imageUrls.size(), currentUser.getUserId());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imágenes subidas exitosamente",
                    "imageUrls", imageUrls,
                    "count", imageUrls.size()
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid image upload request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
            
        } catch (IOException e) {
            log.error("❌ Failed to upload images for user: {}, error: {}", currentUser.getUserId(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor al subir las imágenes: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Subir una sola imagen
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Map<String, Object>> uploadSingleImage(
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en uploadSingleImage");
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Usuario no autenticado"
            ));
        }
        
        log.info("📸 Uploading single image for user: {}", currentUser.getUserId());
        
        try {
            // Validar que se envíe una imagen
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Debe seleccionar una imagen"
                ));
            }
            
            String imageUrl = socialPostImageService.uploadSingleImage(image, currentUser.getUserId());
            
            log.info("✅ Successfully uploaded single image for user: {}", currentUser.getUserId());
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Imagen subida exitosamente",
                    "imageUrl", imageUrl
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid image upload request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
            
        } catch (IOException e) {
            log.error("❌ Failed to upload image for user: {}, error: {}", currentUser.getUserId(), e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor al subir la imagen: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Obtener límites de upload
     */
    @GetMapping("/limits")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Map<String, Object>> getUploadLimits() {
        return ResponseEntity.ok(Map.of(
                "maxImages", 10,
                "maxFileSize", "10MB",
                "allowedFormats", List.of("JPEG", "JPG", "PNG", "WEBP"),
                "maxFileSizeBytes", 10 * 1024 * 1024
        ));
    }
}
