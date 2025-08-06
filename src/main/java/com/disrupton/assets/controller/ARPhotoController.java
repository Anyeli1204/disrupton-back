package com.disrupton.assets.controller;

import com.disrupton.assets.dto.ARPhotoDto;
import com.disrupton.assets.service.FirebaseARPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/firebase/cultural")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ARPhotoController {

    private final FirebaseARPhotoService arPhotoService;

    /**
     * Crear nueva foto AR
     */
    @PostMapping("/ar-photos")
    public ResponseEntity<ARPhotoDto> createARPhoto(@RequestBody ARPhotoDto arPhoto) {
        try {
            log.info("📸 Creando nueva foto AR para objeto: {}", arPhoto.getObjectId());
            ARPhotoDto savedPhoto = arPhotoService.saveARPhoto(arPhoto);
            return ResponseEntity.ok(savedPhoto);
        } catch (Exception e) {
            log.error("❌ Error al crear foto AR: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener fotos AR por objeto cultural
     */
    @GetMapping("/ar-photos/object/{objectId}")
    public ResponseEntity<List<ARPhotoDto>> getARPhotosByObject(@PathVariable String objectId) {
        try {
            log.info("📸 Obteniendo fotos AR para objeto: {}", objectId);
            List<ARPhotoDto> photos = arPhotoService.getARPhotosByObjectId(objectId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("❌ Error al obtener fotos AR: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener fotos AR por usuario
     */
    @GetMapping("/ar-photos/user/{userId}")
    public ResponseEntity<List<ARPhotoDto>> getARPhotosByUser(@PathVariable String userId) {
        try {
            log.info("📸 Obteniendo fotos AR del usuario: {}", userId);
            List<ARPhotoDto> photos = arPhotoService.getARPhotosByUserId(userId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            log.error("❌ Error al obtener fotos AR: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar foto AR
     */
    @DeleteMapping("/ar-photos/{photoId}")
    public ResponseEntity<?> deleteARPhoto(@PathVariable String photoId) {
        try {
            log.info("🗑️ Eliminando foto AR: {}", photoId);
            boolean deleted = arPhotoService.deleteARPhoto(photoId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar foto AR: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 