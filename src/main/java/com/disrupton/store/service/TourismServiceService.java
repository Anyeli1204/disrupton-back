package com.disrupton.store.service;

import com.disrupton.quota.service.QuotaService;
import com.disrupton.store.dto.TourismServiceDto;
import com.disrupton.store.model.TourismService;
import com.disrupton.user.service.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Servicio para gestión de servicios turísticos
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TourismServiceService {

    private final Firestore firestore;
    private final QuotaService quotaService;
    private final UserService userService;
    private static final String COLLECTION_NAME = "tourismServices";

    /**
     * Obtener todos los servicios turísticos
     */
    public List<TourismServiceDto> getAllServices() {
        try {
            log.info("🗺️ Obteniendo todos los servicios turísticos desde Firestore");
            
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            List<DocumentSnapshot> documents = collection.get().get().getDocuments()
                    .stream()
                    .map(queryDoc -> (DocumentSnapshot) queryDoc)
                    .toList();
            
            List<TourismServiceDto> services = new ArrayList<>();
            for (DocumentSnapshot doc : documents) {
                TourismService service = doc.toObject(TourismService.class);
                if (service != null) {
                    service.setId(doc.getId());
                    services.add(convertToDto(service));
                }
            }
            
            log.info("✅ Se obtuvieron {} servicios turísticos", services.size());
            return services;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Error al obtener servicios turísticos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener servicios turísticos", e);
        }
    }

    /**
     * Obtener servicios por categoría
     */
    public List<TourismServiceDto> getServicesByCategory(TourismService.ServiceCategory category) {
        try {
            log.info("🏛️ Obteniendo servicios de categoría: {}", category);
            
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection.whereEqualTo("category", category.name());
            List<DocumentSnapshot> documents = query.get().get().getDocuments()
                    .stream()
                    .map(queryDoc -> (DocumentSnapshot) queryDoc)
                    .toList();
            
            List<TourismServiceDto> services = new ArrayList<>();
            for (DocumentSnapshot doc : documents) {
                TourismService service = doc.toObject(TourismService.class);
                if (service != null) {
                    service.setId(doc.getId());
                    services.add(convertToDto(service));
                }
            }
            
            log.info("✅ Se encontraron {} servicios en categoría {}", services.size(), category);
            return services;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Error al obtener servicios por categoría: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener servicios por categoría", e);
        }
    }

    /**
     * Buscar servicios con filtros
     */
    public List<TourismServiceDto> searchServices(String searchTerm, Double minPrice, Double maxPrice,
                                                String department, TourismService.DifficultyLevel difficulty,
                                                Integer minDuration, Integer maxDuration) {
        try {
            log.info("🔍 Buscando servicios con filtros múltiples");
            
            // Obtener todos los servicios y filtrar en memoria (para búsquedas complejas)
            List<TourismServiceDto> allServices = getAllServices();
            
            return allServices.stream()
                    .filter(service -> {
                        // Filtro por término de búsqueda
                        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                            String term = searchTerm.toLowerCase().trim();
                            if (!service.getName().toLowerCase().contains(term) &&
                                !service.getDescription().toLowerCase().contains(term) &&
                                !service.getLocation().toLowerCase().contains(term)) {
                                return false;
                            }
                        }
                        
                        // Filtro por precio mínimo
                        if (minPrice != null && service.getPricePerPerson() < minPrice) {
                            return false;
                        }
                        
                        // Filtro por precio máximo
                        if (maxPrice != null && service.getPricePerPerson() > maxPrice) {
                            return false;
                        }
                        
                        // Filtro por departamento
                        if (department != null && !department.trim().isEmpty()) {
                            if (!service.getDepartment().toLowerCase().contains(department.toLowerCase().trim())) {
                                return false;
                            }
                        }
                        
                        // Filtro por dificultad
                        if (difficulty != null) {
                            if (!service.getDifficulty().equals(difficulty.name())) {
                                return false;
                            }
                        }
                        
                        // Filtro por duración mínima (comentado por ahora)
                        // if (minDuration != null && service.getDurationHours() < minDuration) {
                        //     return false;
                        // }
                        
                        // Filtro por duración máxima (comentado por ahora)
                        // if (maxDuration != null && service.getDurationHours() > maxDuration) {
                        //     return false;
                        // }
                        
                        return true;
                    })
                    .sorted((s1, s2) -> Double.compare(s2.getRating(), s1.getRating())) // Ordenar por rating descendente
                    .toList();
                    
        } catch (Exception e) {
            log.error("❌ Error en búsqueda de servicios: {}", e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda de servicios", e);
        }
    }

    /**
     * Obtener servicio por ID
     */
    public TourismServiceDto getServiceById(String id) {
        try {
            log.info("🎯 Obteniendo servicio por ID: {}", id);
            
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(id).get().get();
            
            if (!doc.exists()) {
                throw new RuntimeException("Servicio no encontrado con ID: " + id);
            }
            
            TourismService service = doc.toObject(TourismService.class);
            if (service != null) {
                service.setId(doc.getId());
                return convertToDto(service);
            }
            
            throw new RuntimeException("Error al convertir servicio");
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Error al obtener servicio por ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener servicio", e);
        }
    }

    /**
     * Obtener servicios por guía
     */
    public List<TourismServiceDto> getServicesByGuide(String guideId) {
        try {
            log.info("🧭 Obteniendo servicios del guía: {}", guideId);
            
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection.whereEqualTo("guideId", guideId);
            List<DocumentSnapshot> documents = query.get().get().getDocuments()
                    .stream()
                    .map(queryDoc -> (DocumentSnapshot) queryDoc)
                    .toList();
            
            List<TourismServiceDto> services = new ArrayList<>();
            for (DocumentSnapshot doc : documents) {
                TourismService service = doc.toObject(TourismService.class);
                if (service != null) {
                    service.setId(doc.getId());
                    services.add(convertToDto(service));
                }
            }
            
            log.info("✅ Se encontraron {} servicios del guía {}", services.size(), guideId);
            return services;
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Error al obtener servicios del guía: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener servicios del guía", e);
        }
    }

    /**
     * Crear un nuevo servicio turístico (con validación de cuotas)
     */
    public TourismServiceDto createService(String guideId, TourismServiceDto serviceRequest) throws ExecutionException, InterruptedException {
        log.info("🗺️ Creando nuevo servicio para guía: {}", guideId);

        // Validar que el usuario es guía
        var user = userService.getUserById(guideId);
        if (user == null || !"GUIDE".equals(user.getRole())) {
            throw new IllegalArgumentException("Solo los guías pueden crear servicios turísticos");
        }

        // Verificar cuota mensual
        if (!quotaService.canUploadService(guideId)) {
            throw new IllegalStateException("Has excedido tu límite mensual de servicios. Puedes comprar servicios adicionales.");
        }

        // Crear el servicio
        TourismService service = new TourismService();
        String serviceId = UUID.randomUUID().toString();

        // Información básica
        service.setId(serviceId);
        service.setName(serviceRequest.getName());
        service.setDescription(serviceRequest.getDescription());
        service.setShortDescription(serviceRequest.getShortDescription());

        // Precio
        service.setPricePerPerson(serviceRequest.getPricePerPerson());
        service.setGroupPrice(serviceRequest.getGroupPrice());
        service.setCurrency(serviceRequest.getCurrency() != null ? serviceRequest.getCurrency() : "PEN");
        service.setFormattedPrice(serviceRequest.getFormattedPrice());

        // Imágenes
        service.setMainImageUrl(serviceRequest.getMainImageUrl());
        service.setAdditionalImages(serviceRequest.getAdditionalImages());

        // Ubicación
        service.setLocation(serviceRequest.getLocation());
        service.setDepartment(serviceRequest.getDepartment());
        service.setProvince(serviceRequest.getProvince());
        service.setDistrict(serviceRequest.getDistrict());
        service.setLatitude(serviceRequest.getLatitude());
        service.setLongitude(serviceRequest.getLongitude());
        service.setVisitedPlaces(serviceRequest.getVisitedPlaces());

        // Categorización
        if (serviceRequest.getCategory() != null) {
            for (TourismService.ServiceCategory category : TourismService.ServiceCategory.values()) {
                if (category.name().equals(serviceRequest.getCategory())) {
                    service.setCategory(category);
                    break;
                }
            }
        }

        if (serviceRequest.getType() != null) {
            for (TourismService.ServiceType type : TourismService.ServiceType.values()) {
                if (type.name().equals(serviceRequest.getType())) {
                    service.setType(type);
                    break;
                }
            }
        }

        if (serviceRequest.getDifficulty() != null) {
            for (TourismService.DifficultyLevel difficulty : TourismService.DifficultyLevel.values()) {
                if (difficulty.name().equals(serviceRequest.getDifficulty())) {
                    service.setDifficulty(difficulty);
                    break;
                }
            }
        }

        service.setTags(serviceRequest.getTags());

        // Duración y horarios
        service.setDuration(serviceRequest.getDuration());
        service.setSchedule(serviceRequest.getSchedule());
        service.setAvailableDays(serviceRequest.getAvailableDays());
        service.setIsFlexibleSchedule(serviceRequest.getIsFlexibleSchedule());

        // Guía
        service.setGuideId(guideId);
        service.setGuideName(user.getName());
        service.setGuideContact(user.getEmail());
        service.setSpokenLanguages(serviceRequest.getSpokenLanguages());

        // Capacidad y requisitos
        service.setMinGroupSize(serviceRequest.getMinGroupSize());
        service.setMaxGroupSize(serviceRequest.getMaxGroupSize());
        service.setAgeRestriction(serviceRequest.getAgeRestriction());
        service.setPhysicalRequirement(serviceRequest.getPhysicalRequirement());
        service.setIncluded(serviceRequest.getIncluded());
        service.setNotIncluded(serviceRequest.getNotIncluded());

        // Estado
        service.setIsAvailable(true);
        service.setRequiresAdvanceBooking(serviceRequest.getRequiresAdvanceBooking());
        service.setAdvanceBookingTime(serviceRequest.getAdvanceBookingTime());

        // Métricas iniciales
        service.setRating(0.0);
        service.setTotalRatings(0);
        service.setViewCount(0);
        service.setBookingCount(0);

        // Metadatos
        service.setCreatedAt(LocalDateTime.now().toString());
        service.setUpdatedAt(LocalDateTime.now().toString());
        service.setCreatedBy(guideId);

        // Guardar en Firestore
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME).document(serviceId).set(service);
        WriteResult result = future.get();

        // Registrar en el sistema de cuotas
        quotaService.recordServiceUpload(guideId);

        log.info("✅ Servicio creado exitosamente: {} para guía: {}. Timestamp: {}",
                serviceId, guideId, result.getUpdateTime());

        return convertToDto(service);
    }

    /**
     * Actualizar un servicio existente
     */
    public TourismServiceDto updateService(String serviceId, String guideId, TourismServiceDto serviceRequest)
            throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando servicio: {} por guía: {}", serviceId, guideId);

        // Verificar que el servicio existe y pertenece al guía
        DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(serviceId).get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Servicio no encontrado: " + serviceId);
        }

        TourismService existingService = doc.toObject(TourismService.class);
        if (!guideId.equals(existingService.getGuideId())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar este servicio");
        }

        // Actualizar campos modificables
        existingService.setName(serviceRequest.getName());
        existingService.setDescription(serviceRequest.getDescription());
        existingService.setShortDescription(serviceRequest.getShortDescription());
        existingService.setPricePerPerson(serviceRequest.getPricePerPerson());
        existingService.setGroupPrice(serviceRequest.getGroupPrice());
        existingService.setFormattedPrice(serviceRequest.getFormattedPrice());
        existingService.setMainImageUrl(serviceRequest.getMainImageUrl());
        existingService.setAdditionalImages(serviceRequest.getAdditionalImages());
        existingService.setDuration(serviceRequest.getDuration());
        existingService.setSchedule(serviceRequest.getSchedule());
        existingService.setAvailableDays(serviceRequest.getAvailableDays());
        existingService.setIsFlexibleSchedule(serviceRequest.getIsFlexibleSchedule());
        existingService.setMinGroupSize(serviceRequest.getMinGroupSize());
        existingService.setMaxGroupSize(serviceRequest.getMaxGroupSize());
        existingService.setIsAvailable(serviceRequest.getIsAvailable());
        existingService.setIncluded(serviceRequest.getIncluded());
        existingService.setNotIncluded(serviceRequest.getNotIncluded());
        existingService.setUpdatedAt(LocalDateTime.now().toString());

        // Guardar cambios
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME).document(serviceId).set(existingService);
        WriteResult result = future.get();

        log.info("✅ Servicio actualizado: {}. Timestamp: {}", serviceId, result.getUpdateTime());

        return convertToDto(existingService);
    }

    /**
     * Eliminar un servicio
     */
    public boolean deleteService(String serviceId, String guideId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando servicio: {} por guía: {}", serviceId, guideId);

        // Verificar que el servicio existe y pertenece al guía
        DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(serviceId).get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Servicio no encontrado: " + serviceId);
        }

        TourismService service = doc.toObject(TourismService.class);
        if (!guideId.equals(service.getGuideId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este servicio");
        }

        // Eliminar servicio
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME).document(serviceId).delete();
        WriteResult result = future.get();

        log.info("✅ Servicio eliminado: {}. Timestamp: {}", serviceId, result.getUpdateTime());
        return true;
    }

    /**
     * Obtener servicios destacados (featured)
     */
    public List<TourismServiceDto> getFeaturedServices() throws ExecutionException, InterruptedException {
        log.info("⭐ Obteniendo servicios destacados");

        List<TourismServiceDto> allServices = getAllServices();

        // Filtrar servicios de guías con featured activo
        List<TourismServiceDto> featuredServices = new ArrayList<>();

        for (TourismServiceDto service : allServices) {
            try {
                if (quotaService.hasFeaturedPlacement(service.getGuideId())) {
                    featuredServices.add(service);
                }
            } catch (Exception e) {
                log.warn("⚠️ Error verificando featured para guía {}: {}",
                        service.getGuideId(), e.getMessage());
            }
        }

        // Ordenar por rating y fecha de actualización
        featuredServices.sort((s1, s2) -> {
            int ratingComparison = Double.compare(s2.getRating(), s1.getRating());
            if (ratingComparison != 0) return ratingComparison;
            return s2.getUpdatedAt().compareTo(s1.getUpdatedAt());
        });

        log.info("✅ Servicios destacados encontrados: {}", featuredServices.size());
        return featuredServices;
    }

    /**
     * Incrementar contador de visualizaciones
     */
    public void incrementViewCount(String serviceId) {
        try {
            log.info("👁️ Incrementando contador de visualizaciones para servicio: {}", serviceId);

            firestore.collection(COLLECTION_NAME)
                    .document(serviceId)
                    .update("viewCount", com.google.cloud.firestore.FieldValue.increment(1),
                           "lastViewed", LocalDateTime.now().toString());

        } catch (Exception e) {
            log.warn("⚠️ Error al incrementar contador de visualizaciones: {}", e.getMessage());
        }
    }

    /**
     * Obtener estadísticas de servicios
     */
    public ServiceStatsDto getServiceStats() {
        try {
            log.info("📊 Calculando estadísticas de servicios");
            
            List<TourismServiceDto> allServices = getAllServices();
            
            if (allServices.isEmpty()) {
                return new ServiceStatsDto(0, 0.0, 0.0, 0.0, new HashMap<>(), 0);
            }
            
            // Calcular estadísticas
            int totalServices = allServices.size();
            double averagePrice = allServices.stream().mapToDouble(TourismServiceDto::getPricePerPerson).average().orElse(0.0);
            double averageRating = allServices.stream().mapToDouble(TourismServiceDto::getRating).average().orElse(0.0);
            // double averageDuration = allServices.stream().mapToDouble(TourismServiceDto::getDurationHours).average().orElse(0.0);
            double averageDuration = 0.0; // Placeholder since duration is String
            
            // Contar servicios por categoría
            Map<String, Integer> servicesByCategory = new HashMap<>();
            for (TourismServiceDto service : allServices) {
                servicesByCategory.merge(service.getCategory(), 1, Integer::sum);
            }
            
            // Contar servicios disponibles
            int availableServices = (int) allServices.stream()
                    .filter(service -> Boolean.TRUE.equals(service.getIsAvailable()))
                    .count();
            
            return new ServiceStatsDto(totalServices, averagePrice, averageRating, averageDuration, 
                                     servicesByCategory, availableServices);
                                     
        } catch (Exception e) {
            log.error("❌ Error al calcular estadísticas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al calcular estadísticas", e);
        }
    }

    /**
     * Convertir TourismService a TourismServiceDto
     */
    private TourismServiceDto convertToDto(TourismService service) {
        TourismServiceDto dto = new TourismServiceDto();
        
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setShortDescription(service.getShortDescription());
        dto.setCategory(service.getCategory() != null ? service.getCategory().name() : "");
        dto.setCategoryIcon(service.getCategoryIcon());
        dto.setPricePerPerson(service.getPricePerPerson());
        dto.setGroupPrice(service.getGroupPrice());
        dto.setFormattedPrice(service.getFormattedPrice());
        dto.setCurrency(service.getCurrency());
        dto.setMainImageUrl(service.getMainImageUrl());
        dto.setAdditionalImages(service.getAdditionalImages());
        dto.setLocation(service.getLocation());
        dto.setDepartment(service.getDepartment());
        dto.setProvince(service.getProvince());
        dto.setDistrict(service.getDistrict());
        dto.setLatitude(service.getLatitude());
        dto.setLongitude(service.getLongitude());
        dto.setVisitedPlaces(service.getVisitedPlaces());
        dto.setType(service.getType() != null ? service.getType().name() : "");
        dto.setTags(service.getTags());
        dto.setDifficulty(service.getDifficulty() != null ? service.getDifficulty().name() : "");
        dto.setDifficultyIcon(service.getDifficultyIcon());
        dto.setDuration(service.getDuration());
        dto.setSchedule(service.getSchedule());
        dto.setAvailableDays(service.getAvailableDays());
        dto.setIsFlexibleSchedule(service.getIsFlexibleSchedule());
        dto.setGuideId(service.getGuideId());
        dto.setGuideName(service.getGuideName());
        dto.setGuideContact(service.getGuideContact());
        dto.setSpokenLanguages(service.getSpokenLanguages());
        dto.setMinGroupSize(service.getMinGroupSize());
        dto.setMaxGroupSize(service.getMaxGroupSize());
        dto.setGroupSizeText(service.getGroupSizeText());
        dto.setAgeRestriction(service.getAgeRestriction());
        dto.setPhysicalRequirement(service.getPhysicalRequirement());
        dto.setIncluded(service.getIncluded());
        dto.setNotIncluded(service.getNotIncluded());
        dto.setIsAvailable(service.getIsAvailable());
        dto.setRequiresAdvanceBooking(service.getRequiresAdvanceBooking());
        dto.setAdvanceBookingTime(service.getAdvanceBookingTime());
        dto.setRating(service.getRating());
        dto.setTotalRatings(service.getTotalRatings());
        dto.setFormattedRating(service.getFormattedRating());
        dto.setViewCount(service.getViewCount());
        dto.setBookingCount(service.getBookingCount());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        dto.setCreatedBy(service.getCreatedBy());
        
        return dto;
    }

    /**
     * DTO para estadísticas de servicios
     */
    public static class ServiceStatsDto {
        private final int totalServices;
        private final double averagePrice;
        private final double averageRating;
        private final double averageDuration;
        private final Map<String, Integer> servicesByCategory;
        private final int availableServices;

        public ServiceStatsDto(int totalServices, double averagePrice, double averageRating, 
                             double averageDuration, Map<String, Integer> servicesByCategory, 
                             int availableServices) {
            this.totalServices = totalServices;
            this.averagePrice = averagePrice;
            this.averageRating = averageRating;
            this.averageDuration = averageDuration;
            this.servicesByCategory = servicesByCategory;
            this.availableServices = availableServices;
        }

        // Getters
        public int getTotalServices() { return totalServices; }
        public double getAveragePrice() { return averagePrice; }
        public double getAverageRating() { return averageRating; }
        public double getAverageDuration() { return averageDuration; }
        public Map<String, Integer> getServicesByCategory() { return servicesByCategory; }
        public int getAvailableServices() { return availableServices; }
    }
}
