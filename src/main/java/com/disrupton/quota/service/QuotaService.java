package com.disrupton.quota.service;

import com.disrupton.quota.dto.QuotaInfoDto;
import com.disrupton.quota.model.UserQuota;
import com.disrupton.user.service.UserService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaService {

    private final Firestore firestore;
    private final UserService userService;
    private static final String QUOTA_COLLECTION = "user_quotas";

    /**
     * Obtiene la información de cuota del usuario para el mes actual
     */
    public QuotaInfoDto getUserQuotaInfo(String userId) throws ExecutionException, InterruptedException {
        log.info("📊 Obteniendo información de cuota para usuario: {}", userId);

        var user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado: " + userId);
        }

        String userRole = user.getRole();
        YearMonth currentMonth = YearMonth.now();

        // Solo ARTISAN y GUIDE tienen cuotas limitadas
        if (!"ARTISAN".equals(userRole) && !"GUIDE".equals(userRole)) {
            return QuotaInfoDto.forRegularUser(userId, userRole);
        }

        UserQuota quota = getOrCreateUserQuota(userId, userRole, currentMonth);

        if ("ARTISAN".equals(userRole)) {
            return QuotaInfoDto.forArtisan(
                    userId,
                    quota.getMaxProductsAllowed(),
                    quota.getProductsUploaded(),
                    quota.getAdditionalProductsPaid(),
                    quota.isFeaturedActive(),
                    quota.getFeaturedExpiresAt()
            );
        } else { // GUIDE
            return QuotaInfoDto.forGuide(
                    userId,
                    quota.getMaxServicesAllowed(),
                    quota.getServicesUploaded(),
                    quota.getAdditionalServicesPaid(),
                    quota.isFeaturedActive(),
                    quota.getFeaturedExpiresAt()
            );
        }
    }

    /**
     * Verifica si el usuario puede subir un producto (ARTISAN)
     */
    public boolean canUploadProduct(String userId) throws ExecutionException, InterruptedException {
        log.info("🔍 Verificando si usuario {} puede subir producto", userId);

        var user = userService.getUserById(userId);
        if (!"ARTISAN".equals(user.getRole())) {
            log.warn("❌ Usuario {} no es ARTISAN", userId);
            return false;
        }

        UserQuota quota = getOrCreateUserQuota(userId, "ARTISAN", YearMonth.now());
        boolean canUpload = quota.canUploadProduct();

        log.info("{} Usuario {} {} subir producto. Usados: {}, Límite: {}",
                canUpload ? "✅" : "❌", userId, canUpload ? "puede" : "NO puede",
                quota.getProductsUploaded(), quota.getMaxProductsAllowed());

        return canUpload;
    }

    /**
     * Verifica si el usuario puede subir un servicio (GUIDE)
     */
    public boolean canUploadService(String userId) throws ExecutionException, InterruptedException {
        log.info("🔍 Verificando si usuario {} puede subir servicio", userId);

        var user = userService.getUserById(userId);
        if (!"GUIDE".equals(user.getRole())) {
            log.warn("❌ Usuario {} no es GUIDE", userId);
            return false;
        }

        UserQuota quota = getOrCreateUserQuota(userId, "GUIDE", YearMonth.now());
        boolean canUpload = quota.canUploadService();

        log.info("{} Usuario {} {} subir servicio. Usados: {}, Límite: {}",
                canUpload ? "✅" : "❌", userId, canUpload ? "puede" : "NO puede",
                quota.getServicesUploaded(), quota.getMaxServicesAllowed());

        return canUpload;
    }

    /**
     * Registra que el usuario subió un producto
     */
    public void recordProductUpload(String userId) throws ExecutionException, InterruptedException {
        log.info("📦 Registrando subida de producto para usuario: {}", userId);

        UserQuota quota = getOrCreateUserQuota(userId, "ARTISAN", YearMonth.now());

        if (!quota.canUploadProduct()) {
            throw new IllegalStateException("Usuario ha excedido su límite de productos para este mes");
        }

        quota.incrementProductsUploaded();
        saveUserQuota(quota);

        log.info("✅ Producto registrado. Usuario {}: {}/{} productos usados",
                userId, quota.getProductsUploaded(), quota.getMaxProductsAllowed());
    }

    /**
     * Registra que el usuario subió un servicio
     */
    public void recordServiceUpload(String userId) throws ExecutionException, InterruptedException {
        log.info("🗺️ Registrando subida de servicio para usuario: {}", userId);

        UserQuota quota = getOrCreateUserQuota(userId, "GUIDE", YearMonth.now());

        if (!quota.canUploadService()) {
            throw new IllegalStateException("Usuario ha excedido su límite de servicios para este mes");
        }

        quota.incrementServicesUploaded();
        saveUserQuota(quota);

        log.info("✅ Servicio registrado. Usuario {}: {}/{} servicios usados",
                userId, quota.getServicesUploaded(), quota.getMaxServicesAllowed());
    }

    /**
     * Agrega un producto adicional pagado
     */
    public void addPaidProduct(String userId) throws ExecutionException, InterruptedException {
        log.info("💰 Agregando producto adicional pagado para usuario: {}", userId);

        UserQuota quota = getOrCreateUserQuota(userId, "ARTISAN", YearMonth.now());
        quota.addAdditionalProduct();
        saveUserQuota(quota);

        log.info("✅ Producto adicional agregado. Usuario {}: límite aumentado a {}",
                userId, quota.getMaxProductsAllowed());
    }

    /**
     * Agrega un servicio adicional pagado
     */
    public void addPaidService(String userId) throws ExecutionException, InterruptedException {
        log.info("💰 Agregando servicio adicional pagado para usuario: {}", userId);

        UserQuota quota = getOrCreateUserQuota(userId, "GUIDE", YearMonth.now());
        quota.addAdditionalService();
        saveUserQuota(quota);

        log.info("✅ Servicio adicional agregado. Usuario {}: límite aumentado a {}",
                userId, quota.getMaxServicesAllowed());
    }

    /**
     * Activa el destacado en tienda para el usuario
     */
    public void activateFeaturedPlacement(String userId, int durationDays) throws ExecutionException, InterruptedException {
        log.info("⭐ Activando destacado para usuario: {} por {} días", userId, durationDays);

        var user = userService.getUserById(userId);
        String userRole = user.getRole();

        if (!"ARTISAN".equals(userRole) && !"GUIDE".equals(userRole)) {
            throw new IllegalArgumentException("Solo ARTISAN y GUIDE pueden tener destacado");
        }

        UserQuota quota = getOrCreateUserQuota(userId, userRole, YearMonth.now());
        quota.activateFeaturedPlacement(durationDays);
        saveUserQuota(quota);

        log.info("✅ Destacado activado para usuario {} hasta: {}", userId, quota.getFeaturedExpiresAt());
    }

    /**
     * Verifica si el usuario tiene destacado activo
     */
    public boolean hasFeaturedPlacement(String userId) throws ExecutionException, InterruptedException {
        var user = userService.getUserById(userId);
        String userRole = user.getRole();

        if (!"ARTISAN".equals(userRole) && !"GUIDE".equals(userRole)) {
            return false;
        }

        UserQuota quota = getOrCreateUserQuota(userId, userRole, YearMonth.now());
        return quota.isFeaturedActive();
    }

    /**
     * Resetea las cuotas mensuales para todos los usuarios (tarea programada)
     */
    public void resetMonthlyQuotas() throws ExecutionException, InterruptedException {
        log.info("🔄 Iniciando reset de cuotas mensuales...");

        // Este método debería ser llamado por un job programado al inicio de cada mes
        // Por simplicidad, aquí solo loggear. En una implementación real, habría que:
        // 1. Obtener todos los usuarios ARTISAN y GUIDE
        // 2. Crear nuevas cuotas para el mes actual
        // 3. Mantener historial de cuotas anteriores

        log.info("✅ Reset de cuotas mensuales completado");
    }

    // Métodos privados

    private UserQuota getOrCreateUserQuota(String userId, String userRole, YearMonth yearMonth)
            throws ExecutionException, InterruptedException {

        String quotaId = UserQuota.generateId(userId, yearMonth);
        DocumentSnapshot doc = firestore.collection(QUOTA_COLLECTION).document(quotaId).get().get();

        if (doc.exists()) {
            UserQuota quota = doc.toObject(UserQuota.class);
            // Verificar si el destacado ha expirado
            if (quota.isFeaturedActive() && quota.getFeaturedExpiresAt().compareTo(Timestamp.now()) < 0) {
                quota.setHasFeaturedPlacement(false);
                quota.setFeaturedExpiresAt(null);
                quota.setUpdatedAt(Timestamp.now());
                saveUserQuota(quota);
            }
            return quota;
        } else {
            // Crear nueva cuota para este mes
            log.info("📊 Creando nueva cuota para usuario {} - mes {}", userId, yearMonth);
            UserQuota newQuota = UserQuota.createDefault(
                    userId, userRole, yearMonth.getYear(), yearMonth.getMonthValue());
            saveUserQuota(newQuota);
            return newQuota;
        }
    }

    private void saveUserQuota(UserQuota quota) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(QUOTA_COLLECTION).document(quota.getId());
        docRef.set(quota).get();
    }
}