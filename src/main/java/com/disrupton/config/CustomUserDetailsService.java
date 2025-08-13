package com.disrupton.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private Firestore firestore;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Buscar usuario en Firebase por email o username
            var query = firestore.collection("users")
                    .whereEqualTo("email", username)
                    .limit(1);
            
            var querySnapshot = query.get().get();
            
            if (!querySnapshot.isEmpty()) {
                QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
                String userId = document.getId();
                String password = document.getString("password");
                
                if (password == null) {
                    password = "password"; // Contraseña por defecto
                }
                
                return User.builder()
                        .username(userId) // Usar el userId como username
                        .password(password)
                        .authorities(new ArrayList<>())
                        .build();
            }
            
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            
        } catch (InterruptedException | ExecutionException e) {
            throw new UsernameNotFoundException("Error consultando usuario: " + username, e);
        }
    }
}
