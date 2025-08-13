package com.disrupton.user.controller;

import com.disrupton.user.dto.UserDto;
import com.disrupton.user.dto.UserRequest;
import com.disrupton.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            log.info("Getting all users");
            List<UserDto> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId) {
        try {
            log.info("Getting user by ID: {}", userId);
            UserDto user = userService.getUserById(userId);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create new user
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserRequest request) {
        try {
            log.info("Creating new user: {}", request.getEmail());
            UserDto createdUser = userService.createUser(request);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable String userId,
            @RequestBody UserRequest request) {
        try {
            log.info("Updating user: {}", userId);
            UserDto updatedUser = userService.updateUser(userId, request);
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            log.info("Deleting user: {}", userId);
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"User deleted successfully\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        try {
            log.info("Getting user by email: {}", email);
            UserDto user = userService.getUserByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting user by email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String role) {
        try {
            log.info("Getting users by role: {}", role);
            List<UserDto> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users by role: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search users by name or email
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        try {
            log.info("Searching users with query: {}", query);
            List<UserDto> users = userService.searchUsers(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user activity
     */
    @GetMapping("/{userId}/activity")
    public ResponseEntity<Object> getUserActivity(@PathVariable String userId) {
        try {
            log.info("Getting user activity: {}", userId);
            Object activity = userService.getUserActivity(userId);
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            log.error("Error getting user activity: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Enable premium access for user (when they pay for a tour guide)
     */
    @PostMapping("/{userId}/enable-premium")
    public ResponseEntity<UserDto> enablePremiumAccess(
            @PathVariable String userId,
            @RequestParam(defaultValue = "30") int daysDuration) {
        try {
            log.info("Enabling premium access for user: {} for {} days", userId, daysDuration);
            UserDto updatedUser = userService.enablePremiumAccess(userId, daysDuration);
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error enabling premium access: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if user has active premium access
     */
    @GetMapping("/{userId}/premium-status")
    public ResponseEntity<Map<String, Object>> getPremiumStatus(@PathVariable String userId) {
        try {
            log.info("Checking premium status for user: {}", userId);
            boolean hasPremium = userService.hasActivePremiumAccess(userId);
            UserDto user = userService.getUserById(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("hasPremiumAccess", hasPremium);
            response.put("isPremium", user != null ? user.getIsPremium() : false);
            response.put("premiumExpiresAt", user != null ? user.getPremiumExpiresAt() : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking premium status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
