package com.disrupton.moderation.controller;

import com.disrupton.moderation.dto.ModerationQueueDto;
import com.disrupton.moderation.service.FirebaseModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/firebase/cultural")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FirebaseModerationController {

    private final FirebaseModerationService moderationService;

    /**
     * Crear nueva entrada en cola de moderación
     */
    @PostMapping("/moderation-queue")
    public ResponseEntity<ModerationQueueDto> createModerationEntry(@RequestBody ModerationQueueDto entry) {
        try {
            log.info("📋 Creando entrada de moderación para objeto: {}", entry.getObjectId());
            ModerationQueueDto savedEntry = moderationService.saveModerationEntry(entry);
            return ResponseEntity.ok(savedEntry);
        } catch (Exception e) {
            log.error("❌ Error al crear entrada de moderación: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener entradas pendientes de moderación
     */
    @GetMapping("/moderation-queue/pending")
    public ResponseEntity<List<ModerationQueueDto>> getPendingEntries() {
        try {
            log.info("📋 Obteniendo entradas pendientes de moderación");
            List<ModerationQueueDto> entries = moderationService.getPendingEntries();
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            log.error("❌ Error al obtener entradas de moderación: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar estado de moderación
     */
    @PutMapping("/moderation-queue/{entryId}/status")
    public ResponseEntity<ModerationQueueDto> updateModerationStatus(
            @PathVariable String entryId,
            @RequestBody ModerationQueueDto entry) {
        try {
            log.info("🔄 Actualizando estado de moderación: {}", entryId);
            ModerationQueueDto updatedEntry = moderationService.updateModerationStatus(entryId, entry);
            if (updatedEntry == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedEntry);
        } catch (Exception e) {
            log.error("❌ Error al actualizar moderación: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar entrada de moderación
     */
    @DeleteMapping("/moderation-queue/{entryId}")
    public ResponseEntity<?> deleteModerationEntry(@PathVariable String entryId) {
        try {
            log.info("🗑️ Eliminando entrada de moderación: {}", entryId);
            boolean deleted = moderationService.deleteModerationEntry(entryId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar entrada de moderación: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 