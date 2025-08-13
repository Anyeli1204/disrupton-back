package com.disrupton.KiriEngine.service;

import com.disrupton.config.KiriEngineConfig;
import com.disrupton.KiriEngine.model.KiriEngineResponse;
import com.disrupton.KiriEngine.model.ImageUploadRequest;
import com.disrupton.KiriEngine.model.VideoUploadRequest;
import com.disrupton.KiriEngine.model.FeaturelessVideoUploadRequest;
import com.disrupton.KiriEngine.model.FeaturelessImageUploadRequest;
import com.disrupton.KiriEngine.model.ModelStatusResponse;
import com.disrupton.KiriEngine.model.ModelDownloadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiriEngineService {
    
    private final RestTemplate restTemplate;
    private final KiriEngineConfig config;
    
    private static final String UPLOAD_ENDPOINT = "/open/photo/image";
    private static final String VIDEO_UPLOAD_ENDPOINT = "/open/photo/video";
    private static final String FEATURELESS_VIDEO_UPLOAD_ENDPOINT = "/open/featureless/video";
    private static final String FEATURELESS_IMAGE_UPLOAD_ENDPOINT = "/open/featureless/image";
    private static final String STATUS_ENDPOINT = "/open/model/getStatus";
    private static final String DOWNLOAD_ENDPOINT = "/open/model/getModelZip";
    
    /**
     * Sube imágenes a KIRI Engine para generar un modelo 3D
     * 
     * @param request La solicitud con las imágenes y parámetros
     * @return La respuesta de KIRI Engine con el serial del modelo
     * @throws IOException Si hay error al procesar los archivos
     */
    public KiriEngineResponse uploadImages(ImageUploadRequest request) throws IOException {
        log.info("Iniciando carga de {} imágenes a KIRI Engine", 
                request.getImagesFiles().size());
        
        // Validar número de imágenes
        validateImageCount(request.getImagesFiles());
        
        // Crear headers
        HttpHeaders headers = createHeaders();
        
        // Crear body multipart
        MultiValueMap<String, Object> body = createMultipartBody(request);
        
        // Crear request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
        
        // URL completa
        String url = config.getBaseUrl() + UPLOAD_ENDPOINT;
        
        log.info("Enviando solicitud a: {}", url);
        
        try {
            ResponseEntity<KiriEngineResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    KiriEngineResponse.class
            );
            
            KiriEngineResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getOk()) {
                log.info("Carga exitosa. Serial: {}", responseBody.getData().getSerialize());
            } else {
                log.error("Error en la respuesta: {}", responseBody);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("Error al comunicarse con KIRI Engine API", e);
            throw new RuntimeException("Error al procesar la solicitud con KIRI Engine", e);
        }
    }
    
    /**
     * Sube un video a KIRI Engine para generar un modelo 3D
     * 
     * @param request La solicitud con el video y parámetros
     * @return La respuesta de KIRI Engine con el serial del modelo
     * @throws IOException Si hay error al procesar el archivo
     */
    public KiriEngineResponse uploadVideo(VideoUploadRequest request) throws IOException {
        log.info("Iniciando carga de video a KIRI Engine: {}", 
                request.getVideoFile().getOriginalFilename());
        
        // Validar archivo de video
        request.validateVideoFile();
        
        // Crear headers
        HttpHeaders headers = createHeaders();
        
        // Crear body multipart
        MultiValueMap<String, Object> body = createVideoMultipartBody(request);
        
        // Crear request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
        
        // URL completa
        String url = config.getBaseUrl() + VIDEO_UPLOAD_ENDPOINT;
        
        log.info("Enviando solicitud de video a: {}", url);
        
        try {
            ResponseEntity<KiriEngineResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    KiriEngineResponse.class
            );
            
            KiriEngineResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getOk()) {
                log.info("Carga de video exitosa. Serial: {}", responseBody.getData().getSerialize());
            } else {
                log.error("Error en la respuesta de video: {}", responseBody);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("Error al comunicarse con KIRI Engine API para video", e);
            throw new RuntimeException("Error al procesar la solicitud de video con KIRI Engine", e);
        }
    }
    
    /**
     * Sube un video a KIRI Engine para generar un modelo 3D usando Featureless Object Scan
     * 
     * @param request La solicitud con el video y formato
     * @return La respuesta de KIRI Engine con el serial del modelo
     * @throws IOException Si hay error al procesar el archivo
     */
    public KiriEngineResponse uploadFeaturelessVideo(FeaturelessVideoUploadRequest request) throws IOException {
        log.info("Iniciando carga de video Featureless a KIRI Engine: {}", 
                request.getVideoFile().getOriginalFilename());
        
        // Validar archivo de video
        request.validateVideoFile();
        request.validateFileFormat();
        
        // Crear headers
        HttpHeaders headers = createHeaders();
        
        // Crear body multipart
        MultiValueMap<String, Object> body = createFeaturelessVideoMultipartBody(request);
        
        // Crear request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
        
        // URL completa
        String url = config.getBaseUrl() + FEATURELESS_VIDEO_UPLOAD_ENDPOINT;
        
        log.info("Enviando solicitud de video Featureless a: {}", url);
        
        try {
            ResponseEntity<KiriEngineResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    KiriEngineResponse.class
            );
            
            KiriEngineResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getOk()) {
                log.info("Carga de video Featureless exitosa. Serial: {}", responseBody.getData().getSerialize());
                log.info("Tipo de cálculo: Featureless Object Scan (calculateType: 2)");
            } else {
                log.error("Error en la respuesta de video Featureless: {}", responseBody);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("Error al comunicarse con KIRI Engine API para video Featureless", e);
            throw new RuntimeException("Error al procesar la solicitud de video Featureless con KIRI Engine", e);
        }
    }
    
    /**
     * Sube un conjunto de imágenes a KIRI Engine para generar un modelo 3D usando Featureless Object Scan
     * 
     * @param request La solicitud con las imágenes y formato
     * @return La respuesta de KIRI Engine con el serial del modelo
     * @throws IOException Si hay error al procesar los archivos
     */
    public KiriEngineResponse uploadFeaturelessImages(FeaturelessImageUploadRequest request) throws IOException {
        log.info("Iniciando carga de {} imágenes Featureless a KIRI Engine", 
                request.getImagesFiles().size());
        
        // Validar conjunto de imágenes
        request.validateImages();
        request.validateFileFormat();
        
        // Crear headers
        HttpHeaders headers = createHeaders();
        
        // Crear body multipart
        MultiValueMap<String, Object> body = createFeaturelessImageMultipartBody(request);
        
        // Crear request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
        
        // URL completa
        String url = config.getBaseUrl() + FEATURELESS_IMAGE_UPLOAD_ENDPOINT;
        
        log.info("Enviando solicitud de imágenes Featureless a: {}", url);
        
        try {
            ResponseEntity<KiriEngineResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    KiriEngineResponse.class
            );
            
            KiriEngineResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getOk()) {
                log.info("Carga de imágenes Featureless exitosa. Serial: {}", responseBody.getData().getSerialize());
                log.info("Tipo de cálculo: Featureless Object Scan (calculateType: 2)");
            } else {
                log.error("Error en la respuesta de imágenes Featureless: {}", responseBody);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("Error al comunicarse con KIRI Engine API para imágenes Featureless", e);
            throw new RuntimeException("Error al procesar la solicitud de imágenes Featureless con KIRI Engine", e);
        }
    }
    
    /**
     * Consulta el estado real del modelo en KIRI Engine
     * 
     * @param serial El serial del modelo
     * @return La respuesta con el estado actual del modelo
     */
    public ModelStatusResponse getModelStatus(String serial) {
        log.info("Consultando estado del modelo: {}", serial);
        
        // Crear headers
        HttpHeaders headers = createHeaders();
        
        // Crear request entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // URL completa con parámetro serial
        String url = config.getBaseUrl() + STATUS_ENDPOINT + "?serialize=" + serial;
        
        log.info("Consultando estado en: {}", url);
        
        try {
            ResponseEntity<ModelStatusResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ModelStatusResponse.class
            );
            
            ModelStatusResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getOk()) {
                String statusDesc = responseBody.getStatusDescription();
                log.info("Estado del modelo {}: {} ({})", serial, statusDesc, responseBody.getData().getStatus());
            } else {
                log.error("Error en la respuesta de estado: {}", responseBody);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("Error al consultar estado del modelo: {}", e.getMessage());
            throw new RuntimeException("Error al consultar el estado del modelo", e);
        }
    }
    
    /**
     * Obtiene el enlace de descarga del modelo 3D
     * 
     * @param serial El serial del modelo
     * @return La respuesta con el enlace de descarga
     */
    public ModelDownloadResponse downloadModel(String serial) {
        log.info("Solicitando descarga del modelo: {}", serial);
        
        // Primero verificar que el modelo esté listo
        ModelStatusResponse status = getModelStatus(serial);
        if (!status.isReady()) {
            throw new IllegalStateException(
                    "El modelo no está listo para descargar. Estado actual: " + status.getStatusDescription());
        }
        
        // Crear headers
        HttpHeaders headers = createHeaders();
        
        // Crear request entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        
        // URL completa con parámetro serial
        String url = config.getBaseUrl() + DOWNLOAD_ENDPOINT + "?serialize=" + serial;
        
        log.info("Solicitando descarga en: {}", url);
        
        try {
            ResponseEntity<ModelDownloadResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    ModelDownloadResponse.class
            );
            
            ModelDownloadResponse responseBody = response.getBody();
            if (responseBody != null && responseBody.getOk()) {
                log.info("Enlace de descarga obtenido para modelo: {}", serial);
                log.info("URL de descarga: {}", responseBody.getData().getModelUrl());
            } else {
                log.error("Error en la respuesta de descarga: {}", responseBody);
            }
            
            return responseBody;
            
        } catch (Exception e) {
            log.error("Error al obtener enlace de descarga: {}", e.getMessage());
            throw new RuntimeException("Error al obtener el enlace de descarga", e);
        }
    }
    
    /**
     * Valida que el número de imágenes esté dentro del rango permitido
     */
    private void validateImageCount(List<MultipartFile> images) {
        int count = images.size();
        if (count < 20) {
            throw new IllegalArgumentException(
                    "Se requieren al menos 20 imágenes. Proporcionadas: " + count);
        }
        if (count > 300) {
            throw new IllegalArgumentException(
                    "Máximo 300 imágenes permitidas. Proporcionadas: " + count);
        }
    }
    
    /**
     * Crea los headers de la solicitud con el token de autorización
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(config.getApiKey());
        return headers;
    }
    
    /**
     * Crea el body multipart con todos los parámetros
     */
    private MultiValueMap<String, Object> createMultipartBody(ImageUploadRequest request) 
            throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Agregar archivos de imágenes
        for (MultipartFile file : request.getImagesFiles()) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("imagesFiles", resource);
        }
        
        // Agregar parámetros
        body.add("modelQuality", request.getModelQuality().toString());
        body.add("textureQuality", request.getTextureQuality().toString());
        body.add("fileFormat", request.getFileFormat());
        body.add("isMask", request.getIsMask().toString());
        body.add("textureSmoothing", request.getTextureSmoothing().toString());
        
        return body;
    }
    
    /**
     * Crea el body multipart para video con todos los parámetros
     */
    private MultiValueMap<String, Object> createVideoMultipartBody(VideoUploadRequest request) 
            throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Agregar archivo de video
        MultipartFile videoFile = request.getVideoFile();
        ByteArrayResource resource = new ByteArrayResource(videoFile.getBytes()) {
            @Override
            public String getFilename() {
                return videoFile.getOriginalFilename();
            }
        };
        body.add("videoFile", resource);
        
        // Agregar parámetros
        body.add("modelQuality", request.getModelQuality().toString());
        body.add("textureQuality", request.getTextureQuality().toString());
        body.add("fileFormat", request.getFileFormat());
        body.add("isMask", request.getIsMask().toString());
        body.add("textureSmoothing", request.getTextureSmoothing().toString());
        
        return body;
    }
    
    /**
     * Crea el body multipart para video Featureless (solo video y formato)
     */
    private MultiValueMap<String, Object> createFeaturelessVideoMultipartBody(FeaturelessVideoUploadRequest request) 
            throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Agregar archivo de video
        MultipartFile videoFile = request.getVideoFile();
        ByteArrayResource resource = new ByteArrayResource(videoFile.getBytes()) {
            @Override
            public String getFilename() {
                return videoFile.getOriginalFilename();
            }
        };
        body.add("videoFile", resource);
        
        // Agregar solo el formato de archivo (único parámetro requerido)
        body.add("fileFormat", request.getFileFormat());
        
        return body;
    }
    
    /**
     * Crea el body multipart para imágenes Featureless (solo imágenes y formato)
     */
    private MultiValueMap<String, Object> createFeaturelessImageMultipartBody(FeaturelessImageUploadRequest request) 
            throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Agregar archivos de imágenes
        for (MultipartFile file : request.getImagesFiles()) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("imagesFiles", resource);
        }
        
        // Agregar solo el formato de archivo (único parámetro requerido)
        body.add("fileFormat", request.getFileFormat());
        
        return body;
    }
} 