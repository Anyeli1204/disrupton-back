package com.disrupton.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF solo para APIs REST
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/**").permitAll() // Permite todas las rutas
                        // Si deseas proteger ciertas rutas, cámbialo por:
                        // .requestMatchers("/api/privado/**").authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Habilita autenticación básica si lo necesitas

        return http.build();
    }
}
