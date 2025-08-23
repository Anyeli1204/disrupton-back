package com.disrupton.event.controller;

import com.disrupton.event.dto.EventCreateRequest;
import com.disrupton.event.dto.EventResponseDto;
import com.disrupton.event.dto.EventUpdateRequest;
import com.disrupton.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getEvents(
            @RequestParam(value = "all", defaultValue = "false") boolean includeInactive) {
        
        try {
            log.info("Solicitando eventos (includeInactive: {})", includeInactive);

            List<EventResponseDto> events;
            
            // Por ahora solo devolvemos eventos activos
            // TODO: Implementar lógica de roles cuando esté disponible
            if (includeInactive) {
                events = eventService.getAllEvents();
            } else {
                events = eventService.getAllActiveEvents();
            }

            log.info("Devolviendo {} eventos", events.size());
            return ResponseEntity.ok(events);

        } catch (Exception e) {
            log.error("Error al obtener eventos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable String eventId) {
        
        try {
            log.info("Solicitando evento {}", eventId);

            EventResponseDto event = eventService.getEventById(eventId);
            return ResponseEntity.ok(event);

        } catch (IllegalArgumentException e) {
            log.warn("Evento no encontrado: {}", eventId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al obtener evento {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDto> createEvent(@RequestBody EventCreateRequest request) {
        
        try {
            String adminId = "system"; // TODO: Obtener del contexto de autenticación
            
            log.info("Creando evento: {}", request.getTitle());

            EventResponseDto createdEvent = eventService.createEvent(request, adminId);
            
            log.info("Evento creado exitosamente: {}", createdEvent.getTitle());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos para crear evento: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al crear evento: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable String eventId,
            @RequestBody EventUpdateRequest request) {
        
        try {
            log.info("Actualizando evento: {}", eventId);

            EventResponseDto updatedEvent = eventService.updateEvent(eventId, request);
            
            log.info("Evento actualizado exitosamente: {}", eventId);
            return ResponseEntity.ok(updatedEvent);

        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar evento {}: {}", eventId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al actualizar evento {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable String eventId) {
        
        try {
            log.info("Eliminando evento: {}", eventId);

            eventService.deleteEvent(eventId);
            
            log.info("Evento eliminado exitosamente: {}", eventId);
            return ResponseEntity.ok(Map.of("message", "Evento eliminado exitosamente"));

        } catch (IllegalArgumentException e) {
            log.warn("Error al eliminar evento {}: {}", eventId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al eliminar evento {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{eventId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDto> toggleEventStatus(@PathVariable String eventId) {
        
        try {
            log.info("Cambiando estado del evento: {}", eventId);

            // Obtener evento actual
            EventResponseDto currentEvent = eventService.getEventById(eventId);
            
            // Crear request para cambiar solo el estado
            EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                    .isActive(!currentEvent.isActive())
                    .build();

            EventResponseDto updatedEvent = eventService.updateEvent(eventId, updateRequest);
            
            log.info("Estado del evento {} cambiado a: {}", eventId, updatedEvent.isActive());
            return ResponseEntity.ok(updatedEvent);

        } catch (IllegalArgumentException e) {
            log.warn("Error al cambiar estado del evento {}: {}", eventId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al cambiar estado del evento {}: {}", eventId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        try {
            log.info("Solicitando estadísticas de eventos");

            List<EventResponseDto> allEvents = eventService.getAllEvents();
            List<EventResponseDto> activeEvents = eventService.getAllActiveEvents();
            
            long totalEvents = allEvents.size();
            long activeEventsCount = activeEvents.size();
            long inactiveEvents = totalEvents - activeEventsCount;
            long upcomingEvents = activeEvents.stream()
                    .filter(event -> !event.isPastEvent())
                    .count();
            long pastEvents = activeEvents.stream()
                    .filter(EventResponseDto::isPastEvent)
                    .count();

            Map<String, Object> stats = Map.of(
                    "totalEvents", totalEvents,
                    "activeEvents", activeEventsCount,
                    "inactiveEvents", inactiveEvents,
                    "upcomingEvents", upcomingEvents,
                    "pastEvents", pastEvents
            );

            log.info("Estadísticas de eventos enviadas: {}", stats);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error al obtener estadísticas de eventos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
