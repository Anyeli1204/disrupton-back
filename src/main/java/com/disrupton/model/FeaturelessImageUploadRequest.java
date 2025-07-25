package com.disrupton.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FeaturelessImageUploadRequest {
    
    private List<MultipartFile> imagesFiles;
    private String fileFormat = "OBJ";
    
    public enum FileFormat {
        OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ
    }
    
    /**
     * Valida que el conjunto de imágenes cumpla con los requisitos para Featureless Object Scan
     * - Mínimo 20 imágenes
     * - Máximo 300 imágenes
     * - Formato soportado
     */
    public void validateImages() {
        if (imagesFiles == null || imagesFiles.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos una imagen");
        }
        
        int count = imagesFiles.size();
        if (count < 20) {
            throw new IllegalArgumentException(
                    "Se requieren al menos 20 imágenes para Featureless Object Scan. Proporcionadas: " + count);
        }
        if (count > 300) {
            throw new IllegalArgumentException(
                    "Máximo 300 imágenes permitidas para Featureless Object Scan. Proporcionadas: " + count);
        }
        
        // Validar cada imagen
        for (int i = 0; i < imagesFiles.size(); i++) {
            MultipartFile image = imagesFiles.get(i);
            if (image == null || image.isEmpty()) {
                throw new IllegalArgumentException("La imagen " + (i + 1) + " está vacía");
            }
            
            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null || !isValidImageFormat(originalFilename)) {
                throw new IllegalArgumentException(
                        "Formato de imagen no soportado en la imagen " + (i + 1) + ": " + originalFilename + 
                        ". Use JPG, JPEG, PNG");
            }
        }
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
    
    /**
     * Valida que el formato del archivo sea una imagen soportada
     */
    private boolean isValidImageFormat(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".jpg") || 
               lowerCase.endsWith(".jpeg") || 
               lowerCase.endsWith(".png");
    }
} 