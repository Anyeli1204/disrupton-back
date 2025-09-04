package com.disrupton.store.controller;

import com.disrupton.store.dto.ProductDto;
import com.disrupton.store.model.Product;
import com.disrupton.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
