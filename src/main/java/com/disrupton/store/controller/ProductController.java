package com.disrupton.store.controller;

import com.disrupton.auth.annotation.RequireRole;
import com.disrupton.store.dto.ProductDto;
import com.disrupton.store.model.Product;
import com.disrupton.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para productos artesanales
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tienda/productos")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    /**
     * Obtener todos los productos
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        try {
            log.info("🛍️ Obteniendo todos los productos artesanales");
            
            List<ProductDto> products = productService.getAllProducts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener productos: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener productos artesanales");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener productos por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String categoria) {
        try {
            log.info("🎨 Obteniendo productos de categoría: {}", categoria);
            
            Product.ProductCategory categoryEnum = Product.ProductCategory.valueOf(categoria.toUpperCase());
            List<ProductDto> products = productService.getProductsByCategory(categoryEnum);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            response.put("categoria", categoria);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Categoría no válida: {}", categoria);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Categoría no válida: " + categoria);
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener productos por categoría: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener productos por categoría");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Buscar productos
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String departamento) {
        try {
            log.info("🔍 Buscando productos con término: '{}', precio: {}-{}, departamento: '{}'", 
                    termino, precioMin, precioMax, departamento);
            
            List<ProductDto> products = productService.searchProducts(termino, precioMin, precioMax, departamento);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            response.put("filtros", Map.of(
                "termino", termino != null ? termino : "",
                "precioMin", precioMin,
                "precioMax", precioMax,
                "departamento", departamento != null ? departamento : ""
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en búsqueda de productos: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error en la búsqueda de productos");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable String id) {
        try {
            log.info("📦 Obteniendo producto por ID: {}", id);
            
            ProductDto product = productService.getProductById(id);
            
            // Incrementar contador de visualizaciones
            productService.incrementViewCount(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", product);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.warn("⚠️ Producto no encontrado: {}", id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Producto no encontrado");
            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("❌ Error al obtener producto: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener producto");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener productos por artesano
     */
    @GetMapping("/artesano/{artesanoId}")
    public ResponseEntity<Map<String, Object>> getProductsByArtisan(@PathVariable String artesanoId) {
        try {
            log.info("👨‍🎨 Obteniendo productos del artesano: {}", artesanoId);
            
            List<ProductDto> products = productService.getProductsByArtisan(artesanoId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            response.put("artesanoId", artesanoId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener productos del artesano: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener productos del artesano");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener estadísticas de productos
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        try {
            log.info("📊 Obteniendo estadísticas de productos");
            
            ProductService.ProductStatsDto stats = productService.getProductStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener estadísticas: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener estadísticas de productos");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener productos destacados
     */
    @GetMapping("/destacados")
    public ResponseEntity<Map<String, Object>> getFeaturedProducts() {
        try {
            log.info("⭐ Obteniendo productos destacados");

            List<ProductDto> products = productService.getFeaturedProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al obtener productos destacados: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener productos destacados");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Crear un nuevo producto (solo ARTISAN)
     */
    @PostMapping("/crear")
    @RequireRole({"ARTISAN"})
    public ResponseEntity<Map<String, Object>> createProduct(
            @Valid @RequestBody ProductDto productRequest,
            Authentication authentication) {
        try {
            String artisanId = authentication.getName();
            log.info("📦 Creando producto para artesano: {}", artisanId);

            ProductDto createdProduct = productService.createProduct(artisanId, productRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdProduct);
            response.put("message", "Producto creado exitosamente");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            log.warn("⚠️ Límite excedido: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", "QUOTA_EXCEEDED");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Datos inválidos: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error creando producto: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error interno al crear producto");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Actualizar un producto existente (solo el artesano dueño)
     */
    @PutMapping("/{productId}")
    @RequireRole({"ARTISAN"})
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody ProductDto productRequest,
            Authentication authentication) {
        try {
            String artisanId = authentication.getName();
            log.info("🔄 Actualizando producto {} por artesano: {}", productId, artisanId);

            ProductDto updatedProduct = productService.updateProduct(productId, artisanId, productRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedProduct);
            response.put("message", "Producto actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de autorización/datos: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error actualizando producto: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error interno al actualizar producto");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Eliminar un producto (solo el artesano dueño)
     */
    @DeleteMapping("/{productId}")
    @RequireRole({"ARTISAN"})
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable String productId,
            Authentication authentication) {
        try {
            String artisanId = authentication.getName();
            log.info("🗑️ Eliminando producto {} por artesano: {}", productId, artisanId);

            boolean deleted = productService.deleteProduct(productId, artisanId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Producto eliminado exitosamente" : "No se pudo eliminar el producto");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de autorización: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("❌ Error eliminando producto: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error interno al eliminar producto");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener mis productos (solo para artesanos)
     */
    @GetMapping("/mis-productos")
    @RequireRole({"ARTISAN"})
    public ResponseEntity<Map<String, Object>> getMyProducts(Authentication authentication) {
        try {
            String artisanId = authentication.getName();
            log.info("👨‍🎨 Obteniendo productos del artesano: {}", artisanId);

            List<ProductDto> products = productService.getProductsByArtisan(artisanId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error obteniendo mis productos: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener tus productos");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener categorías disponibles
     */
    @GetMapping("/categorias")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            log.info("📂 Obteniendo categorías de productos");

            Map<String, String> categories = new HashMap<>();
            for (Product.ProductCategory category : Product.ProductCategory.values()) {
                categories.put(category.name(), category.getDisplayName());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            response.put("count", categories.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error al obtener categorías: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Error al obtener categorías");

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
