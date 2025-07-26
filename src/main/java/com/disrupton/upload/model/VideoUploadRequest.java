package com.disrupton.upload.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoUploadRequest {
    
    private MultipartFile videoFile;
    private Integer modelQuality = 0; // Default: High
    private Integer textureQuality = 0; // Default: 4K
    private String fileFormat = "OBJ";
    private Integer isMask = 1; // Default: Turn on Auto Object Masking
    private Integer textureSmoothing = 1; // Default: Turn on Texture Smoothing
    
    // Enums para validación (reutilizando los mismos de ImageUploadRequest)
    public enum ModelQuality {
        HIGH(0), MEDIUM(1), LOW(2), ULTRA(3);
        
        private final int value;
        
        ModelQuality(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public enum TextureQuality {
        FOUR_K(0), TWO_K(1), ONE_K(2), EIGHT_K(3);
        
        private final int value;
        
        TextureQuality(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public enum FileFormat {
        OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ
    }
    
    /**
     * Valida que el archivo de video cumpla con los requisitos
     * - Resolución máxima: 1920x1080
     * - Duración máxima: 3 minutos
     * - Formato soportado
     */
    public void validateVideoFile() {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo de video es requerido");
        }
        
        // Validar formato de archivo
        String originalFilename = videoFile.getOriginalFilename();
        if (originalFilename == null || !isValidVideoFormat(originalFilename)) {
            throw new IllegalArgumentException("Formato de video no soportado. Use MP4, AVI, MOV, WMV, FLV, WEBM");
        }
        
        // Validar tamaño del archivo (aproximadamente 3 minutos a 1920x1080)
        long maxSizeBytes = 500 * 1024 * 1024; // 500MB como límite aproximado
        if (videoFile.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo de video es demasiado grande. Máximo 500MB");
        }
    }
    
    /**
     * Valida que el formato del archivo sea un video soportado
     */
    private boolean isValidVideoFormat(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".mp4") || 
               lowerCase.endsWith(".avi") || 
               lowerCase.endsWith(".mov") || 
               lowerCase.endsWith(".wmv") || 
               lowerCase.endsWith(".flv") || 
               lowerCase.endsWith(".webm");
    }
} 