package com.disrupton.config;

import com.disrupton.auth.filter.JwtAuthenticationFilter;
import com.disrupton.auth.filter.RoleAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private RoleAuthorizationFilter roleAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactiva CSRF solo para APIs REST
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (sin autenticación)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/kiri-engine/**").permitAll()
                        .requestMatchers("/test-*.html").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        // Todas las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                                       )
                       .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                       .addFilterAfter(roleAuthorizationFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
