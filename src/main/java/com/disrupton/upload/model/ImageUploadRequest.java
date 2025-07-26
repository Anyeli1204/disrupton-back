package com.disrupton.upload.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ImageUploadRequest {
    
    private List<MultipartFile> imagesFiles;
    private Integer modelQuality = 0; // Default: High
    private Integer textureQuality = 0; // Default: 4K
    private String fileFormat = "OBJ";
    private Integer isMask = 1; // Default: Turn on Auto Object Masking
    private Integer textureSmoothing = 1; // Default: Turn on Texture Smoothing
    
    // Enums para validación
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
} 