package com.disrupton.quota.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuota {

    @DocumentId
    private String id; // userId_yearMonth (ej: user123_2025-01)

    private String userId;
    private String userRole;
    private int year;
    private int month;

    // Cuotas para ARTISAN
    private int productsUploaded; // Productos subidos este mes
    private int additionalProductsPaid; // Productos adicionales pagados
    private int maxProductsAllowed; // Límite total de productos permitidos

    // Cuotas para GUIDE
    private int servicesUploaded; // Servicios subidos este mes
    private int additionalServicesPaid; // Servicios adicionales pagados
    private int maxServicesAllowed; // Límite total de servicios permitidos

    // Featured placement tracking
    private boolean hasFeaturedPlacement; // Si tiene destacado activo
    private Timestamp featuredExpiresAt; // Cuándo expira el destacado
    private int featuredPlacementsPaid; // Número de destacados pagados este mes

    // Metadatos
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resetAt; // Última vez que se resetearon las cuotas

    // Métodos de conveniencia

    public static String generateId(String userId, int year, int month) {
        return userId + "_" + year + "-" + String.format("%02d", month);
    }

    public static String generateId(String userId, YearMonth yearMonth) {
        return generateId(userId, yearMonth.getYear(), yearMonth.getMonthValue());
    }

    public boolean canUploadProduct() {
        if (!"ARTISAN".equals(userRole)) return false;
        return (productsUploaded + additionalProductsPaid) < maxProductsAllowed;
    }

    public boolean canUploadService() {
        if (!"GUIDE".equals(userRole)) return false;
        return (servicesUploaded + additionalServicesPaid) < maxServicesAllowed;
    }

    public int getRemainingProducts() {
        if (!"ARTISAN".equals(userRole)) return 0;
        return Math.max(0, maxProductsAllowed - productsUploaded);
    }

    public int getRemainingServices() {
        if (!"GUIDE".equals(userRole)) return 0;
        return Math.max(0, maxServicesAllowed - servicesUploaded);
    }

    public boolean isFeaturedActive() {
        if (featuredExpiresAt == null) return false;
        return Timestamp.now().compareTo(featuredExpiresAt) < 0;
    }

    public void incrementProductsUploaded() {
        this.productsUploaded++;
        this.updatedAt = Timestamp.now();
    }

    public void incrementServicesUploaded() {
        this.servicesUploaded++;
        this.updatedAt = Timestamp.now();
    }

    public void addAdditionalProduct() {
        this.additionalProductsPaid++;
        this.maxProductsAllowed++;
        this.updatedAt = Timestamp.now();
    }

    public void addAdditionalService() {
        this.additionalServicesPaid++;
        this.maxServicesAllowed++;
        this.updatedAt = Timestamp.now();
    }

    public void activateFeaturedPlacement(int durationDays) {
        this.hasFeaturedPlacement = true;
        this.featuredPlacementsPaid++;

        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + (durationDays * 24L * 60L * 60L * 1000L);
        this.featuredExpiresAt = Timestamp.of(new java.util.Date(expirationTime));
        this.updatedAt = Timestamp.now();
    }

    public static UserQuota createDefault(String userId, String userRole, int year, int month) {
        UserQuota quota = new UserQuota();
        quota.setId(generateId(userId, year, month));
        quota.setUserId(userId);
        quota.setUserRole(userRole);
        quota.setYear(year);
        quota.setMonth(month);

        // Establecer límites por defecto según el rol
        if ("ARTISAN".equals(userRole)) {
            quota.setMaxProductsAllowed(5); // 5 productos gratis por mes
            quota.setProductsUploaded(0);
            quota.setAdditionalProductsPaid(0);
        } else if ("GUIDE".equals(userRole)) {
            quota.setMaxServicesAllowed(5); // 5 servicios gratis por mes
            quota.setServicesUploaded(0);
            quota.setAdditionalServicesPaid(0);
        }

        quota.setHasFeaturedPlacement(false);
        quota.setFeaturedPlacementsPaid(0);
        quota.setCreatedAt(Timestamp.now());
        quota.setUpdatedAt(Timestamp.now());
        quota.setResetAt(Timestamp.now());

        return quota;
    }
}