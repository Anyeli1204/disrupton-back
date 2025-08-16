package com.disrupton.config;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
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
                String role = document.getString("role"); // Obtener el rol
                String password = document.getString("password");

                if (password == null) {
                    password = "default-password"; // Una contraseña placeholder
                }

                // ✅ Añadir el rol a las autoridades
                List<GrantedAuthority> authorities = new ArrayList<>();
                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }

                return new User(email, password, authorities); // Devolver UserDetails completo
            }

            throw new UsernameNotFoundException("Usuario no encontrado con ID: " + userId);

        } catch (InterruptedException | ExecutionException e) {
            throw new UsernameNotFoundException("Error consultando usuario con ID: " + userId, e);
        }
    }
}
