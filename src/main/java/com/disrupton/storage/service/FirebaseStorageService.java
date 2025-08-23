package com.disrupton.storage.service;

import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FirebaseStorageService {

    @Value("${firebase.project.storage.bucket:disrupton-new.firebasestorage.app}")  // ✅ Correcto
    private String bucketName;

    private final Storage storage; // ✅ Inyectar el bean configurado

    // Constructor para inyección de dependencias
    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }
    /**
     * Sube un archivo 3D (modelo) a Firebase Storage
     */
    public String uploadModel3D(MultipartFile file, String userId, String modelId) throws IOException {
        log.info("📁 Subiendo modelo 3D: {} para usuario: {}", file.getOriginalFilename(), userId);
        
        String fileName = generateFileName(file.getOriginalFilename(), "models");
        String filePath = String.format("models/%s/%s/%s", userId, modelId, fileName);
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        Blob blob = storage.create(blobInfo, file.getBytes());
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Modelo 3D subido exitosamente: {}", downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Sube una imagen miniatura (thumbnail) a Firebase Storage
     */
    public String uploadThumbnail(MultipartFile file, String userId, String modelId) throws IOException {
        log.info("🖼️ Subiendo thumbnail: {} para usuario: {}", file.getOriginalFilename(), userId);
        
        String fileName = generateFileName(file.getOriginalFilename(), "thumbnails");
        String filePath = String.format("thumbnails/%s/%s/%s", userId, modelId, fileName);
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        Blob blob = storage.create(blobInfo, file.getBytes());
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Thumbnail subido exitosamente: {}", downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Sube múltiples imágenes para procesamiento con KIRI Engine
     */
    public String uploadImagesForProcessing(MultipartFile[] files, String userId, String modelId) throws IOException {
        log.info("📸 Subiendo {} imágenes para procesamiento", files.length);
        
        String[] urls = new String[files.length];
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String fileName = generateFileName(file.getOriginalFilename(), "processing");
            String filePath = String.format("processing/%s/%s/%s", userId, modelId, fileName);
            
            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            Blob blob = storage.create(blobInfo, file.getBytes());
            urls[i] = blob.getMediaLink();
        }
        
        log.info("✅ {} imágenes subidas para procesamiento", files.length);
        return String.join(",", urls);
    }

    /**
     * Elimina un archivo de Firebase Storage
     */
    public boolean deleteFile(String filePath) {
        try {
            log.info("🗑️ Eliminando archivo: {}", filePath);
            
            BlobId blobId = BlobId.of(bucketName, filePath);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                log.info("✅ Archivo eliminado exitosamente: {}", filePath);
            } else {
                log.warn("⚠️ Archivo no encontrado para eliminar: {}", filePath);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene la URL pública de un archivo
     */
    public String getPublicUrl(String filePath) {
        try {
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            
            if (blob != null) {
                return blob.getMediaLink();
            } else {
                log.warn("⚠️ Archivo no encontrado: {}", filePath);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Error al obtener URL pública: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Genera un nombre de archivo único
     */
    private String generateFileName(String originalFileName, String prefix) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String uniqueId = UUID.randomUUID().toString();
        return prefix + "_" + uniqueId + extension;
    }

    /**
     * Verifica si un archivo existe
     */
    public boolean fileExists(String filePath) {
        try {
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            return blob != null;
        } catch (Exception e) {
            log.error("❌ Error al verificar archivo: {}", e.getMessage(), e);
            return false;
        }
    }
    /**
     * Sube múltiples imágenes para comentarios del mural
     */
    public String uploadCommentImages(MultipartFile[] files, String userId, String commentId) throws IOException {
        log.info("💬 Subiendo {} imágenes para comentario del mural", files.length);

        String[] urls = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String fileName = generateFileName(file.getOriginalFilename(), "comment");
            String filePath = String.format("comments/%s/%s/%s", userId, commentId, fileName);

            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());
            urls[i] = blob.getMediaLink();
        }

        log.info("✅ {} imágenes subidas para comentario", files.length);
        return String.join(",", urls);
    }
} 