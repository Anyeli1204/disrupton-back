package com.disrupton.admin.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import com.disrupton.user.dto.UserDto;
import com.disrupton.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@RequireRole(UserRole.ADMIN)
public class AdminController {

    private final UserService userService;

    /**
     * Dashboard de administración
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        log.info("📊 Accediendo al dashboard de administración");
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Dashboard de administración");
        dashboard.put("timestamp", System.currentTimeMillis());
        dashboard.put("admin", true);
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtener todos los usuarios (solo admin)
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("👥 Admin solicitando lista de todos los usuarios");
        
        try {
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Error obteniendo usuarios: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cambiar rol de usuario (solo admin)
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable String userId,
            @RequestParam String newRole) {
        
        log.info("🔄 Admin cambiando rol del usuario {} a {}", userId, newRole);
        
        try {
            // Validar que el rol sea válido
            UserRole role = UserRole.fromCode(newRole);
            
            // Actualizar rol del usuario
            UserDto updatedUser = userService.updateUserRole(userId, role.getCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rol actualizado exitosamente");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error cambiando rol: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al cambiar rol: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Activar/desactivar usuario (solo admin)
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(
            @PathVariable String userId,
            @RequestParam boolean active) {
        
        log.info("🔄 Admin {} usuario {}", active ? "activando" : "desactivando", userId);
        
        try {
            UserDto updatedUser = userService.updateUserStatus(userId, active);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Estado del usuario actualizado");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error cambiando estado: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Estadísticas del sistema (solo admin)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        log.info("📈 Admin solicitando estadísticas del sistema");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Obtener estadísticas básicas
            List<UserDto> allUsers = userService.getAllUsers();
            long totalUsers = allUsers.size();
            long activeUsers = allUsers.stream().filter(UserDto::getIsActive).count();
            long adminUsers = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            
            stats.put("totalUsers", totalUsers);
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", totalUsers - activeUsers);
            stats.put("adminUsers", adminUsers);
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
