package com.disrupton.service;

import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FirebaseStorageService {

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private static final String BUCKET_NAME = "disrupton2025.appspot.com"; // Cambiar por tu bucket

    /**
     * Sube un archivo a Firebase Storage
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        log.info("📤 Subiendo archivo: {} a la carpeta: {}", file.getOriginalFilename(), folder);
        
        // Generar nombre único para el archivo
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String filePath = folder + "/" + fileName;
        
        // Crear blob info
        BlobId blobId = BlobId.of(BUCKET_NAME, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        // Subir archivo
        Blob blob = storage.create(blobInfo, file.getBytes());
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Archivo subido exitosamente: {}", downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Sube múltiples archivos a Firebase Storage
     */
    public String[] uploadMultipleFiles(MultipartFile[] files, String folder) throws IOException {
        log.info("📤 Subiendo {} archivos a la carpeta: {}", files.length, folder);
        
        String[] urls = new String[files.length];
        
        for (int i = 0; i < files.length; i++) {
            urls[i] = uploadFile(files[i], folder);
        }
        
        log.info("✅ {} archivos subidos exitosamente", files.length);
        return urls;
    }

    /**
     * Sube un modelo 3D a Firebase Storage
     */
    public String uploadModel3D(byte[] modelData, String objectId, String format) throws IOException {
        log.info("🏗️ Subiendo modelo 3D para objeto: {}", objectId);
        
        String fileName = "model." + format.toLowerCase();
        String filePath = "cultural-objects/" + objectId + "/" + fileName;
        
        // Crear blob info
        BlobId blobId = BlobId.of(BUCKET_NAME, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(getContentTypeForFormat(format))
                .build();
        
        // Subir archivo
        Blob blob = storage.create(blobInfo, modelData);
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Modelo 3D subido exitosamente: {}", downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Sube una imagen miniatura a Firebase Storage
     */
    public String uploadThumbnail(byte[] imageData, String objectId) throws IOException {
        log.info("🖼️ Subiendo miniatura para objeto: {}", objectId);
        
        String fileName = "thumbnail.jpg";
        String filePath = "cultural-objects/" + objectId + "/" + fileName;
        
        // Crear blob info
        BlobId blobId = BlobId.of(BUCKET_NAME, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/jpeg")
                .build();
        
        // Subir archivo
        Blob blob = storage.create(blobInfo, imageData);
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Miniatura subida exitosamente: {}", downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Sube una foto de Realidad Aumentada
     */
    public String uploadARPhoto(MultipartFile photo, String photoId) throws IOException {
        log.info("📸 Subiendo foto de RA: {}", photoId);
        
        String fileName = "photo.jpg";
        String filePath = "ar-photos/" + photoId + "/" + fileName;
        
        // Crear blob info
        BlobId blobId = BlobId.of(BUCKET_NAME, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(photo.getContentType())
                .build();
        
        // Subir archivo
        Blob blob = storage.create(blobInfo, photo.getBytes());
        
        String downloadUrl = blob.getMediaLink();
        log.info("✅ Foto de RA subida exitosamente: {}", downloadUrl);
        
        return downloadUrl;
    }

    /**
     * Elimina un archivo de Firebase Storage
     */
    public boolean deleteFile(String filePath) {
        log.info("🗑️ Eliminando archivo: {}", filePath);
        
        try {
            BlobId blobId = BlobId.of(BUCKET_NAME, filePath);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                log.info("✅ Archivo eliminado exitosamente: {}", filePath);
            } else {
                log.warn("⚠️ Archivo no encontrado: {}", filePath);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Elimina todos los archivos de un objeto cultural
     */
    public boolean deleteObjectFiles(String objectId) {
        log.info("🗑️ Eliminando archivos del objeto cultural: {}", objectId);
        
        try {
            String prefix = "cultural-objects/" + objectId + "/";
            
            // Listar todos los archivos con el prefijo
            Page<Blob> blobs = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(prefix));
            
            boolean allDeleted = true;
            for (Blob blob : blobs.iterateAll()) {
                boolean deleted = storage.delete(blob.getBlobId());
                if (!deleted) {
                    allDeleted = false;
                    log.warn("⚠️ No se pudo eliminar: {}", blob.getName());
                }
            }
            
            if (allDeleted) {
                log.info("✅ Todos los archivos del objeto eliminados exitosamente");
            } else {
                log.warn("⚠️ Algunos archivos no pudieron ser eliminados");
            }
            
            return allDeleted;
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivos del objeto: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtiene la URL pública de un archivo
     */
    public String getPublicUrl(String filePath) {
        return "https://storage.googleapis.com/" + BUCKET_NAME + "/" + filePath;
    }

    /**
     * Genera un nombre único para el archivo
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Obtiene el content type para diferentes formatos de archivo
     */
    private String getContentTypeForFormat(String format) {
        switch (format.toUpperCase()) {
            case "OBJ":
                return "text/plain";
            case "FBX":
                return "application/octet-stream";
            case "STL":
                return "application/sla";
            case "PLY":
                return "text/plain";
            case "GLB":
                return "model/gltf-binary";
            case "GLTF":
                return "model/gltf+json";
            case "USDZ":
                return "model/vnd.usdz+zip";
            case "XYZ":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
} 