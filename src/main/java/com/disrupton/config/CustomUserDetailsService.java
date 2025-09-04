package com.disrupton.config;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.disrupton.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private Firestore firestore;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        try {
            // ✅ Búsqueda correcta por ID de documento
            DocumentSnapshot document = firestore.collection("users").document(userId).get().get();

            if (document.exists()) {
                String email = document.getString("email");
                String name = document.getString("name");
                String role = document.getString("role");
                String profileImageUrl = document.getString("profileImageUrl");
                Boolean isActive = document.getBoolean("isActive");

                // If role is null or empty, default to USER and update in database
                if (role == null || role.trim().isEmpty()) {
                    role = "USER";
                    try {
                        firestore.collection("users").document(userId).update("role", role).get();
                        log.info("✅ Updated user {} role to {}", userId, role);
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to update user role in database: {}", e.getMessage());
                    }
                }

                // Crear el objeto User personalizado que implementa UserDetails
                User user = new User();
                user.setUserId(userId);
                user.setEmail(email);
                user.setName(name);
                user.setRole(role);
                user.setProfileImageUrl(profileImageUrl);
                user.setIsActive(isActive != null ? isActive : true);
                user.setCreatedAt(LocalDateTime.now()); // Placeholder
                user.setUpdatedAt(LocalDateTime.now()); // Placeholder

                return new CustomUserPrincipal(user, role);
            }

            throw new UsernameNotFoundException("Usuario no encontrado con ID: " + userId);

        } catch (InterruptedException | ExecutionException e) {
            throw new UsernameNotFoundException("Error consultando usuario con ID: " + userId, e);
        }
    }

    // Clase interna que implementa UserDetails y contiene nuestro User personalizado
    public static class CustomUserPrincipal implements UserDetails {
        private final User user;
        private final Collection<? extends GrantedAuthority> authorities;

        public CustomUserPrincipal(User user, String role) {
            this.user = user;
            List<GrantedAuthority> auths = new ArrayList<>();
            // Default to USER role if no role is provided
            String effectiveRole = (role != null && !role.trim().isEmpty()) ? role : "USER";
            String roleWithPrefix = "ROLE_" + effectiveRole.toUpperCase();
            auths.add(new SimpleGrantedAuthority(roleWithPrefix));
            log.info("🔑 Creando CustomUserPrincipal - Usuario: {}, Rol original: {}, Rol efectivo: {}, Rol con prefijo: {}", 
                    user.getEmail(), role, effectiveRole, roleWithPrefix);
            this.authorities = auths;
        }

        public User getUser() {
            return user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return "default-password"; // Placeholder
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive() != null ? user.getIsActive() : true;
        }
    }
}
