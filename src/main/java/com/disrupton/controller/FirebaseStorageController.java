package com.disrupton.controller;

import com.disrupton.service.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/firebase/storage")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FirebaseStorageController {
    
    private final FirebaseStorageService storageService;
    
    /**
     * Subir modelo 3D
     */
    @PostMapping("/upload-model")
    public ResponseEntity<Map<String, Object>> uploadModel3D(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("modelId") String modelId) {
        
        try {
            log.info("📁 Subiendo modelo 3D: {} para usuario: {}", file.getOriginalFilename(), userId);
            
            String downloadUrl = storageService.uploadModel3D(file, userId, modelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("downloadUrl", downloadUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al subir modelo 3D: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Subir thumbnail
     */
    @PostMapping("/upload-thumbnail")
    public ResponseEntity<Map<String, Object>> uploadThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("modelId") String modelId) {
        
        try {
            log.info("🖼️ Subiendo thumbnail: {} para usuario: {}", file.getOriginalFilename(), userId);
            
            String downloadUrl = storageService.uploadThumbnail(file, userId, modelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("downloadUrl", downloadUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al subir thumbnail: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Subir múltiples imágenes para procesamiento
     */
    @PostMapping("/upload-images-for-processing")
    public ResponseEntity<Map<String, Object>> uploadImagesForProcessing(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("userId") String userId,
            @RequestParam("modelId") String modelId) {
        
        try {
            log.info("📸 Subiendo {} imágenes para procesamiento", files.length);
            
            String downloadUrls = storageService.uploadImagesForProcessing(files, userId, modelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("downloadUrls", downloadUrls.split(","));
            response.put("fileCount", files.length);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al subir imágenes para procesamiento: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Eliminar archivo
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("filePath") String filePath) {
        
        try {
            log.info("🗑️ Eliminando archivo: {}", filePath);
            
            boolean deleted = storageService.deleteFile(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("filePath", filePath);
            
            if (deleted) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Obtener URL pública de un archivo
     */
    @GetMapping("/public-url")
    public ResponseEntity<Map<String, Object>> getPublicUrl(@RequestParam("filePath") String filePath) {
        
        try {
            log.info("🔗 Obteniendo URL pública para: {}", filePath);
            
            String publicUrl = storageService.getPublicUrl(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", publicUrl != null);
            response.put("publicUrl", publicUrl);
            response.put("filePath", filePath);
            
            if (publicUrl != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            log.error("❌ Error al obtener URL pública: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Verificar si un archivo existe
     */
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> fileExists(@RequestParam("filePath") String filePath) {
        
        try {
            log.info("🔍 Verificando existencia de archivo: {}", filePath);
            
            boolean exists = storageService.fileExists(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            response.put("filePath", filePath);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al verificar archivo: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 