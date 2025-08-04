package com.disrupton.controller;

import com.disrupton.dto.UserDto;
import com.disrupton.service.FirebaseUserService;
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
public class UserController {

    private final FirebaseUserService userService;

    /**
     * Crear nuevo usuario
     */
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
        try {
            log.info("👤 Creando nuevo usuario: {}", user.getEmail());
            UserDto savedUser = userService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            log.error("❌ Error al crear usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        try {
            log.info("🔍 Obteniendo usuario: {}", userId);
            UserDto user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("❌ Error al obtener usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener todos los usuarios
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            log.info("📋 Obteniendo todos los usuarios");
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Error al obtener usuarios: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener usuarios por rol
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        try {
            log.info("👥 Obteniendo usuarios con rol: {}", role);
            List<UserDto> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Error al obtener usuarios por rol: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar usuario
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String userId, @RequestBody UserDto user) {
        try {
            log.info("🔄 Actualizando usuario: {}", userId);
            UserDto updatedUser = userService.updateUser(userId, user);
            if (updatedUser == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("❌ Error al actualizar usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Eliminar usuario
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            log.info("🗑️ Eliminando usuario: {}", userId);
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verificar si usuario existe
     */
    @GetMapping("/users/{userId}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable String userId) {
        try {
            log.info("🔍 Verificando existencia de usuario: {}", userId);
            boolean exists = userService.userExists(userId);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("❌ Error al verificar usuario: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}