package com.disrupton.storage.controller;

import com.disrupton.storage.dto.UploadResponse;
import com.disrupton.storage.model.StorageFile;
import com.disrupton.storage.service.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final FirebaseStorageService storageService;

    /**
     * Sube un modelo 3D a Firebase Storage
     */
    @PostMapping("/upload-model")
    public ResponseEntity<UploadResponse> uploadModel3D(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("modelId") String modelId) {
        
        try {
            log.info("📁 Subiendo modelo 3D: {} para usuario: {}", file.getOriginalFilename(), userId);
            
            StorageFile storageFile = storageService.uploadModel3D(file, userId, modelId);
            
            UploadResponse response = UploadResponse.success(
                storageFile.getFileName(),
                storageFile.getOriginalFileName(),
                storageFile.getFileSize(),
                storageFile.getDownloadUrl(),
                storageFile.getFilePath(),
                storageFile.getUserId(),
                storageFile.getModelId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al subir modelo 3D: {}", e.getMessage(), e);
            
            UploadResponse response = UploadResponse.error(
                e.getMessage(),
                file.getOriginalFilename(),
                file.getSize()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Sube una imagen miniatura (thumbnail)
     */
    @PostMapping("/upload-thumbnail")
    public ResponseEntity<UploadResponse> uploadThumbnail(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("modelId") String modelId) {
        
        try {
            log.info("🖼️ Subiendo thumbnail: {} para usuario: {}", file.getOriginalFilename(), userId);
            
            StorageFile storageFile = storageService.uploadThumbnail(file, userId, modelId);
            
            UploadResponse response = UploadResponse.success(
                storageFile.getFileName(),
                storageFile.getOriginalFileName(),
                storageFile.getFileSize(),
                storageFile.getDownloadUrl(),
                storageFile.getFilePath(),
                storageFile.getUserId(),
                storageFile.getModelId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al subir thumbnail: {}", e.getMessage(), e);
            
            UploadResponse response = UploadResponse.error(
                e.getMessage(),
                file.getOriginalFilename(),
                file.getSize()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Sube múltiples imágenes para procesamiento
     */
    @PostMapping("/upload-processing-images")
    public ResponseEntity<Map<String, Object>> uploadProcessingImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("userId") String userId,
            @RequestParam("modelId") String modelId) {
        
        try {
            log.info("📸 Subiendo {} imágenes para procesamiento", files.length);
            
            StorageFile[] storageFiles = storageService.uploadImagesForProcessing(files, userId, modelId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imágenes subidas para procesamiento");
            response.put("filesCount", files.length);
            response.put("userId", userId);
            response.put("modelId", modelId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al subir imágenes para procesamiento: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("filesCount", files.length);
            response.put("userId", userId);
            response.put("modelId", modelId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Elimina un archivo de Firebase Storage
     */
    @DeleteMapping("/delete-file")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("filePath") String filePath) {
        try {
            log.info("🗑️ Eliminando archivo: {}", filePath);
            
            boolean deleted = storageService.deleteFile(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Archivo eliminado exitosamente" : "Archivo no encontrado");
            response.put("filePath", filePath);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("filePath", filePath);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Obtiene la URL pública de un archivo
     */
    @GetMapping("/public-url")
    public ResponseEntity<Map<String, Object>> getPublicUrl(@RequestParam("filePath") String filePath) {
        try {
            String publicUrl = storageService.getPublicUrl(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", publicUrl != null);
            response.put("publicUrl", publicUrl);
            response.put("filePath", filePath);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener URL pública: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("filePath", filePath);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Verifica si un archivo existe
     */
    @GetMapping("/file-exists")
    public ResponseEntity<Map<String, Object>> fileExists(@RequestParam("filePath") String filePath) {
        try {
            boolean exists = storageService.fileExists(filePath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            response.put("filePath", filePath);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al verificar archivo: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("filePath", filePath);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint de prueba para verificar la conexión
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            log.info("🔍 Probando conexión con Firebase Storage...");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conexión con Firebase Storage exitosa");
            response.put("bucket", "disrupton-new.firebasestorage.app");
            response.put("project", "disrupton-new");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en prueba de conexión: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
} 