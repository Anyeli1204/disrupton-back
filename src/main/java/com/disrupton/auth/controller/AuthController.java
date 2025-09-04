package com.disrupton.auth.controller;

import com.disrupton.auth.dto.AuthResponse;
import com.disrupton.auth.dto.LoginRequest;
import com.disrupton.auth.dto.RegisterRequest;
import com.disrupton.auth.service.AuthService;
import com.disrupton.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("📝 Registrando nuevo usuario: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Autentica un usuario existente
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("🔐 Autenticando usuario: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Refresca el token de acceso
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("🔄 Refrescando token");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token de refresco requerido"));
        }
        
        String refreshToken = authHeader.substring(7);
        AuthResponse response = authService.refreshToken(refreshToken);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("🚪 Cerrando sesión");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token requerido"));
        }
        
        // Extraer userId del token
        String token = authHeader.substring(7);
        String userId = authService.getUserIdFromToken(token);
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token inválido"));
        }
        
        AuthResponse response = authService.logout(userId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verifica si un token es válido
     */
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("✅ Verificando token");
        
        // Si no hay header de autorización, solo verificar que el endpoint funciona
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .message("API funcionando correctamente")
                    .build());
        }
        
        String token = authHeader.substring(7);
        String userId = authService.getUserIdFromToken(token);
        
        if (userId != null) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .success(true)
                    .message("Token válido")
                    .userId(userId)
                    .build());
        } else {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token inválido"));
        }
    }

    /**
     * Debug endpoint para ver información del usuario actual
     */
    @GetMapping("/debug/user-info")
    public ResponseEntity<Map<String, Object>> debugUserInfo(@AuthenticationPrincipal User currentUser, 
                                                            Authentication authentication) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        if (currentUser != null) {
            debugInfo.put("userId", currentUser.getUserId());
            debugInfo.put("email", currentUser.getEmail());
            debugInfo.put("role", currentUser.getRole());
            debugInfo.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            debugInfo.put("currentUser", "null");
        }
        
        debugInfo.put("authenticated", authentication != null && authentication.isAuthenticated());
        debugInfo.put("principal", authentication != null ? authentication.getPrincipal().getClass().getSimpleName() : "null");
        
        return ResponseEntity.ok(debugInfo);
    }
}
