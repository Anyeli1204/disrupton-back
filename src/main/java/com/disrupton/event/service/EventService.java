package com.disrupton.event.service;

import com.disrupton.event.dto.EventCreateRequest;
import com.disrupton.event.dto.EventResponseDto;
import com.disrupton.event.dto.EventUpdateRequest;
import com.disrupton.event.model.Event;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final Firestore firestore;
    private static final String EVENTS_COLLECTION = "events";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Crear nuevo evento (solo ADMIN)
     */
    public EventResponseDto createEvent(EventCreateRequest request, String adminId) {
        try {
            String eventId = UUID.randomUUID().toString();
            Timestamp now = Timestamp.now();
            Timestamp eventDateTime = parseDateTime(request.getDateTime());

            Event event = Event.builder()
                    .id(eventId)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .imageUrl(request.getImageUrl())
                    .dateTime(eventDateTime)
                    .location(request.getLocation())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .createdBy(adminId)
                    .createdAt(now)
                    .updatedAt(now)
                    .isActive(true)
                    .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                    .attendeesCount(0)
                    .build();

            // Guardar en Firestore
            firestore.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .set(eventToMap(event))
                    .get();

            log.info("Evento creado exitosamente: {} por admin: {}", event.getTitle(), adminId);
            return eventToResponseDto(event);

        } catch (Exception e) {
            log.error("Error al crear evento: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear evento", e);
        }
    }

    /**
     * Obtener todos los eventos activos (para USER y ADMIN)
     */
    public List<EventResponseDto> getAllActiveEvents() {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(EVENTS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .orderBy("dateTime")
                    .get()
                    .get()
                    .getDocuments();

            if (documents.isEmpty()) {
                // Si no hay eventos, crear algunos eventos de prueba
                createSampleEvents();
                // Volver a consultar
                documents = firestore.collection(EVENTS_COLLECTION)
                        .whereEqualTo("isActive", true)
                        .orderBy("dateTime")
                        .get()
                        .get()
                        .getDocuments();
            }

            return documents.stream()
                    .map(this::documentToEvent)
                    .map(this::eventToResponseDto)
                    .collect(Collectors.toList());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error al obtener eventos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener eventos", e);
        }
    }

    /**
     * Obtener todos los eventos (para ADMIN) incluyendo inactivos
     */
    public List<EventResponseDto> getAllEvents() {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(EVENTS_COLLECTION)
                    .orderBy("createdAt")
                    .get()
                    .get()
                    .getDocuments();

            return documents.stream()
                    .map(this::documentToEvent)
                    .map(this::eventToResponseDto)
                    .collect(Collectors.toList());

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error al obtener todos los eventos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener eventos", e);
        }
    }

    /**
     * Obtener evento por ID
     */
    public EventResponseDto getEventById(String eventId) {
        try {
            DocumentSnapshot document = firestore.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .get()
                    .get();

            if (!document.exists()) {
                throw new IllegalArgumentException("Evento no encontrado");
            }

            Event event = documentToEvent(document);
            return eventToResponseDto(event);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error al obtener evento {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener evento", e);
        }
    }

    /**
     * Actualizar evento (solo ADMIN)
     */
    public EventResponseDto updateEvent(String eventId, EventUpdateRequest request) {
        try {
            DocumentReference docRef = firestore.collection(EVENTS_COLLECTION).document(eventId);
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                throw new IllegalArgumentException("Evento no encontrado");
            }

            Event currentEvent = documentToEvent(document);
            
            // Actualizar campos
            Event.EventBuilder updatedEventBuilder = currentEvent.toBuilder()
                    .updatedAt(Timestamp.now());

            if (request.getTitle() != null) {
                updatedEventBuilder.title(request.getTitle());
            }
            if (request.getDescription() != null) {
                updatedEventBuilder.description(request.getDescription());
            }
            if (request.getImageUrl() != null) {
                updatedEventBuilder.imageUrl(request.getImageUrl());
            }
            if (request.getDateTime() != null) {
                updatedEventBuilder.dateTime(parseDateTime(request.getDateTime()));
            }
            if (request.getLocation() != null) {
                updatedEventBuilder.location(request.getLocation());
            }
            if (request.getLatitude() != null) {
                updatedEventBuilder.latitude(request.getLatitude());
            }
            if (request.getLongitude() != null) {
                updatedEventBuilder.longitude(request.getLongitude());
            }
            if (request.getTags() != null) {
                updatedEventBuilder.tags(request.getTags());
            }
            if (request.getIsActive() != null) {
                updatedEventBuilder.isActive(request.getIsActive());
            }

            Event updatedEvent = updatedEventBuilder.build();

            // Guardar cambios
            docRef.set(eventToMap(updatedEvent)).get();

            log.info("Evento actualizado exitosamente: {}", eventId);
            return eventToResponseDto(updatedEvent);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error al actualizar evento {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar evento", e);
        }
    }

    /**
     * Eliminar evento (solo ADMIN)
     */
    public void deleteEvent(String eventId) {
        try {
            DocumentSnapshot document = firestore.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .get()
                    .get();

            if (!document.exists()) {
                throw new IllegalArgumentException("Evento no encontrado");
            }

            firestore.collection(EVENTS_COLLECTION)
                    .document(eventId)
                    .delete()
                    .get();

            log.info("Evento eliminado exitosamente: {}", eventId);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Error al eliminar evento {}: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar evento", e);
        }
    }

    /**
     * Crear eventos de prueba si la base de datos está vacía
     */
    private void createSampleEvents() {
        try {
            log.info("Creando eventos de prueba...");

            List<Event> sampleEvents = Arrays.asList(
                Event.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Festival de Arte Cusqueño")
                    .description("Un festival que celebra el arte tradicional y contemporáneo de Cusco, con exposiciones de artesanos locales, demostraciones en vivo y talleres interactivos.")
                    .imageUrl("https://images.unsplash.com/photo-1561575180-c34fa5e4e44a?w=800")
                    .dateTime(Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(7, ChronoUnit.DAYS).getEpochSecond(), 0))
                    .location("Plaza de Armas, Cusco")
                    .latitude(-13.5170)
                    .longitude(-71.9785)
                    .createdBy("system")
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .isActive(true)
                    .tags(Arrays.asList("arte", "cultura", "cusco", "festival"))
                    .attendeesCount(0)
                    .build(),

                Event.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Concierto de Música Andina")
                    .description("Noche de música tradicional andina con artistas reconocidos de todo el Perú. Disfruta de quenas, charangos y voces que nos conectan con nuestras raíces.")
                    .imageUrl("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800")
                    .dateTime(Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(14, ChronoUnit.DAYS).getEpochSecond(), 0))
                    .location("Anfiteatro del Parque de la Exposición, Lima")
                    .latitude(-12.0704)
                    .longitude(-77.0359)
                    .createdBy("system")
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .isActive(true)
                    .tags(Arrays.asList("música", "andina", "concierto", "lima"))
                    .attendeesCount(0)
                    .build(),

                Event.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Feria Gastronómica Peruana")
                    .description("Degusta los sabores más auténticos del Perú en esta feria gastronómica que reúne a los mejores chefs y cocineros tradicionales del país.")
                    .imageUrl("https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800")
                    .dateTime(Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(21, ChronoUnit.DAYS).getEpochSecond(), 0))
                    .location("Larcomar, Miraflores")
                    .latitude(-12.1267)
                    .longitude(-77.0299)
                    .createdBy("system")
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .isActive(true)
                    .tags(Arrays.asList("gastronomía", "comida", "perú", "feria"))
                    .attendeesCount(0)
                    .build(),

                Event.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Taller de Textiles Tradicionales")
                    .description("Aprende las técnicas ancestrales del tejido peruano de la mano de artesanas expertas. Incluye materiales y certificado de participación.")
                    .imageUrl("https://images.unsplash.com/photo-1594736797933-d0401ba2fe65?w=800")
                    .dateTime(Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(28, ChronoUnit.DAYS).getEpochSecond(), 0))
                    .location("Centro Cultural de San Blas, Cusco")
                    .latitude(-13.5089)
                    .longitude(-71.9770)
                    .createdBy("system")
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .isActive(true)
                    .tags(Arrays.asList("textiles", "taller", "artesanía", "cusco"))
                    .attendeesCount(0)
                    .build(),

                Event.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Exposición de Fotografía: Perú Milenario")
                    .description("Una muestra fotográfica que captura la esencia del Perú ancestral y moderno, explorando paisajes, rostros y tradiciones que perduran en el tiempo.")
                    .imageUrl("https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800")
                    .dateTime(Timestamp.ofTimeSecondsAndNanos(Instant.now().plus(35, ChronoUnit.DAYS).getEpochSecond(), 0))
                    .location("Museo de Arte de Lima (MALI)")
                    .latitude(-12.0642)
                    .longitude(-77.0347)
                    .createdBy("system")
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .isActive(true)
                    .tags(Arrays.asList("fotografía", "exposición", "arte", "lima"))
                    .attendeesCount(0)
                    .build()
            );

            for (Event event : sampleEvents) {
                firestore.collection(EVENTS_COLLECTION)
                        .document(event.getId())
                        .set(eventToMap(event))
                        .get();
            }

            log.info("Eventos de prueba creados exitosamente: {} eventos", sampleEvents.size());

        } catch (Exception e) {
            log.error("Error al crear eventos de prueba: {}", e.getMessage(), e);
        }
    }

    // Métodos auxiliares

    private Timestamp parseDateTime(String dateTimeStr) {
        try {
            Instant instant = Instant.parse(dateTimeStr);
            return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use ISO 8601 format", e);
        }
    }

    private Event documentToEvent(DocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        if (data == null) {
            throw new IllegalArgumentException("Documento de evento inválido");
        }

        return Event.builder()
                .id(document.getId())
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .imageUrl((String) data.get("imageUrl"))
                .dateTime((Timestamp) data.get("dateTime"))
                .location((String) data.get("location"))
                .latitude(data.get("latitude") != null ? ((Number) data.get("latitude")).doubleValue() : null)
                .longitude(data.get("longitude") != null ? ((Number) data.get("longitude")).doubleValue() : null)
                .createdBy((String) data.get("createdBy"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt((Timestamp) data.get("updatedAt"))
                .isActive((Boolean) data.getOrDefault("isActive", true))
                .tags((List<String>) data.getOrDefault("tags", new ArrayList<>()))
                .attendeesCount(data.get("attendeesCount") != null ? ((Number) data.get("attendeesCount")).intValue() : 0)
                .build();
    }

    private Map<String, Object> eventToMap(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("title", event.getTitle());
        map.put("description", event.getDescription());
        map.put("imageUrl", event.getImageUrl());
        map.put("dateTime", event.getDateTime());
        map.put("location", event.getLocation());
        map.put("latitude", event.getLatitude());
        map.put("longitude", event.getLongitude());
        map.put("createdBy", event.getCreatedBy());
        map.put("createdAt", event.getCreatedAt());
        map.put("updatedAt", event.getUpdatedAt());
        map.put("isActive", event.isActive());
        map.put("tags", event.getTags());
        map.put("attendeesCount", event.getAttendeesCount());
        return map;
    }

    private EventResponseDto eventToResponseDto(Event event) {
        // Formatear fecha
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                event.getDateTime().toDate().toInstant(),
                ZoneId.systemDefault()
        );
        
        // Calcular si el evento ya pasó
        boolean isPastEvent = dateTime.isBefore(LocalDateTime.now());
        
        // Calcular tiempo hasta el evento
        String timeUntilEvent = calculateTimeUntilEvent(dateTime);

        return EventResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .dateTime(dateTime.format(DATE_FORMATTER))
                .location(event.getLocation())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt().toDate().toInstant().toString())
                .updatedAt(event.getUpdatedAt().toDate().toInstant().toString())
                .isActive(event.isActive())
                .tags(event.getTags())
                .attendeesCount(event.getAttendeesCount())
                .isPastEvent(isPastEvent)
                .timeUntilEvent(timeUntilEvent)
                .build();
    }

    private String calculateTimeUntilEvent(LocalDateTime eventDateTime) {
        LocalDateTime now = LocalDateTime.now();
        
        if (eventDateTime.isBefore(now)) {
            return "Evento pasado";
        }
        
        long days = ChronoUnit.DAYS.between(now, eventDateTime);
        
        if (days > 0) {
            return days + " día" + (days > 1 ? "s" : "");
        }
        
        long hours = ChronoUnit.HOURS.between(now, eventDateTime);
        if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "");
        }
        
        long minutes = ChronoUnit.MINUTES.between(now, eventDateTime);
        return minutes + " minuto" + (minutes > 1 ? "s" : "");
    }
}
