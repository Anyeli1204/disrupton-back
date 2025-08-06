package com.disrupton.moderation.service;

import com.disrupton.moderation.dto.ModerationQueueDto;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseModerationService {

    private final Firestore db;
    private static final String COLLECTION_NAME = "moderation_queue";

    /**
     * Guardar nueva entrada de moderación
     */
    public ModerationQueueDto saveModerationEntry(ModerationQueueDto entry) throws ExecutionException, InterruptedException {
        log.info("📋 Guardando entrada de moderación para objeto: {}", entry.getObjectId());

        // Asignar estado por defecto si no existe
        if (entry.getStatus() == null) {
            entry.setStatus("pending");
        }

        // Guardar en Firestore
        var docRef = db.collection(COLLECTION_NAME).document();
        String entryId = docRef.getId();
        entry.setId(entryId);

        docRef.set(entry).get();

        log.info("✅ Entrada de moderación guardada con ID: {}", entryId);
        return entry;
    }

    /**
     * Obtener entradas pendientes de moderación
     */
    public List<ModerationQueueDto> getPendingEntries() throws ExecutionException, InterruptedException {
        log.info("📋 Obteniendo entradas pendientes de moderación");

        var query = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "pending")
                .get();

        return query.get().getDocuments().stream()
                .map(document -> {
                    ModerationQueueDto entry = document.toObject(ModerationQueueDto.class);
                    entry.setId(document.getId());
                    return entry;
                })
                .toList();
    }

    /**
     * Actualizar estado de moderación
     */
    public ModerationQueueDto updateModerationStatus(String entryId, ModerationQueueDto entry) throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando estado de moderación: {}", entryId);

        var docRef = db.collection(COLLECTION_NAME).document(entryId);
        var document = docRef.get().get();

        if (document.exists()) {
            entry.setId(entryId);
            docRef.set(entry).get();
            log.info("✅ Estado de moderación actualizado: {}", entryId);
            return entry;
        } else {
            log.warn("⚠️ Entrada de moderación no encontrada: {}", entryId);
            return null;
        }
    }

    /**
     * Eliminar entrada de moderación
     */
    public boolean deleteModerationEntry(String entryId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando entrada de moderación: {}", entryId);

        var docRef = db.collection(COLLECTION_NAME).document(entryId);
        var document = docRef.get().get();

        if (document.exists()) {
            docRef.delete().get();
            log.info("✅ Entrada de moderación eliminada: {}", entryId);
            return true;
        } else {
            log.warn("⚠️ Entrada de moderación no encontrada: {}", entryId);
            return false;
        }
    }
} 