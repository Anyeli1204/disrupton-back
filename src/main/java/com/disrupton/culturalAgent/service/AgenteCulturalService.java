package com.disrupton.culturalAgent.service;

import com.disrupton.culturalAgent.dto.AgenteCulturalDto;
import com.disrupton.culturalAgent.model.AgenteCultural;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgenteCulturalService {

    private final Firestore firestore;

    /**
     * Obtiene todos los agentes culturales activos
     */
    public List<AgenteCulturalDto> obtenerTodosLosAgentes() throws ExecutionException, InterruptedException {
        log.info("🏛️ Obteniendo todos los agentes culturales");
        
        QuerySnapshot querySnapshot = firestore.collection("agentes_culturales")
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .get();

        List<AgenteCultural> agentes = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            AgenteCultural agente = document.toObject(AgenteCultural.class);
            agente.setId(document.getId());
            agentes.add(agente);
        }

        return agentes.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene agentes culturales por tipo
     */
    public List<AgenteCulturalDto> obtenerAgentesPorTipo(AgenteCultural.AgentType tipo) 
            throws ExecutionException, InterruptedException {
        log.info("🎨 Obteniendo agentes del tipo: {}", tipo);
        
        // Consulta simplificada temporalmente hasta que se cree el índice compuesto
        QuerySnapshot querySnapshot = firestore.collection("agentes_culturales")
                .whereEqualTo("type", tipo.toString())
                .whereEqualTo("isActive", true)
                .get()
                .get();

        List<AgenteCultural> agentes = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            AgenteCultural agente = document.toObject(AgenteCultural.class);
            agente.setId(document.getId());
            agentes.add(agente);
        }

        return agentes.stream()
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating())) // Ordenar por rating descendente en Java temporalmente
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /**
     * Busca agentes por nombre o especialidad
     */
    public List<AgenteCulturalDto> buscarAgentes(String termino, AgenteCultural.AgentType tipo) 
            throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando agentes con término: '{}' y tipo: {}", termino, tipo);
        
        Query query = firestore.collection("agentes_culturales")
                .whereEqualTo("isActive", true);
        
        if (tipo != null) {
            query = query.whereEqualTo("type", tipo.toString());
        }
        
        QuerySnapshot querySnapshot = query.get().get();

        List<AgenteCultural> agentes = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            AgenteCultural agente = document.toObject(AgenteCultural.class);
            agente.setId(document.getId());
            
            // Filtrar por término de búsqueda
            if (coincideConTermino(agente, termino)) {
                agentes.add(agente);
            }
        }

        return agentes.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene agentes por ubicación
     */
    public List<AgenteCulturalDto> obtenerAgentesPorUbicacion(String departamento) 
            throws ExecutionException, InterruptedException {
        log.info("📍 Obteniendo agentes del departamento: {}", departamento);
        
        QuerySnapshot querySnapshot = firestore.collection("agentes_culturales")
                .whereEqualTo("department", departamento)
                .whereEqualTo("isActive", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .get();

        List<AgenteCultural> agentes = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
            AgenteCultural agente = document.toObject(AgenteCultural.class);
            agente.setId(document.getId());
            agentes.add(agente);
        }

        return agentes.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte AgenteCultural a AgenteCulturalDto
     */
    private AgenteCulturalDto convertirADto(AgenteCultural agente) {
        return AgenteCulturalDto.builder()
                .id(agente.getId())
                .name(agente.getName())
                .imageUrl(agente.getImageUrl())
                .region(agente.getRegion())
                .description(agente.getDescription())
                .expertise(agente.getExpertise())
                .specialties(agente.getSpecialties())
                .type(agente.getType())
                .phone(agente.getPhone())
                .whatsapp(agente.getWhatsapp())
                .email(agente.getEmail())
                .rating(agente.getRating())
                .totalRatings(agente.getTotalRatings())
                .location(agente.getLocation())
                .latitude(agente.getLatitude())
                .longitude(agente.getLongitude())
                .workPhotoHighlight(agente.getWorkPhotos() != null && !agente.getWorkPhotos().isEmpty() 
                        ? agente.getWorkPhotos().get(0) : null)
                .isActive(agente.getIsActive())
                .department(agente.getDepartment())
                .province(agente.getProvince())
                .district(agente.getDistrict())
                // Campos derivados
                .typeIcon(agente.getTypeIcon())
                .typeLabel(agente.getTypeLabel())
                .primaryContact(agente.getPrimaryContact())
                .fullLocation(agente.getFullLocation())
                .formattedRating(agente.getRating() != null ? 
                        String.format("⭐ %.1f (%d)", agente.getRating(), agente.getTotalRatings() != null ? agente.getTotalRatings() : 0) : "")
                .mainSpecialty(agente.getType() == AgenteCultural.AgentType.ARTISAN ? 
                        agente.getCraftTechnique() : agente.getTourismType())
                .spokenLanguages(agente.getSpokenLanguages())
                .experienceDisplay(agente.getExperienceYears() != null ? 
                        agente.getExperienceYears() + " años de experiencia" : "")
                .build();
    }

    /**
     * Verifica si un agente coincide con el término de búsqueda
     */
    private boolean coincideConTermino(AgenteCultural agente, String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return true;
        }
        
        String terminoLower = termino.toLowerCase();
        
        return (agente.getName() != null && agente.getName().toLowerCase().contains(terminoLower)) ||
               (agente.getExpertise() != null && agente.getExpertise().toLowerCase().contains(terminoLower)) ||
               (agente.getDescription() != null && agente.getDescription().toLowerCase().contains(terminoLower)) ||
               (agente.getRegion() != null && agente.getRegion().toLowerCase().contains(terminoLower)) ||
               (agente.getSpecialties() != null && agente.getSpecialties().stream()
                       .anyMatch(specialty -> specialty.toLowerCase().contains(terminoLower)));
    }
}
