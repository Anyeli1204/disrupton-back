package com.disrupton.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret:disrupton-secret-key-2025}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un token de acceso JWT
     */
    public String generateToken(String userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "access");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un token de refresco JWT
     */
    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida un token JWT y retorna el userId
     */
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Verificar que sea un token de acceso
            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                throw new JwtException("Token inválido: no es un token de acceso");
            }

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ Error validando token: {}", e.getMessage());
            throw new JwtException("Token inválido");
        }
    }

    /**
     * Valida un token de refresco y retorna el userId
     */
    public String validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Verificar que sea un token de refresco
            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                throw new JwtException("Token inválido: no es un token de refresco");
            }

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("❌ Error validando refresh token: {}", e.getMessage());
            throw new JwtException("Token de refresco inválido");
        }
    }

    /**
     * Extrae el userId de un token sin validar
     */
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un token ha expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
