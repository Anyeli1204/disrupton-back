package com.disrupton.socialPost.service;

import com.disrupton.socialPost.dto.*;
import com.disrupton.socialPost.model.SocialPostFirestore;
import com.disrupton.user.model.User;
import com.google.cloud.firestore.*;
import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialPostFirestoreService {
    
    private final Firestore firestore;
    
    private static final String POSTS_COLLECTION = "social_posts";
    private static final String LIKES_COLLECTION = "post_likes";
    private static final String COMMENTS_COLLECTION = "post_comments";
    private static final String SAVES_COLLECTION = "post_saves";
    
    /**
     * Crear una nueva publicación social
     */
    public SocialPostResponse createPost(CreatePostRequest request, User currentUser) {
        log.info("Creating new social post for user: {}", currentUser.getUserId());
        
        try {
            String postId = UUID.randomUUID().toString();
            
            // Convertir URLs de imágenes a formato de Firestore
            List<Map<String, Object>> images = new ArrayList<>();
            if (request.getImageUrls() != null) {
                for (int i = 0; i < request.getImageUrls().size(); i++) {
                    Map<String, Object> imageData = Map.of(
                            "id", UUID.randomUUID().toString(),
                            "imageUrl", request.getImageUrls().get(i),
                            "displayOrder", i,
                            "originalFileName", "image_" + i + ".jpg"
                    );
                    images.add(imageData);
                }
            }
            
            SocialPostFirestore post = SocialPostFirestore.builder()
                    .id(postId)
                    .userId(currentUser.getUserId())
                    .userName(currentUser.getName())
                    .userRole(currentUser.getRole())
                    .userProfileImageUrl(currentUser.getProfileImageUrl())
                    .description(request.getDescription())
                    .images(images)
                    .location(request.getLocation())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .department(request.getDepartment())
                    .tags(request.getTags())
                    .mentionedCulturalObjects(request.getMentionedCulturalObjects())
                    .privacy(request.getPrivacy().name())
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .build();
            
            // Guardar en Firestore
            DocumentReference docRef = firestore.collection(POSTS_COLLECTION).document(postId);
            docRef.set(post).get();
            
            log.info("Social post created successfully with ID: {}", postId);
            return convertToResponse(post, currentUser.getUserId());
            
        } catch (Exception e) {
            log.error("Error creating social post: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la publicación: " + e.getMessage());
        }
    }
    
    /**
     * Obtener feed principal
     */
    public Page<SocialPostResponse> getFeedPosts(String currentUserId, int page, int size) {
        log.info("Getting feed posts for user: {}, page: {}, size: {}", currentUserId, page, size);
        
        try {
            CollectionReference postsRef = firestore.collection(POSTS_COLLECTION);
            
            // Simplificar query para evitar índice compuesto por ahora
            Query query = postsRef
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(size * 2); // Obtener más documentos para filtrar después
            
            QuerySnapshot querySnapshot = query.get().get();
            
            List<SocialPostResponse> posts = querySnapshot.getDocuments().stream()
                    .map(doc -> {
                        SocialPostFirestore post = doc.toObject(SocialPostFirestore.class);
                        return convertToResponse(post, currentUserId);
                    })
                    .filter(post -> "PUBLIC".equals(post.getPrivacy())) // Filtrar públicos en memoria
                    .skip(page * size) // Paginación en memoria
                    .limit(size) // Limitar resultado
                    .collect(Collectors.toList());
            
            // Para desarrollo, usar un total estimado
            long total = Math.max(posts.size(), 20);
            
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(posts, pageable, total);
            
        } catch (Exception e) {
            log.error("Error getting feed posts: {}", e.getMessage(), e);
            return Page.empty();
        }
    }
    
    /**
     * Buscar posts por contenido
     */
    public Page<SocialPostResponse> searchPosts(String query, String currentUserId, int page, int size) {
        log.info("Searching posts with query: {} for user: {}", query, currentUserId);
        
        try {
            // Firestore no soporta búsqueda de texto completo nativamente,
            // pero podemos buscar por campos específicos
            CollectionReference postsRef = firestore.collection(POSTS_COLLECTION);
            
            // Buscar posts que contengan la query en la descripción o ubicación
            Query firestoreQuery = postsRef
                    .whereEqualTo("privacy", "PUBLIC")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(size)
                    .offset(page * size);
            
            QuerySnapshot querySnapshot = firestoreQuery.get().get();
            
            List<SocialPostResponse> posts = querySnapshot.getDocuments().stream()
                    .map(doc -> doc.toObject(SocialPostFirestore.class))
                    .filter(post -> containsQuery(post, query.toLowerCase()))
                    .map(post -> convertToResponse(post, currentUserId))
                    .collect(Collectors.toList());
            
            Pageable pageable = PageRequest.of(page, size);
            return new PageImpl<>(posts, pageable, posts.size());
            
        } catch (Exception e) {
            log.error("Error searching posts: {}", e.getMessage(), e);
            return Page.empty();
        }
    }
    
    /**
     * Dar/quitar like a un post
     */
    public boolean toggleLike(String postId, User currentUser) {
        log.info("Toggling like for post: {} by user: {}", postId, currentUser.getUserId());
        
        try {
            String likeId = postId + "_" + currentUser.getUserId();
            DocumentReference likeRef = firestore.collection(LIKES_COLLECTION).document(likeId);
            
            DocumentSnapshot likeDoc = likeRef.get().get();
            
            if (likeDoc.exists()) {
                // Quitar like
                likeRef.delete().get();
                updatePostCount(postId, "likesCount", -1);
                log.info("Like removed from post: {}", postId);
                return false;
            } else {
                // Dar like
                Map<String, Object> likeData = Map.of(
                        "postId", postId,
                        "userId", currentUser.getUserId(),
                        "userName", currentUser.getName(),
                        "userProfileImageUrl", Optional.ofNullable(currentUser.getProfileImageUrl()).orElse(""),
                        "createdAt", Timestamp.now()
                );
                
                likeRef.set(likeData).get();
                updatePostCount(postId, "likesCount", 1);
                log.info("Like added to post: {}", postId);
                return true;
            }
            
        } catch (Exception e) {
            log.error("Error toggling like: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el like: " + e.getMessage());
        }
    }
    
    /**
     * Guardar/desguardar un post
     */
    public boolean toggleSave(String postId, String currentUserId) {
        log.info("Toggling save for post: {} by user: {}", postId, currentUserId);
        
        try {
            String saveId = postId + "_" + currentUserId;
            DocumentReference saveRef = firestore.collection(SAVES_COLLECTION).document(saveId);
            
            DocumentSnapshot saveDoc = saveRef.get().get();
            
            if (saveDoc.exists()) {
                // Quitar de guardados
                saveRef.delete().get();
                updatePostCount(postId, "savesCount", -1);
                log.info("Post removed from saved: {}", postId);
                return false;
            } else {
                // Guardar post
                Map<String, Object> saveData = Map.of(
                        "postId", postId,
                        "userId", currentUserId,
                        "createdAt", Timestamp.now()
                );
                
                saveRef.set(saveData).get();
                updatePostCount(postId, "savesCount", 1);
                log.info("Post saved: {}", postId);
                return true;
            }
            
        } catch (Exception e) {
            log.error("Error toggling save: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar guardado: " + e.getMessage());
        }
    }
    
    // Métodos auxiliares
    
    private SocialPostResponse convertToResponse(SocialPostFirestore post, String currentUserId) {
        List<SocialPostResponse.PostImageDto> imageDtos = new ArrayList<>();
        
        if (post.getImages() != null) {
            imageDtos = post.getImages().stream()
                    .map(img -> SocialPostResponse.PostImageDto.builder()
                            .id((String) img.get("id"))
                            .imageUrl((String) img.get("imageUrl"))
                            .displayOrder(((Number) img.get("displayOrder")).intValue())
                            .originalFileName((String) img.get("originalFileName"))
                            .build())
                    .collect(Collectors.toList());
        }
        
        boolean isLiked = currentUserId != null && checkUserInteraction(LIKES_COLLECTION, post.getId(), currentUserId);
        boolean isSaved = currentUserId != null && checkUserInteraction(SAVES_COLLECTION, post.getId(), currentUserId);
        
        return SocialPostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userName(post.getUserName())
                .userRole(post.getUserRole())
                .userProfileImageUrl(post.getUserProfileImageUrl())
                .description(post.getDescription())
                .images(imageDtos)
                .location(post.getLocation())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .department(post.getDepartment())
                .tags(post.getTags())
                .mentionedCulturalObjects(post.getMentionedCulturalObjects())
                .privacy(post.getPrivacy())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .savesCount(post.getSavesCount())
                .sharesCount(post.getSharesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isLikedByCurrentUser(isLiked)
                .isSavedByCurrentUser(isSaved)
                .isOwnPost(currentUserId != null && currentUserId.equals(post.getUserId()))
                .build();
    }
    
    private boolean containsQuery(SocialPostFirestore post, String query) {
        if (post.getDescription() != null && post.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        if (post.getLocation() != null && post.getLocation().toLowerCase().contains(query)) {
            return true;
        }
        if (post.getDepartment() != null && post.getDepartment().toLowerCase().contains(query)) {
            return true;
        }
        if (post.getTags() != null && post.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query))) {
            return true;
        }
        return false;
    }
    
    private boolean checkUserInteraction(String collection, String postId, String userId) {
        try {
            String docId = postId + "_" + userId;
            DocumentSnapshot doc = firestore.collection(collection).document(docId).get().get();
            return doc.exists();
        } catch (Exception e) {
            log.error("Error checking user interaction: {}", e.getMessage());
            return false;
        }
    }
    
    private void updatePostCount(String postId, String field, int delta) {
        try {
            DocumentReference postRef = firestore.collection(POSTS_COLLECTION).document(postId);
            postRef.update(field, FieldValue.increment(delta)).get();
        } catch (Exception e) {
            log.error("Error updating post count: {}", e.getMessage());
        }
    }
    
    private long getPostsCount(String field, String value) {
        try {
            QuerySnapshot snapshot = firestore.collection(POSTS_COLLECTION)
                    .whereEqualTo(field, value)
                    .get()
                    .get();
            return snapshot.size();
        } catch (Exception e) {
            log.error("Error getting posts count: {}", e.getMessage());
            return 0;
        }
    }
}
