package com.disrupton.store.service;

import com.disrupton.quota.service.QuotaService;
import com.disrupton.store.dto.ProductDto;
import com.disrupton.store.model.Product;
import com.disrupton.user.service.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de productos artesanales
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final Firestore db;
    private final QuotaService quotaService;
    private final UserService userService;
    private static final String COLLECTION_NAME = "products";

    /**
     * Obtener todos los productos disponibles
     */
    public List<ProductDto> getAllProducts() throws ExecutionException, InterruptedException {
        log.info("🛍️ Obteniendo todos los productos artesanales");
        
        CollectionReference products = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = products.get();
        QuerySnapshot querySnapshot = query.get();
        
        List<ProductDto> productDtos = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                product.setId(document.getId());
                productDtos.add(convertToDto(product));
            }
        }
        
        log.info("✅ Productos encontrados: {}", productDtos.size());
        return productDtos;
    }

    /**
     * Obtener productos por categoría
     */
    public List<ProductDto> getProductsByCategory(Product.ProductCategory category) 
            throws ExecutionException, InterruptedException {
        log.info("🎨 Obteniendo productos de categoría: {}", category);
        
        CollectionReference products = db.collection(COLLECTION_NAME);
        Query query = products.whereEqualTo("category", category.name());
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        
        List<ProductDto> productDtos = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                product.setId(document.getId());
                productDtos.add(convertToDto(product));
            }
        }
        
        return productDtos;
    }

    /**
     * Buscar productos por término
     */
    public List<ProductDto> searchProducts(String searchTerm, Double minPrice, Double maxPrice, 
                                         String department) throws ExecutionException, InterruptedException {
        log.info("🔍 Buscando productos con término: '{}', precio: {}-{}, departamento: '{}'", 
                searchTerm, minPrice, maxPrice, department);
        
        CollectionReference products = db.collection(COLLECTION_NAME);
        Query query = products;
        
        // Filtro por departamento
        if (department != null && !department.trim().isEmpty()) {
            query = query.whereEqualTo("department", department);
        }
        
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<ProductDto> allProducts = new ArrayList<>();
        
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                product.setId(document.getId());
                allProducts.add(convertToDto(product));
            }
        }
        
        // Filtros adicionales en memoria (debido a limitaciones de Firestore)
        return allProducts.stream()
                .filter(product -> {
                    // Filtro por término de búsqueda
                    if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                        String term = searchTerm.toLowerCase();
                        return product.getName().toLowerCase().contains(term) ||
                               product.getDescription().toLowerCase().contains(term) ||
                               product.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(term)) ||
                               product.getMaterials().stream().anyMatch(material -> material.toLowerCase().contains(term));
                    }
                    return true;
                })
                .filter(product -> {
                    // Filtro por precio mínimo
                    if (minPrice != null) {
                        return product.getPrice() >= minPrice;
                    }
                    return true;
                })
                .filter(product -> {
                    // Filtro por precio máximo
                    if (maxPrice != null) {
                        return product.getPrice() <= maxPrice;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtener producto por ID
     */
    public ProductDto getProductById(String productId) throws ExecutionException, InterruptedException {
        log.info("📦 Obteniendo producto por ID: {}", productId);
        
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(productId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        
        if (document.exists()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                product.setId(document.getId());
                return convertToDto(product);
            }
        }
        
        throw new RuntimeException("Producto no encontrado: " + productId);
    }

    /**
     * Obtener productos por artesano
     */
    public List<ProductDto> getProductsByArtisan(String artisanId) 
            throws ExecutionException, InterruptedException {
        log.info("👨‍🎨 Obteniendo productos del artesano: {}", artisanId);
        
        CollectionReference products = db.collection(COLLECTION_NAME);
        Query query = products.whereEqualTo("artisanId", artisanId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        
        List<ProductDto> productDtos = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                product.setId(document.getId());
                productDtos.add(convertToDto(product));
            }
        }
        
        return productDtos;
    }

    /**
     * Crear un nuevo producto (con validación de cuotas)
     */
    public ProductDto createProduct(String artisanId, ProductDto productRequest) throws ExecutionException, InterruptedException {
        log.info("📦 Creando nuevo producto para artesano: {}", artisanId);

        // Validar que el usuario es artesano
        var user = userService.getUserById(artisanId);
        if (user == null || !"ARTISAN".equals(user.getRole())) {
            throw new IllegalArgumentException("Solo los artesanos pueden crear productos");
        }

        // Verificar cuota mensual
        if (!quotaService.canUploadProduct(artisanId)) {
            throw new IllegalStateException("Has excedido tu límite mensual de productos. Puedes comprar productos adicionales.");
        }

        // Crear el producto
        Product product = new Product();
        String productId = UUID.randomUUID().toString();

        // Información básica
        product.setId(productId);
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setShortDescription(productRequest.getShortDescription());

        // Precio
        product.setPrice(productRequest.getPrice());
        product.setCurrency(productRequest.getCurrency() != null ? productRequest.getCurrency() : "PEN");
        product.setFormattedPrice(productRequest.getFormattedPrice());

        // Imágenes
        product.setMainImageUrl(productRequest.getMainImageUrl());
        product.setAdditionalImages(productRequest.getAdditionalImages());

        // Ubicación
        product.setOrigin(productRequest.getOrigin());
        product.setDepartment(productRequest.getDepartment());
        product.setProvince(productRequest.getProvince());
        product.setDistrict(productRequest.getDistrict());
        product.setLatitude(productRequest.getLatitude());
        product.setLongitude(productRequest.getLongitude());

        // Categorización
        if (productRequest.getCategory() != null) {
            for (Product.ProductCategory category : Product.ProductCategory.values()) {
                if (category.getDisplayName().equals(productRequest.getCategory())) {
                    product.setCategory(category);
                    break;
                }
            }
        }

        if (productRequest.getType() != null) {
            for (Product.ProductType type : Product.ProductType.values()) {
                if (type.getDisplayName().equals(productRequest.getType())) {
                    product.setType(type);
                    break;
                }
            }
        }

        product.setTags(productRequest.getTags());
        product.setMaterials(productRequest.getMaterials());

        // Artesano
        product.setArtisanId(artisanId);
        product.setArtisanName(user.getName());
        product.setArtisanContact(user.getEmail());

        // Estado
        product.setIsAvailable(true);
        product.setStockQuantity(productRequest.getStockQuantity() != null ? productRequest.getStockQuantity() : 1);
        product.setIsHandmade(productRequest.getIsHandmade() != null ? productRequest.getIsHandmade() : true);
        product.setCraftingTime(productRequest.getCraftingTime());

        // Métricas iniciales
        product.setRating(0.0);
        product.setTotalRatings(0);
        product.setViewCount(0);
        product.setPurchaseCount(0);

        // Metadatos
        product.setCreatedAt(LocalDateTime.now().toString());
        product.setUpdatedAt(LocalDateTime.now().toString());
        product.setCreatedBy(artisanId);

        // Guardar en Firestore
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).set(product);
        WriteResult result = future.get();

        // Registrar en el sistema de cuotas
        quotaService.recordProductUpload(artisanId);

        log.info("✅ Producto creado exitosamente: {} para artesano: {}. Timestamp: {}",
                productId, artisanId, result.getUpdateTime());

        return convertToDto(product);
    }

    /**
     * Actualizar un producto existente
     */
    public ProductDto updateProduct(String productId, String artisanId, ProductDto productRequest)
            throws ExecutionException, InterruptedException {
        log.info("🔄 Actualizando producto: {} por artesano: {}", productId, artisanId);

        // Verificar que el producto existe y pertenece al artesano
        DocumentSnapshot doc = db.collection(COLLECTION_NAME).document(productId).get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Producto no encontrado: " + productId);
        }

        Product existingProduct = doc.toObject(Product.class);
        if (!artisanId.equals(existingProduct.getArtisanId())) {
            throw new IllegalArgumentException("No tienes permiso para actualizar este producto");
        }

        // Actualizar campos modificables
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setShortDescription(productRequest.getShortDescription());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setFormattedPrice(productRequest.getFormattedPrice());
        existingProduct.setMainImageUrl(productRequest.getMainImageUrl());
        existingProduct.setAdditionalImages(productRequest.getAdditionalImages());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        existingProduct.setIsAvailable(productRequest.getIsAvailable());
        existingProduct.setCraftingTime(productRequest.getCraftingTime());
        existingProduct.setTags(productRequest.getTags());
        existingProduct.setMaterials(productRequest.getMaterials());
        existingProduct.setUpdatedAt(LocalDateTime.now().toString());

        // Guardar cambios
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).set(existingProduct);
        WriteResult result = future.get();

        log.info("✅ Producto actualizado: {}. Timestamp: {}", productId, result.getUpdateTime());

        return convertToDto(existingProduct);
    }

    /**
     * Eliminar un producto
     */
    public boolean deleteProduct(String productId, String artisanId) throws ExecutionException, InterruptedException {
        log.info("🗑️ Eliminando producto: {} por artesano: {}", productId, artisanId);

        // Verificar que el producto existe y pertenece al artesano
        DocumentSnapshot doc = db.collection(COLLECTION_NAME).document(productId).get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Producto no encontrado: " + productId);
        }

        Product product = doc.toObject(Product.class);
        if (!artisanId.equals(product.getArtisanId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este producto");
        }

        // Eliminar producto
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(productId).delete();
        WriteResult result = future.get();

        log.info("✅ Producto eliminado: {}. Timestamp: {}", productId, result.getUpdateTime());
        return true;
    }

    /**
     * Obtener productos destacados (featured)
     */
    public List<ProductDto> getFeaturedProducts() throws ExecutionException, InterruptedException {
        log.info("⭐ Obteniendo productos destacados");

        List<ProductDto> allProducts = getAllProducts();

        // Filtrar productos de artesanos con featured activo
        List<ProductDto> featuredProducts = new ArrayList<>();

        for (ProductDto product : allProducts) {
            try {
                if (quotaService.hasFeaturedPlacement(product.getArtisanId())) {
                    featuredProducts.add(product);
                }
            } catch (Exception e) {
                log.warn("⚠️ Error verificando featured para artesano {}: {}",
                        product.getArtisanId(), e.getMessage());
            }
        }

        // Ordenar por rating y fecha de actualización
        featuredProducts.sort((p1, p2) -> {
            int ratingComparison = Double.compare(p2.getRating(), p1.getRating());
            if (ratingComparison != 0) return ratingComparison;
            return p2.getUpdatedAt().compareTo(p1.getUpdatedAt());
        });

        log.info("✅ Productos destacados encontrados: {}", featuredProducts.size());
        return featuredProducts;
    }

    /**
     * Incrementar contador de visualizaciones
     */
    public void incrementViewCount(String productId) {
        try {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(productId);
            docRef.update("viewCount", FieldValue.increment(1));
            log.info("👁️ Incrementado view count para producto: {}", productId);
        } catch (Exception e) {
            log.error("❌ Error al incrementar view count: {}", e.getMessage());
        }
    }

    /**
     * Obtener estadísticas de productos
     */
    public ProductStatsDto getProductStats() throws ExecutionException, InterruptedException {
        List<ProductDto> allProducts = getAllProducts();
        
        long totalProducts = allProducts.size();
        long availableProducts = allProducts.stream()
                .filter(ProductDto::getIsAvailable)
                .count();
        
        double avgPrice = allProducts.stream()
                .filter(ProductDto::getIsAvailable)
                .mapToDouble(ProductDto::getPrice)
                .average()
                .orElse(0.0);
        
        long uniqueArtisans = allProducts.stream()
                .map(ProductDto::getArtisanId)
                .distinct()
                .count();
        
        return new ProductStatsDto(totalProducts, availableProducts, avgPrice, uniqueArtisans);
    }

    /**
     * Convertir Product a ProductDto
     */
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        
        // Información básica
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setShortDescription(product.getShortDescription());
        
        // Precio
        dto.setPrice(product.getPrice());
        dto.setCurrency(product.getCurrency());
        dto.setFormattedPrice(product.getFormattedPrice());
        
        // Imágenes
        dto.setMainImageUrl(product.getMainImageUrl());
        dto.setAdditionalImages(product.getAdditionalImages());
        
        // Ubicación
        dto.setOrigin(product.getOrigin());
        dto.setDepartment(product.getDepartment());
        dto.setProvince(product.getProvince());
        dto.setDistrict(product.getDistrict());
        dto.setLatitude(product.getLatitude());
        dto.setLongitude(product.getLongitude());
        
        // Categorización
        dto.setCategory(product.getCategory() != null ? product.getCategory().getDisplayName() : "");
        dto.setCategoryIcon(product.getCategoryIcon());
        dto.setType(product.getType() != null ? product.getType().getDisplayName() : "");
        dto.setTags(product.getTags());
        dto.setMaterials(product.getMaterials());
        
        // Artesano
        dto.setArtisanId(product.getArtisanId());
        dto.setArtisanName(product.getArtisanName());
        dto.setArtisanContact(product.getArtisanContact());
        
        // Estado
        dto.setIsAvailable(product.getIsAvailable());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setAvailabilityStatus(product.getAvailabilityStatus());
        dto.setIsHandmade(product.getIsHandmade());
        dto.setCraftingTime(product.getCraftingTime());
        
        // Métricas
        dto.setRating(product.getRating());
        dto.setTotalRatings(product.getTotalRatings());
        dto.setFormattedRating(product.getFormattedRating());
        dto.setViewCount(product.getViewCount());
        dto.setPurchaseCount(product.getPurchaseCount());
        
        // Metadatos
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        
        return dto;
    }

    /**
     * DTO para estadísticas de productos
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ProductStatsDto {
        private long totalProducts;
        private long availableProducts;
        private double averagePrice;
        private long uniqueArtisans;
    }
}
