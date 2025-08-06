package com.disrupton.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    
    private boolean success;
    private String message;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String downloadUrl;
    private String filePath;
    private String userId;
    private String modelId;
    private Long timestamp;
    
    // Constructor para éxito
    public static UploadResponse success(String fileName, String originalFileName, Long fileSize, 
                                       String downloadUrl, String filePath, String userId, String modelId) {
        return UploadResponse.builder()
                .success(true)
                .message("Archivo subido exitosamente")
                .fileName(fileName)
                .originalFileName(originalFileName)
                .fileSize(fileSize)
                .downloadUrl(downloadUrl)
                .filePath(filePath)
                .userId(userId)
                .modelId(modelId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // Constructor para error
    public static UploadResponse error(String message, String originalFileName, Long fileSize) {
        return UploadResponse.builder()
                .success(false)
                .message(message)
                .originalFileName(originalFileName)
                .fileSize(fileSize)
                .timestamp(System.currentTimeMillis())
                .build();
    }
} 