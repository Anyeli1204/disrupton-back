package com.disrupton.auth.service;

import com.disrupton.auth.dto.AuthResponse;
import com.disrupton.auth.dto.LoginRequest;
import com.disrupton.auth.dto.RegisterRequest;
import com.disrupton.user.model.User;
import com.disrupton.user.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    /**
     * Registra un nuevo usuario
     */
    public AuthResponse register(RegisterRequest request) {
        try {
            log.info("🔐 Registrando nuevo usuario: {}", request.getEmail());
            
            // Crear usuario en Firebase Auth
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getDisplayName())
                    .setEmailVerified(false);
            
            // Solo agregar teléfono si no está vacío y tiene formato válido
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                String phoneNumber = request.getPhoneNumber().trim();
                // Verificar que tenga formato E.164 (empiece con +)
                if (phoneNumber.startsWith("+")) {
                    createRequest.setPhoneNumber(phoneNumber);
                } else {
                    log.warn("⚠️ Número de teléfono sin formato E.164, omitiendo: {}", phoneNumber);
                }
            }

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);
            String authUserId = userRecord.getUid();
            // Crear token personalizado
            String customToken = FirebaseAuth.getInstance().createCustomToken(userRecord.getUid());
            
            // Generar JWT tokens
            String accessToken = jwtService.generateToken(userRecord.getUid(), userRecord.getEmail());
            String refreshToken = jwtService.generateRefreshToken(userRecord.getUid());
            
            // Crear usuario en Firestore
            User user = new User();
            user.setUserId(userRecord.getUid());
            user.setEmail(userRecord.getEmail());
            user.setName(userRecord.getDisplayName());
            user.setRole("USER");
            user.setIsActive(true);
            user.setCreatedAt(java.time.LocalDateTime.now());
            user.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Crear UserRequest para el servicio
            com.disrupton.user.dto.UserRequest userRequest = new com.disrupton.user.dto.UserRequest();
            userRequest.setEmail(userRecord.getEmail());
            userRequest.setName(userRecord.getDisplayName());
            userRequest.setRole("USER");
            
            userService.createUser(userRequest, authUserId);
            
            log.info("✅ Usuario registrado exitosamente: {}", userRecord.getUid());
            
            return AuthResponse.success(
                accessToken, 
                refreshToken, 
                userRecord.getUid(), 
                userRecord.getEmail(), 
                userRecord.getDisplayName()
            );
            
        } catch (FirebaseAuthException e) {
            log.error("❌ Error registrando usuario: {}", e.getMessage());
            return AuthResponse.error("Error al registrar usuario: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado: {}", e.getMessage());
            return AuthResponse.error("Error interno del servidor");
        }
    }

    /**
     * Autentica un usuario existente
     */
    public AuthResponse login(LoginRequest request) {
        try {
            log.info("🔐 Autenticando usuario: {}", request.getEmail());
            
            // Verificar credenciales con Firebase Auth
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
            
            // En una implementación real, verificarías la contraseña aquí
            // Por ahora, asumimos que el usuario existe y es válido
            
            // Generar JWT tokens
            String accessToken = jwtService.generateToken(userRecord.getUid(), userRecord.getEmail());
            String refreshToken = jwtService.generateRefreshToken(userRecord.getUid());
            
            log.info("✅ Usuario autenticado exitosamente: {}", userRecord.getUid());
            
            return AuthResponse.success(
                accessToken, 
                refreshToken, 
                userRecord.getUid(), 
                userRecord.getEmail(), 
                userRecord.getDisplayName()
            );
            
        } catch (FirebaseAuthException e) {
            log.error("❌ Error autenticando usuario: {}", e.getMessage());
            return AuthResponse.error("Credenciales inválidas");
        } catch (Exception e) {
            log.error("❌ Error inesperado: {}", e.getMessage());
            return AuthResponse.error("Error interno del servidor");
        }
    }

    /**
     * Refresca el token de acceso
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            String userId = jwtService.validateRefreshToken(refreshToken);
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
            
            String newAccessToken = jwtService.generateToken(userId, userRecord.getEmail());
            String newRefreshToken = jwtService.generateRefreshToken(userId);
            
            return AuthResponse.success(
                newAccessToken, 
                newRefreshToken, 
                userId, 
                userRecord.getEmail(), 
                userRecord.getDisplayName()
            );
            
        } catch (Exception e) {
            log.error("❌ Error refrescando token: {}", e.getMessage());
            return AuthResponse.error("Token de refresco inválido");
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    public AuthResponse logout(String userId) {
        try {
            log.info("🔐 Cerrando sesión para usuario: {}", userId);
            
            // En una implementación real, invalidarías el token aquí
            // Por ahora, solo retornamos éxito
            
            return AuthResponse.builder()
                    .success(true)
                    .message("Sesión cerrada exitosamente")
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ Error cerrando sesión: {}", e.getMessage());
            return AuthResponse.error("Error al cerrar sesión");
        }
    }

    /**
     * Extrae el userId de un token
     */
    public String getUserIdFromToken(String token) {
        try {
            return jwtService.validateToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
