package com.disrupton.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageFile {
    
    private String id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private String downloadUrl;
    private String contentType;
    private Long fileSize;
    private String userId;
    private String modelId;
    private String bucketName;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    
    // Tipos de archivo
    public enum FileType {
        MODEL_3D,
        THUMBNAIL,
        PROCESSING_IMAGE
    }
    
    private FileType fileType;
} 