package com.disrupton.quota.dto;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaInfoDto {

    // Información del usuario
    private String userId;
    private String userRole;
    private int currentMonth;
    private int currentYear;

    // Información de productos (ARTISAN)
    private Integer maxProductsAllowed;
    private Integer productsUploaded;
    private Integer remainingProducts;
    private Integer additionalProductsPaid;

    // Información de servicios (GUIDE)
    private Integer maxServicesAllowed;
    private Integer servicesUploaded;
    private Integer remainingServices;
    private Integer additionalServicesPaid;

    // Información de destacado
    private Boolean hasFeaturedPlacement;
    private Timestamp featuredExpiresAt;
    private Integer featuredPlacementsPaid;

    // Estado general
    private Boolean canUploadMore;
    private String message;
    private Timestamp lastUpdated;

    public static QuotaInfoDto forArtisan(String userId, int maxProducts, int uploaded,
                                        int additional, boolean featured, Timestamp featuredExpires) {
        return QuotaInfoDto.builder()
                .userId(userId)
                .userRole("ARTISAN")
                .maxProductsAllowed(maxProducts)
                .productsUploaded(uploaded)
                .remainingProducts(Math.max(0, maxProducts - uploaded))
                .additionalProductsPaid(additional)
                .hasFeaturedPlacement(featured)
                .featuredExpiresAt(featuredExpires)
                .canUploadMore(uploaded < maxProducts)
                .lastUpdated(Timestamp.now())
                .build();
    }

    public static QuotaInfoDto forGuide(String userId, int maxServices, int uploaded,
                                      int additional, boolean featured, Timestamp featuredExpires) {
        return QuotaInfoDto.builder()
                .userId(userId)
                .userRole("GUIDE")
                .maxServicesAllowed(maxServices)
                .servicesUploaded(uploaded)
                .remainingServices(Math.max(0, maxServices - uploaded))
                .additionalServicesPaid(additional)
                .hasFeaturedPlacement(featured)
                .featuredExpiresAt(featuredExpires)
                .canUploadMore(uploaded < maxServices)
                .lastUpdated(Timestamp.now())
                .build();
    }

    public static QuotaInfoDto forRegularUser(String userId, String role) {
        return QuotaInfoDto.builder()
                .userId(userId)
                .userRole(role)
                .canUploadMore(false)
                .message("Los usuarios regulares no tienen cuotas de productos/servicios")
                .lastUpdated(Timestamp.now())
                .build();
    }
}