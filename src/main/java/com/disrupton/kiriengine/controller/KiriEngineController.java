package com.disrupton.kiriengine.controller;

import com.disrupton.upload.model.ImageUploadRequest;
import com.disrupton.upload.model.VideoUploadRequest;
import com.disrupton.upload.model.FeaturelessVideoUploadRequest;
import com.disrupton.upload.model.FeaturelessImageUploadRequest;
import com.disrupton.kiriengine.model.KiriEngineResponse;
import com.disrupton.kiriengine.model.ModelStatusResponse;
import com.disrupton.kiriengine.model.ModelDownloadResponse;
import com.disrupton.kiriengine.service.KiriEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/kiri-engine")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class KiriEngineController {
    
    private final KiriEngineService kiriEngineService;
    
    /**
     * Endpoint para subir imágenes y generar modelo 3D
     * 
     * @param imagesFiles Lista de archivos de imágenes
     * @param modelQuality Calidad del modelo (0: High, 1: Medium, 2: Low, 3: Ultra)
     * @param textureQuality Calidad de textura (0: 4K, 1: 2K, 2: 1K, 3: 8K)
     * @param fileFormat Formato del archivo (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)
     * @param isMask Auto Object Masking (0: Off, 1: On)
     * @param textureSmoothing Texture Smoothing (0: Off, 1: On)
     * @return Respuesta con el serial del modelo generado
     */
    @PostMapping("/upload-images")
    public ResponseEntity<?> uploadImages(
            @RequestParam("imagesFiles") List<MultipartFile> imagesFiles,
            @RequestParam(value = "modelQuality", defaultValue = "0") Integer modelQuality,
            @RequestParam(value = "textureQuality", defaultValue = "0") Integer textureQuality,
            @RequestParam(value = "fileFormat", defaultValue = "OBJ") String fileFormat,
            @RequestParam(value = "isMask", defaultValue = "1") Integer isMask,
            @RequestParam(value = "textureSmoothing", defaultValue = "1") Integer textureSmoothing) {
        
        try {
            log.info("Recibida solicitud para subir {} imágenes", imagesFiles.size());
            
            // Crear objeto de solicitud
            ImageUploadRequest request = new ImageUploadRequest();
            request.setImagesFiles(imagesFiles);
            request.setModelQuality(modelQuality);
            request.setTextureQuality(textureQuality);
            request.setFileFormat(fileFormat);
            request.setIsMask(isMask);
            request.setTextureSmoothing(textureSmoothing);
            
            // Procesar solicitud
            KiriEngineResponse response = kiriEngineService.uploadImages(request);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Error de validación", e.getMessage()));
        } catch (IOException e) {
            log.error("Error al procesar archivos: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error al procesar archivos", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para consultar el estado real del modelo en KIRI Engine
     * 
     * @param serial El serial del modelo obtenido al subir las imágenes
     * @return Estado real del modelo desde KIRI Engine
     */
    @GetMapping("/model-status/{serial}")
    public ResponseEntity<?> getModelStatus(@PathVariable String serial) {
        try {
            log.info("Consultando estado del modelo: {}", serial);
            
            // Consultar estado real en KIRI Engine
            ModelStatusResponse response = kiriEngineService.getModelStatus(serial);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al consultar estado del modelo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error al consultar estado", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para descargar el modelo 3D
     * 
     * @param serial El serial del modelo
     * @return Enlace de descarga del modelo 3D
     */
    @GetMapping("/download-model/{serial}")
    public ResponseEntity<?> downloadModel(@PathVariable String serial) {
        try {
            log.info("Solicitando descarga del modelo: {}", serial);
            
            // Obtener enlace de descarga
            ModelDownloadResponse response = kiriEngineService.downloadModel(serial);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            log.error("Modelo no está listo: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Modelo no está listo", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al descargar modelo: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error al descargar modelo", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para subir video y generar modelo 3D
     * 
     * @param videoFile Archivo de video
     * @param modelQuality Calidad del modelo (0: High, 1: Medium, 2: Low, 3: Ultra)
     * @param textureQuality Calidad de textura (0: 4K, 1: 2K, 2: 1K, 3: 8K)
     * @param fileFormat Formato del archivo (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)
     * @param isMask Auto Object Masking (0: Off, 1: On)
     * @param textureSmoothing Texture Smoothing (0: Off, 1: On)
     * @return Respuesta con el serial del modelo generado
     */
    @PostMapping("/upload-video")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam(value = "modelQuality", defaultValue = "0") Integer modelQuality,
            @RequestParam(value = "textureQuality", defaultValue = "0") Integer textureQuality,
            @RequestParam(value = "fileFormat", defaultValue = "OBJ") String fileFormat,
            @RequestParam(value = "isMask", defaultValue = "1") Integer isMask,
            @RequestParam(value = "textureSmoothing", defaultValue = "1") Integer textureSmoothing) {
        
        try {
            log.info("Recibida solicitud para subir video: {}", videoFile.getOriginalFilename());
            
            // Crear objeto de solicitud
            VideoUploadRequest request = new VideoUploadRequest();
            request.setVideoFile(videoFile);
            request.setModelQuality(modelQuality);
            request.setTextureQuality(textureQuality);
            request.setFileFormat(fileFormat);
            request.setIsMask(isMask);
            request.setTextureSmoothing(textureSmoothing);
            
            // Procesar solicitud
            KiriEngineResponse response = kiriEngineService.uploadVideo(request);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Error de validación", e.getMessage()));
        } catch (IOException e) {
            log.error("Error al procesar archivo de video: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error al procesar archivo de video", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para subir video y generar modelo 3D usando Featureless Object Scan
     * 
     * @param videoFile Archivo de video
     * @param fileFormat Formato del archivo (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)
     * @return Respuesta con el serial del modelo generado
     */
    @PostMapping("/upload-featureless-video")
    public ResponseEntity<?> uploadFeaturelessVideo(
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam(value = "fileFormat", defaultValue = "OBJ") String fileFormat) {
        
        try {
            log.info("Recibida solicitud para subir video Featureless: {}", videoFile.getOriginalFilename());
            
            // Crear objeto de solicitud
            FeaturelessVideoUploadRequest request = new FeaturelessVideoUploadRequest();
            request.setVideoFile(videoFile);
            request.setFileFormat(fileFormat);
            
            // Procesar solicitud
            KiriEngineResponse response = kiriEngineService.uploadFeaturelessVideo(request);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Error de validación", e.getMessage()));
        } catch (IOException e) {
            log.error("Error al procesar archivo de video Featureless: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error al procesar archivo de video", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para subir imágenes y generar modelo 3D usando Featureless Object Scan
     * 
     * @param imagesFiles Lista de archivos de imágenes
     * @param fileFormat Formato del archivo (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)
     * @return Respuesta con el serial del modelo generado
     */
    @PostMapping("/upload-featureless-images")
    public ResponseEntity<?> uploadFeaturelessImages(
            @RequestParam("imagesFiles") List<MultipartFile> imagesFiles,
            @RequestParam(value = "fileFormat", defaultValue = "OBJ") String fileFormat) {
        
        try {
            log.info("Recibida solicitud para subir {} imágenes Featureless", imagesFiles.size());
            
            // Crear objeto de solicitud
            FeaturelessImageUploadRequest request = new FeaturelessImageUploadRequest();
            request.setImagesFiles(imagesFiles);
            request.setFileFormat(fileFormat);
            
            // Procesar solicitud
            KiriEngineResponse response = kiriEngineService.uploadFeaturelessImages(request);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Error de validación", e.getMessage()));
        } catch (IOException e) {
            log.error("Error al procesar archivos de imágenes Featureless: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error al procesar archivos de imágenes", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new ErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }
    
    /**
     * Endpoint de prueba para verificar la conectividad
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("KIRI Engine Service está funcionando correctamente");
    }
    
    /**
     * Clase para respuestas de error
     */
    public static class ErrorResponse {
        private String error;
        private String message;
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        
        // Getters y setters
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 