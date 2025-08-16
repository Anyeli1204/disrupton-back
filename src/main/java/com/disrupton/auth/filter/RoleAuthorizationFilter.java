package com.disrupton.auth.filter;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleAuthorizationFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Prevenir el NullPointerException si no hay un controlador para la ruta.
            HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
            if (handlerChain == null) {
                filterChain.doFilter(request, response);
                return;
            }
            HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();

            // Buscar la anotación @RequireRole en el método o en la clase
            RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }

            // Si el endpoint no requiere un rol específico, continuar.
            if (annotation == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Obtener la autenticación que el filtro anterior (JwtAuthenticationFilter) ya estableció.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Usuario no autenticado\"}");
                return;
            }

            // 3. Obtener los roles del usuario YA CARGADOS, sin volver a consultar la base de datos.
            Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();
            List<UserRole> requiredRoles = Arrays.asList(annotation.value());

            // Convertir los roles del usuario a un formato comparable.
            // Spring Security añade el prefijo "ROLE_". Lo quitamos para comparar.
            List<String> userRoles = userAuthorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            // 4. Verificar si el usuario tiene al menos uno de los roles requeridos.
            boolean hasRequiredRole = requiredRoles.stream()
                    .anyMatch(requiredRole -> userRoles.contains(requiredRole.name()));

            if (!hasRequiredRole) {
                log.warn("⚠️ Acceso denegado: Usuario {} con roles {} intentó acceder a endpoint que requiere {}",
                        authentication.getName(), userRoles, requiredRoles);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Acceso denegado: Rol insuficiente\"}");
                return;
            }

        } catch (Exception e) {
            log.error("❌ Error inesperado en RoleAuthorizationFilter: {}", e.getMessage(), e);
            // En caso de un error inesperado, es más seguro denegar el acceso.
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error interno en el filtro de autorización\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}