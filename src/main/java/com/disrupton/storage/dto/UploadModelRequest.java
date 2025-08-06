package com.disrupton.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadModelRequest {
    
    private MultipartFile file;
    private String userId;
    private String modelId;
    private String description;
    private String tags;
} 