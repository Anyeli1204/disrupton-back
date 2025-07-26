package com.disrupton.cultural.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class CulturalUploadRequest {
    
    // Información del objeto cultural
    private String name;
    private String description;
    private String origin;
    private String culturalType;
    private String localPhrases;
    private String story;
    private String region;
    private String captureNotes;
    
    // Información técnica (heredada de KIRI Engine)
    private List<MultipartFile> imagesFiles;
    private String fileFormat = "OBJ";
    
    // Información del usuario
    private Long userId;
    
    public enum CulturalType {
        ARTESANIA, GASTRONOMIA, MUSICA, DANZA, TEXTIL, CERAMICA, 
        ARQUITECTURA, FESTIVAL, RITUAL, LENGUAJE, TRADICION, OTRO
    }
    
    public enum FileFormat {
        OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ
    }
    
    /**
     * Valida la información cultural requerida
     */
    public void validateCulturalInfo() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del objeto cultural es requerido");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del objeto cultural es requerida");
        }
        
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalArgumentException("El lugar de origen es requerido");
        }
        
        if (culturalType == null || culturalType.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de cultura es requerido");
        }
        
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("La región es requerida");
        }
        
        // Validar tipo de cultura
        try {
            CulturalType.valueOf(culturalType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de cultura no válido. Use: ARTESANIA, GASTRONOMIA, MUSICA, DANZA, TEXTIL, CERAMICA, ARQUITECTURA, FESTIVAL, RITUAL, LENGUAJE, TRADICION, OTRO");
        }
    }
    
    /**
     * Valida las imágenes (reutiliza lógica de ImageUploadRequest)
     */
    public void validateImages() {
        if (imagesFiles == null || imagesFiles.isEmpty()) {
            throw new IllegalArgumentException("Se requiere al menos una imagen");
        }
        
        int count = imagesFiles.size();
        if (count < 20) {
            throw new IllegalArgumentException(
                    "Se requieren al menos 20 imágenes para generar un modelo 3D. Proporcionadas: " + count);
        }
        if (count > 300) {
            throw new IllegalArgumentException(
                    "Máximo 300 imágenes permitidas. Proporcionadas: " + count);
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
     * Valida que el formato del archivo sea una imagen soportada
     */
    private boolean isValidImageFormat(String filename) {
        String lowerCase = filename.toLowerCase();
        return lowerCase.endsWith(".jpg") || 
               lowerCase.endsWith(".jpeg") || 
               lowerCase.endsWith(".png");
    }
} 