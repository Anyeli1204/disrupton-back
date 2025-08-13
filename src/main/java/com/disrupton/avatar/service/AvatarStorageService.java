package com.disrupton.avatar.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarStorageService {

    @Value("${firebase.project.storage.bucket:disrupton-new.firebasestorage.app}")
    private String bucketName;

    private final Storage storage;

    /**
     * Sube un avatar GLB a Firebase Storage
     */
    public String uploadAvatarGLB(MultipartFile file, String avatarName) throws IOException {
        log.info("🎭 Subiendo avatar GLB: {} - Tamaño: {} bytes", file.getOriginalFilename(), file.getSize());
        
        // Validar que sea un archivo GLB
        if (!isGLBFile(file)) {
            throw new IllegalArgumentException("El archivo debe ser un modelo GLB válido");
        }
        
        // Validar tamaño (máximo 50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo GLB no puede ser mayor a 50MB");
        }
        
        String fileName = generateAvatarFileName(file.getOriginalFilename(), avatarName);
        String filePath = String.format("avatars/glb/%s", fileName);
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("avatarName", avatarName);
        metadata.put("uploadedAt", String.valueOf(System.currentTimeMillis()));
        
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("model/gltf-binary")
                .setMetadata(metadata)
                .build();
        
        Blob blob = storage.create(blobInfo, file.getBytes());
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Avatar GLB subido exitosamente: {} - URL: {}", fileName, downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Sube múltiples avatares GLB (para bulk upload)
     */
    public String[] uploadMultipleAvatars(MultipartFile[] files, String[] avatarNames) throws IOException {
        log.info("🎭 Subiendo {} avatares GLB", files.length);
        
        if (files.length != avatarNames.length) {
            throw new IllegalArgumentException("El número de archivos debe coincidir con el número de nombres");
        }
        
        String[] urls = new String[files.length];
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String avatarName = avatarNames[i];
            
            try {
                urls[i] = uploadAvatarGLB(file, avatarName);
                log.info("✅ Avatar {} subido: {}", i + 1, avatarName);
            } catch (Exception e) {
                log.error("❌ Error subiendo avatar {}: {}", i + 1, e.getMessage());
                urls[i] = null;
            }
        }
        
        log.info("✅ {} avatares subidos exitosamente", files.length);
        return urls;
    }

    /**
     * Sube un avatar GLB desde un archivo local (para migración)
     */
    public String uploadAvatarFromLocalPath(String localFilePath, String avatarName) throws IOException {
        log.info("🎭 Subiendo avatar desde archivo local: {} - Nombre: {}", localFilePath, avatarName);
        
        java.io.File localFile = new java.io.File(localFilePath);
        
        if (!localFile.exists()) {
            throw new IllegalArgumentException("El archivo local no existe: " + localFilePath);
        }
        
        if (!localFilePath.toLowerCase().endsWith(".glb")) {
            throw new IllegalArgumentException("El archivo debe ser un modelo GLB");
        }
        
        String fileName = generateAvatarFileName(localFile.getName(), avatarName);
        String filePath = String.format("avatars/glb/%s", fileName);
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("avatarName", avatarName);
        metadata.put("uploadedAt", String.valueOf(System.currentTimeMillis()));
        metadata.put("source", "local_file");
        
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("model/gltf-binary")
                .setMetadata(metadata)
                .build();
        
        // Leer archivo local
        byte[] fileBytes = java.nio.file.Files.readAllBytes(localFile.toPath());
        
        Blob blob = storage.create(blobInfo, fileBytes);
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Avatar GLB subido desde archivo local: {} - URL: {}", fileName, downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Elimina un avatar GLB de Firebase Storage
     */
    public boolean deleteAvatarGLB(String avatarName) {
        try {
            log.info("🗑️ Eliminando avatar GLB: {}", avatarName);
            
            // Buscar el archivo por nombre
            String prefix = String.format("avatars/glb/%s", avatarName);
            
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
                boolean deleted = storage.delete(blob.getBlobId());
                if (deleted) {
                    log.info("✅ Avatar GLB eliminado exitosamente: {}", avatarName);
                    return true;
                }
            }
            
            log.warn("⚠️ Avatar GLB no encontrado para eliminar: {}", avatarName);
            return false;
        } catch (Exception e) {
            log.error("❌ Error eliminando avatar GLB: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene la URL pública de un avatar GLB
     */
    public String getAvatarGLBUrl(String avatarName) {
        try {
            log.info("🔍 Obteniendo URL del avatar GLB: {}", avatarName);
            
            String prefix = String.format("avatars/glb/%s", avatarName);
            
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
                String url = blob.getMediaLink();
                log.info("✅ URL del avatar GLB obtenida: {}", url);
                return url;
            }
            
            log.warn("⚠️ Avatar GLB no encontrado: {}", avatarName);
            return null;
        } catch (Exception e) {
            log.error("❌ Error obteniendo URL del avatar GLB: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Lista todos los avatares GLB disponibles
     */
    public String[] listAvailableAvatars() {
        try {
            log.info("📋 Listando avatares GLB disponibles");
            
            java.util.List<String> avatarNames = new java.util.ArrayList<>();
            
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix("avatars/glb/")).iterateAll()) {
                String fileName = blob.getName();
                String avatarName = fileName.replace("avatars/glb/", "").replace(".glb", "");
                avatarNames.add(avatarName);
            }
            
            log.info("✅ {} avatares GLB encontrados", avatarNames.size());
            return avatarNames.toArray(new String[0]);
        } catch (Exception e) {
            log.error("❌ Error listando avatares GLB: {}", e.getMessage(), e);
            return new String[0];
        }
    }

    /**
     * Verifica si un avatar GLB existe
     */
    public boolean avatarExists(String avatarName) {
        try {
            String prefix = String.format("avatars/glb/%s", avatarName);
            
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("❌ Error verificando existencia del avatar: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene información de un avatar GLB
     */
    public AvatarInfo getAvatarInfo(String avatarName) {
        try {
            log.info("ℹ️ Obteniendo información del avatar GLB: {}", avatarName);
            
            String prefix = String.format("avatars/glb/%s", avatarName);
            
            for (Blob blob : storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
                AvatarInfo info = new AvatarInfo();
                info.setName(avatarName);
                info.setUrl(blob.getMediaLink());
                info.setSize(blob.getSize());
                info.setCreated(new java.util.Date(blob.getCreateTime()));
                info.setUpdated(new java.util.Date(blob.getUpdateTime()));
                info.setContentType(blob.getContentType());
                
                // Obtener metadatos
                if (blob.getMetadata() != null) {
                    info.setAvatarName(blob.getMetadata().get("avatarName"));
                    info.setUploadedAt(blob.getMetadata().get("uploadedAt"));
                    info.setSource(blob.getMetadata().get("source"));
                }
                
                log.info("✅ Información del avatar GLB obtenida: {}", avatarName);
                return info;
            }
            
            log.warn("⚠️ Avatar GLB no encontrado: {}", avatarName);
            return null;
        } catch (Exception e) {
            log.error("❌ Error obteniendo información del avatar GLB: {}", e.getMessage(), e);
            return null;
        }
    }

    // Métodos privados de utilidad
    
    private boolean isGLBFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.toLowerCase().endsWith(".glb");
    }
    
    private String generateAvatarFileName(String originalFileName, String avatarName) {
        String extension = ".glb";
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return avatarName + "_" + uniqueId + extension;
    }

    // Clase interna para información del avatar
    public static class AvatarInfo {
        private String name;
        private String url;
        private Long size;
        private java.util.Date created;
        private java.util.Date updated;
        private String contentType;
        private String avatarName;
        private String uploadedAt;
        private String source;

        // Getters y Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }
        
        public java.util.Date getCreated() { return created; }
        public void setCreated(java.util.Date created) { this.created = created; }
        
        public java.util.Date getUpdated() { return updated; }
        public void setUpdated(java.util.Date updated) { this.updated = updated; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public String getAvatarName() { return avatarName; }
        public void setAvatarName(String avatarName) { this.avatarName = avatarName; }
        
        public String getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
}
