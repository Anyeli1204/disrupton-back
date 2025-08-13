package com.disrupton.auth.filter;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import com.disrupton.auth.service.AuthService;
import com.disrupton.user.service.UserService;
import com.disrupton.user.dto.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserService userService;
    private final RequestMappingHandlerMapping handlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Obtener el handler method para el request
            HandlerMethod handlerMethod = (HandlerMethod) handlerMapping.getHandler(request).getHandler();
            
            // Verificar si el método tiene la anotación @RequireRole
            RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
            RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            
            RequireRole annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
            
            if (annotation != null) {
                // Extraer token del header
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Token requerido\"}");
                    return;
                }
                
                String token = authHeader.substring(7);
                String userId = authService.getUserIdFromToken(token);
                
                if (userId == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Token inválido\"}");
                    return;
                }
                
                // Obtener usuario y su rol
                UserDto user = userService.getUserById(userId);
                if (user == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Usuario no encontrado\"}");
                    return;
                }
                
                UserRole userRole = UserRole.fromCode(user.getRole());
                List<UserRole> requiredRoles = Arrays.asList(annotation.value());
                
                // Verificar si el usuario tiene el rol requerido
                boolean hasRequiredRole = requiredRoles.stream()
                        .anyMatch(requiredRole -> {
                            switch (requiredRole) {
                                case ADMIN:
                                    return userRole.isAdmin();
                                case MODERATOR:
                                    return userRole.isModerator();
                                case GUIDE:
                                    return userRole.isGuide();
                                case PREMIUM:
                                    return userRole.isPremium();
                                case USER:
                                    return true; // Todos los usuarios pueden acceder
                                default:
                                    return userRole == requiredRole;
                            }
                        });
                
                if (!hasRequiredRole) {
                    log.warn("⚠️ Acceso denegado: Usuario {} con rol {} intentó acceder a endpoint que requiere roles: {}", 
                            userId, userRole.getCode(), requiredRoles);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Acceso denegado: Rol insuficiente\"}");
                    return;
                }
                
                log.info("✅ Acceso autorizado: Usuario {} con rol {} accedió a endpoint", userId, userRole.getCode());
            }
            
        } catch (Exception e) {
            log.error("❌ Error en RoleAuthorizationFilter: {}", e.getMessage());
            // Continuar con el filtro si hay error
        }
        
        filterChain.doFilter(request, response);
    }
}
