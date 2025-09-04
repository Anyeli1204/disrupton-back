package com.disrupton.socialPost.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialPostImageService {
    
    private final Storage storage;
    
    @Value("${firebase.project.storage.bucket:disrupton-new.firebasestorage.app}")
    private String bucketName;
    
    /**
     * Subir múltiples imágenes para posts sociales
     */
    public List<String> uploadPostImages(List<MultipartFile> images, String userId) throws IOException {
        log.info("📸 Uploading {} images for social post by user: {}", images.size(), userId);
        
        List<String> imageUrls = new ArrayList<>();
        
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            
            // Validar que sea una imagen
            if (!isValidImageFile(image)) {
                throw new IllegalArgumentException("El archivo " + image.getOriginalFilename() + " no es una imagen válida");
            }
            
            // Validar tamaño (máximo 10MB)
            if (image.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("La imagen " + image.getOriginalFilename() + " excede el tamaño máximo de 10MB");
            }
            
            try {
                String imageUrl = uploadSingleImage(image, userId, i);
                imageUrls.add(imageUrl);
                log.info("✅ Image {} uploaded successfully: {}", i + 1, imageUrl);
            } catch (IOException e) {
                log.error("❌ Failed to upload image {}: {}", i + 1, e.getMessage());
                throw new IOException("Error al subir la imagen " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        log.info("🎉 All {} images uploaded successfully for user: {}", images.size(), userId);
        return imageUrls;
    }
    
    /**
     * Subir una sola imagen
     */
    public String uploadSingleImage(MultipartFile image, String userId) throws IOException {
        return uploadSingleImage(image, userId, 0);
    }
    
    private String uploadSingleImage(MultipartFile image, String userId, int index) throws IOException {
        String postId = UUID.randomUUID().toString();
        String fileName = generateImageFileName(image.getOriginalFilename(), postId, index);
        
        // Crear ruta específica para imágenes de posts sociales
        String filePath = String.format("social-posts/%s/%s", userId, fileName);
        
        log.info("📸 Uploading social post image: {} to path: {}", image.getOriginalFilename(), filePath);
        
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(image.getContentType())
                .build();
        
        Blob blob = storage.create(blobInfo, image.getBytes());
        
        String downloadUrl = blob.signUrl(7, java.util.concurrent.TimeUnit.DAYS).toString();
        log.info("✅ Social post image uploaded successfully: {}", downloadUrl);
        
        return downloadUrl;
    }
    
    /**
     * Validar que el archivo sea una imagen
     */
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp")
        );
    }
    
    /**
     * Generar nombre único para la imagen
     */
    private String generateImageFileName(String originalFileName, String postId, int index) {
        String extension = getFileExtension(originalFileName);
        return String.format("post_%s_img_%d_%s.%s", 
                postId, 
                index, 
                UUID.randomUUID().toString().substring(0, 8), 
                extension);
    }
    
    /**
     * Obtener extensión del archivo
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg"; // extensión por defecto
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
