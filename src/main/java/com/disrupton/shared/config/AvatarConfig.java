package com.disrupton.shared.config;

import com.disrupton.avatar.service.AvatarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

/**
 * Configuración para inicializar los 3 avatares predeterminados al iniciar la aplicación
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AvatarConfig implements ApplicationRunner {

    private final AvatarService avatarService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Inicializando avatares predeterminados...");
            avatarService.initializeDefaultAvatars();
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error al inicializar avatares predeterminados: {}", e.getMessage(), e);
        }
    }
}
