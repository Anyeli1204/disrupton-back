package com.disrupton.store.service;

import com.disrupton.store.dto.TourismServiceDto;
import com.disrupton.store.model.TourismService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
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
