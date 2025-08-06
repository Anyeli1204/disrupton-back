package com.disrupton.KiriEngine.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FeaturelessVideoUploadRequest {
    
    private MultipartFile videoFile;
    private String fileFormat = "OBJ";
    
    public enum FileFormat {
        OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ
    }
    
    /**
     * Valida que el archivo de video cumpla con los requisitos para Featureless Object Scan
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
    
    /**
     * Valida que el formato de archivo de salida sea soportado
     */
    public void validateFileFormat() {
        if (fileFormat == null || fileFormat.trim().isEmpty()) {
            throw new IllegalArgumentException("El formato de archivo es requerido");
        }
        
        try {
            FileFormat.valueOf(fileFormat.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato de archivo no soportado. Use: OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ");
        }
    }
} 