package com.disrupton.socialPost.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.auth.enums.UserRole;
import com.disrupton.socialPost.dto.*;
import com.disrupton.socialPost.service.SocialPostFirestoreService;
import com.disrupton.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SocialPostController {
    
    private final SocialPostFirestoreService socialPostService;
    
    /**
     * Crear nueva publicación
     */
    @PostMapping("/posts")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<SocialPostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en createPost");
            return ResponseEntity.status(401).build();
        }
        
        log.info("🎨 Creating new social post for user: {}", currentUser.getUserId());
        
        SocialPostResponse response = socialPostService.createPost(request, currentUser);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtener posts (endpoint principal que usa Flutter)
     */
    @GetMapping("/posts")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<List<SocialPostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String department,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en getPosts");
            return ResponseEntity.status(401).build();
        }
        
        log.info("📱 Getting posts for user: {}, page: {}, limit: {}", currentUser.getUserId(), page, limit);
        
        Page<SocialPostResponse> posts = socialPostService.getFeedPosts(currentUser.getUserId(), page, limit);
        
        return ResponseEntity.ok(posts.getContent());
    }

    /**
     * Obtener feed principal
     */
    @GetMapping("/feed")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Page<SocialPostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en getFeed");
            return ResponseEntity.status(401).build();
        }
        
        log.info("📱 Getting feed for user: {}, page: {}, size: {}", currentUser.getUserId(), page, size);
        
        Page<SocialPostResponse> posts = socialPostService.getFeedPosts(currentUser.getUserId(), page, size);
        
        return ResponseEntity.ok(posts);
    }
    
    /**
     * Obtener posts de usuario específico
     */
    @GetMapping("/users/{userId}/posts")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Page<SocialPostResponse>> getUserPosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("👤 Getting posts for user: {}, requested by: {}", userId, currentUser.getUserId());
        
        // TODO: Implementar en SocialPostFirestoreService
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * Buscar publicaciones
     */
    @GetMapping("/search")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Page<SocialPostResponse>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("🔍 Searching posts with query: '{}' for user: {}", query, currentUser.getUserId());
        
        Page<SocialPostResponse> posts = socialPostService.searchPosts(query, currentUser.getUserId(), page, size);
        
        return ResponseEntity.ok(posts);
    }
    
    /**
     * Obtener posts trending
     */
    @GetMapping("/trending")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Page<SocialPostResponse>> getTrendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("🔥 Getting trending posts for user: {}", currentUser.getUserId());
        
        // TODO: Implementar en SocialPostFirestoreService
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * Obtener posts guardados
     */
    @GetMapping("/saved")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Page<SocialPostResponse>> getSavedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("💾 Getting saved posts for user: {}", currentUser.getUserId());
        
        // TODO: Implementar en SocialPostFirestoreService
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * Dar/quitar like a una publicación
     */
    @PostMapping("/posts/{postId}/like")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable String postId,
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en toggleLike");
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
        }
        
        log.info("❤️ Toggling like for post: {} by user: {}", postId, currentUser.getUserId());
        
        boolean isLiked = socialPostService.toggleLike(postId, currentUser);
        
        return ResponseEntity.ok(Map.of(
                "isLiked", isLiked,
                "message", isLiked ? "Post liked successfully" : "Like removed successfully"
        ));
    }
    
    /**
     * Guardar/desguardar una publicación
     */
    @PostMapping("/posts/{postId}/save")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Map<String, Object>> toggleSave(
            @PathVariable String postId,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("💾 Toggling save for post: {} by user: {}", postId, currentUser.getUserId());
        
        boolean isSaved = socialPostService.toggleSave(postId, currentUser.getUserId());
        
        return ResponseEntity.ok(Map.of(
                "isSaved", isSaved,
                "message", isSaved ? "Post saved successfully" : "Post removed from saved"
        ));
    }
    
    /**
     * Crear comentario en una publicación
     */
    @PostMapping("/posts/{postId}/comments")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("💬 Creating comment for post: {} by user: {}", postId, currentUser.getUserId());
        
        // TODO: Implementar en SocialPostFirestoreService
        return ResponseEntity.ok(CommentResponse.builder()
                .id(UUID.randomUUID().toString())
                .postId(postId)
                .userId(currentUser.getUserId())
                .userName(currentUser.getName())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build());
    }
    
    /**
     * Obtener comentarios de una publicación
     */
    @GetMapping("/posts/{postId}/comments")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<Page<CommentResponse>> getPostComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        
        log.info("💬 Getting comments for post: {}", postId);
        
        // TODO: Implementar en SocialPostFirestoreService
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * Obtener tags trending
     */
    @GetMapping("/trending-tags")
    @RequireRole({UserRole.USER, UserRole.ADMIN, UserRole.MODERATOR, UserRole.GUIDE, UserRole.ARTISAN, UserRole.PREMIUM})
    public ResponseEntity<List<String>> getTrendingTags(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("🏷️ Getting trending tags, limit: {}", limit);
        
        // TODO: Implementar en SocialPostFirestoreService
        List<String> mockTags = List.of("cultura", "artesania", "peru", "turismo", "tradicion");
        return ResponseEntity.ok(mockTags);
    }
    
    /**
     * Crear posts de prueba (solo para desarrollo)
     */
    @PostMapping("/create-test-posts")
    @RequireRole({UserRole.ADMIN, UserRole.MODERATOR})
    public ResponseEntity<Map<String, Object>> createTestPosts(
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser == null) {
            log.error("❌ Usuario no autenticado en createTestPosts");
            return ResponseEntity.status(401).build();
        }
        
        log.info("🧪 Creating test posts for user: {}", currentUser.getUserId());
        
        try {
            // Crear algunos posts de ejemplo
            List<String> testPosts = List.of(
                "¡Acabo de visitar las increíbles ruinas de Machu Picchu! 🏔️ La cultura inca sigue siendo fascinante después de tantos siglos. #MachuPicchu #CulturaPeruana #Turismo",
                "Aprendiendo a tejer mantas tradicionales en Cusco 🧶 Es impresionante la técnica ancestral que usan los artesanos locales. #Artesania #Cusco #TejidoTradicional",
                "Probando la gastronomía local en el mercado de San Pedro 🍲 Los sabores del Perú nunca dejan de sorprenderme! #GastronomiaPeruana #Mercado #Sabores",
                "Participando en una ceremonia tradicional en el Valle Sagrado 🌟 La conexión con la naturaleza y los ancestros es única. #ValeSagrado #Ceremonias #Tradicion"
            );
            
            List<SocialPostResponse> createdPosts = new ArrayList<>();
            
            for (String content : testPosts) {
                CreatePostRequest request = new CreatePostRequest();
                request.setDescription(content); // Usar description en lugar de content
                request.setLocation("Cusco, Perú");
                request.setDepartment("Cusco");
                request.setPrivacy(CreatePostRequest.PostPrivacy.PUBLIC);
                request.setTags(List.of("cultura", "peru", "turismo")); // Usar List directamente
                
                SocialPostResponse post = socialPostService.createPost(request, currentUser);
                createdPosts.add(post);
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Posts de prueba creados exitosamente",
                    "postsCreated", createdPosts.size(),
                    "posts", createdPosts
            ));
            
        } catch (Exception e) {
            log.error("❌ Error creating test posts: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error al crear posts de prueba: " + e.getMessage()
            ));
        }
    }

    /**
     * Debug endpoint para ver información del usuario actual
     */
    @GetMapping("/debug/user-info")
    public ResponseEntity<Map<String, Object>> debugUserInfo(@AuthenticationPrincipal User currentUser, 
                                                            Authentication authentication) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        if (currentUser != null) {
            debugInfo.put("userId", currentUser.getUserId());
            debugInfo.put("email", currentUser.getEmail());
            debugInfo.put("role", currentUser.getRole());
            debugInfo.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        } else {
            debugInfo.put("currentUser", "null");
        }
        
        debugInfo.put("authenticated", authentication != null && authentication.isAuthenticated());
        debugInfo.put("principal", authentication != null ? authentication.getPrincipal().getClass().getSimpleName() : "null");
        
        return ResponseEntity.ok(debugInfo);
    }
}
