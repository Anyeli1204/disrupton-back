package com.disrupton.store.seeder;

import com.disrupton.store.model.Product;
import com.disrupton.store.model.TourismService;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Seeder para poblar la base de datos con productos artesanales y servicios turísticos peruanos
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class StoreSeeder implements CommandLineRunner {

    private final Firestore firestore;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        try {
            log.info("🌱 Iniciando seeder de la tienda cultural peruana...");
            
            // Limpiar datos existentes primero
            log.info("🧹 Limpiando datos existentes...");
            clearExistingData();
            
            // Crear nuevos datos
            seedProducts();
            seedTourismServices();
            
            log.info("✅ Seeder completado exitosamente!");
            
        } catch (Exception e) {
            log.error("❌ Error en el seeder: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpiar datos existentes de productos y servicios
     */
    private void clearExistingData() {
        try {
            // Limpiar productos
            var productDocs = firestore.collection("products").get().get().getDocuments();
            for (var doc : productDocs) {
                doc.getReference().delete().get();
            }
            log.info("�️ Eliminados {} productos existentes", productDocs.size());
            
            // Limpiar servicios turísticos
            var serviceDocs = firestore.collection("tourismServices").get().get().getDocuments();
            for (var doc : serviceDocs) {
                doc.getReference().delete().get();
            }
            log.info("🗑️ Eliminados {} servicios existentes", serviceDocs.size());
            
        } catch (Exception e) {
            log.error("❌ Error al limpiar datos existentes: {}", e.getMessage());
        }
    }

    /**
     * Poblar productos artesanales peruanos
     */
    private void seedProducts() {
        log.info("🎨 Creando productos artesanales peruanos...");
        
        List<Product> products = Arrays.asList(
            // TEXTILES
            createProduct("Chullo Alpaca Cusqueño", "Gorro tradicional tejido a mano con lana de alpaca 100% natural, diseños incaicos auténticos", 
                Product.ProductCategory.TEXTILES, Product.ProductType.UNICO, 85.00, "Cusco", "CUSCO",
                Arrays.asList("https://example.com/chullo1.jpg", "https://example.com/chullo2.jpg"),
                "artisan_001", "María Quispe", "maria.quispe@gmail.com", "+51 984 123 456"),
                
            createProduct("Poncho Ayacuchano", "Poncho ceremonial tejido en telar tradicional con técnicas ancestrales, lana de oveja y alpaca", 
                Product.ProductCategory.TEXTILES, Product.ProductType.LIMITADO, 120.00, "Ayacucho", "AYACUCHO",
                Arrays.asList("https://example.com/poncho1.jpg", "https://example.com/poncho2.jpg"),
                "artisan_002", "Carlos Mendoza", "carlos.mendoza@gmail.com", "+51 966 789 012"),
                
            createProduct("Manta Huancavelicana", "Manta multicolor tejida a mano con patrones geométricos andinos, ideal para decoración", 
                Product.ProductCategory.TEXTILES, Product.ProductType.PERSONALIZABLE, 95.00, "Huancavelica", "HUANCAVELICA",
                Arrays.asList("https://example.com/manta1.jpg", "https://example.com/manta2.jpg"),
                "artisan_003", "Rosa Huamán", "rosa.huaman@gmail.com", "+51 987 654 321"),

            // CERÁMICA
            createProduct("Toro de Pucará", "Torito ceremonial de cerámica vidriada, símbolo de protección y buena suerte", 
                Product.ProductCategory.CERAMICA, Product.ProductType.UNICO, 45.00, "Pucará - Puno", "PUNO",
                Arrays.asList("https://example.com/toro1.jpg", "https://example.com/toro2.jpg"),
                "artisan_004", "Pedro Mamani", "pedro.mamani@gmail.com", "+51 975 123 789"),
                
            createProduct("Cántaro Shipibo", "Cántaro ceremonial con diseños kené tradicionales shipibo-konibo, cerámica natural", 
                Product.ProductCategory.CERAMICA, Product.ProductType.SERIE, 75.00, "Ucayali", "UCAYALI",
                Arrays.asList("https://example.com/cantaro1.jpg", "https://example.com/cantaro2.jpg"),
                "artisan_005", "Olinda Cumapa", "olinda.cumapa@gmail.com", "+51 961 456 123"),
                
            createProduct("Vasija Mochica", "Réplica de huaco retrato mochica, cerámica pintada a mano con técnicas precolombinas", 
                Product.ProductCategory.CERAMICA, Product.ProductType.SERIE, 110.00, "Trujillo", "LA LIBERTAD",
                Arrays.asList("https://example.com/vasija1.jpg", "https://example.com/vasija2.jpg"),
                "artisan_006", "Jorge Vásquez", "jorge.vasquez@gmail.com", "+51 944 789 456"),

            // ORFEBRERÍA
            createProduct("Aretes de Filigrana", "Aretes elaborados en plata 950 con técnica de filigrana catacaense, diseño floral", 
                Product.ProductCategory.ORFEBRERIA, Product.ProductType.UNICO, 180.00, "Catacaos - Piura", "PIURA",
                Arrays.asList("https://example.com/aretes1.jpg", "https://example.com/aretes2.jpg"),
                "artisan_007", "Carmen Torres", "carmen.torres@gmail.com", "+51 956 321 987"),
                
            createProduct("Pulsera Inca", "Pulsera en plata con símbolos incaicos, trabajo artesanal con incrustaciones de piedras andinas", 
                Product.ProductCategory.ORFEBRERIA, Product.ProductType.LIMITADO, 210.00, "Cusco", "CUSCO",
                Arrays.asList("https://example.com/pulsera1.jpg", "https://example.com/pulsera2.jpg"),
                "artisan_008", "Roberto Ccahuana", "roberto.ccahuana@gmail.com", "+51 987 159 753"),

            // TALLADO
            createProduct("Retablo Ayacuchano", "Retablo tradicional con escenas costumbristas, madera tallada y pintada a mano", 
                Product.ProductCategory.TALLADO, Product.ProductType.UNICO, 320.00, "Ayacucho", "AYACUCHO",
                Arrays.asList("https://example.com/retablo1.jpg", "https://example.com/retablo2.jpg"),
                "artisan_009", "Luis Sulca", "luis.sulca@gmail.com", "+51 963 741 852"),
                
            createProduct("Máscara Diablada", "Máscara ceremonial para danza de la diablada, madera tallada con detalles metálicos", 
                Product.ProductCategory.TALLADO, Product.ProductType.LIMITADO, 280.00, "Puno", "PUNO",
                Arrays.asList("https://example.com/mascara1.jpg", "https://example.com/mascara2.jpg"),
                "artisan_010", "Elena Choque", "elena.choque@gmail.com", "+51 978 654 132"),

            // OTROS
            createProduct("Sombrero de Huamanga", "Sombrero tradicional en cuero fino, elaborado con técnicas ancestrales ayacuchanas", 
                Product.ProductCategory.OTROS, Product.ProductType.PERSONALIZABLE, 150.00, "Ayacucho", "AYACUCHO",
                Arrays.asList("https://example.com/sombrero1.jpg", "https://example.com/sombrero2.jpg"),
                "artisan_011", "Miguel Rojas", "miguel.rojas@gmail.com", "+51 964 258 741"),
                
            createProduct("Cartuchera Cusqueña", "Bolso pequeño en cuero repujado con motivos incaicos, ideal para uso diario", 
                Product.ProductCategory.OTROS, Product.ProductType.SERIE, 85.00, "Cusco", "CUSCO",
                Arrays.asList("https://example.com/cartuchera1.jpg", "https://example.com/cartuchera2.jpg"),
                "artisan_012", "Ana Condori", "ana.condori@gmail.com", "+51 951 357 246")
        );

        // Guardar productos en Firestore
        for (Product product : products) {
            try {
                firestore.collection("products").add(product).get();
                log.info("✅ Producto creado: {}", product.getName());
            } catch (Exception e) {
                log.error("❌ Error al crear producto {}: {}", product.getName(), e.getMessage());
            }
        }
    }

    /**
     * Poblar servicios turísticos peruanos
     */
    private void seedTourismServices() {
        log.info("🗺️ Creando servicios turísticos peruanos...");
        
        List<TourismService> services = Arrays.asList(
            // TOURS CULTURALES
            createTourismService("Tour Machu Picchu Clásico", "Visita guiada completa a la ciudadela inca de Machu Picchu con explicación histórica detallada", 
                TourismService.ServiceCategory.CULTURAL, 350.00, "Machu Picchu", "CUSCO", 8,
                TourismService.DifficultyLevel.MODERADO, 15, Arrays.asList("Español", "Inglés", "Quechua"),
                Arrays.asList("Transporte en bus", "Guía especializado", "Entrada a Machu Picchu"),
                Arrays.asList("Alimentación", "Tren a Aguas Calientes", "Propinas"),
                Arrays.asList("Llevar pasaporte original", "Ropa cómoda", "Protector solar"),
                3, Arrays.asList("https://example.com/machu1.jpg", "https://example.com/machu2.jpg"),
                "guide_001", "José Condori", "jose.condori@turismo.pe"),
                
            createTourismService("City Tour Cusco Colonial", "Recorrido por el centro histórico del Cusco visitando templos, museos y mercados tradicionales", 
                TourismService.ServiceCategory.CULTURAL, 80.00, "Centro Histórico Cusco", "CUSCO", 4,
                TourismService.DifficultyLevel.FACIL, 20, Arrays.asList("Español", "Inglés"),
                Arrays.asList("Guía local", "Entradas a sitios", "Mapa turístico"),
                Arrays.asList("Transporte", "Alimentación", "Compras personales"),
                Arrays.asList("Calzado cómodo", "Cámara fotográfica"),
                1, Arrays.asList("https://example.com/cusco1.jpg", "https://example.com/cusco2.jpg"),
                "guide_002", "María Huamán", "maria.huaman@turismo.pe"),

            // TOURS DE NATURALEZA
            createTourismService("Trekking Salkantay", "Caminata de 3 días por la ruta alternativa a Machu Picchu atravesando paisajes andinos espectaculares", 
                TourismService.ServiceCategory.NATURALEZA, 420.00, "Nevado Salkantay", "CUSCO", 72,
                TourismService.DifficultyLevel.DIFICIL, 12, Arrays.asList("Español", "Inglés"),
                Arrays.asList("Guía de montaña", "Campamento completo", "Cocinero", "Porteadores"),
                Arrays.asList("Saco de dormir personal", "Bastones de trekking", "Seguro personal"),
                Arrays.asList("Excelente condición física", "Ropa de abrigo", "Medicamentos personales"),
                7, Arrays.asList("https://example.com/salkantay1.jpg", "https://example.com/salkantay2.jpg"),
                "guide_003", "Carlos Mamani", "carlos.mamani@aventura.pe"),
                
            createTourismService("Avistamiento de Aves Manu", "Expedición ornitológica al Parque Nacional del Manu, uno de los hotspots de biodiversidad mundial", 
                TourismService.ServiceCategory.NATURALEZA, 580.00, "Parque Nacional Manu", "MADRE DE DIOS", 48,
                TourismService.DifficultyLevel.MODERADO, 8, Arrays.asList("Español", "Inglés"),
                Arrays.asList("Guía ornitólogo", "Transporte 4x4", "Hospedaje en lodge", "Todas las comidas"),
                Arrays.asList("Vuelos", "Binoculares profesionales", "Seguro médico"),
                Arrays.asList("Vacuna fiebre amarilla", "Repelente", "Ropa manga larga"),
                15, Arrays.asList("https://example.com/manu1.jpg", "https://example.com/manu2.jpg"),
                "guide_004", "Pedro Vargas", "pedro.vargas@naturaleza.pe"),

            // TOURS GASTRONÓMICOS
            createTourismService("Tour Gastronómico Lima", "Recorrido culinario por los mejores mercados y restaurantes de Lima con degustaciones incluidas", 
                TourismService.ServiceCategory.GASTRONOMICO, 120.00, "Lima Centro", "LIMA", 6,
                TourismService.DifficultyLevel.FACIL, 12, Arrays.asList("Español", "Inglés"),
                Arrays.asList("Guía gastronómico", "Degustaciones", "Bebidas tradicionales", "Recetario digital"),
                Arrays.asList("Almuerzo completo", "Transporte privado", "Propinas"),
                Arrays.asList("Estómago vacío", "Apetito aventurero", "Cámara para fotos"),
                2, Arrays.asList("https://example.com/lima_food1.jpg", "https://example.com/lima_food2.jpg"),
                "guide_005", "Sofía Ramírez", "sofia.ramirez@gastronomia.pe"),

            // TOURS DE AVENTURA
            createTourismService("Rafting Urubamba", "Descenso en ráfting por el río Urubamba con rápidos clase III-IV, incluye equipo completo de seguridad", 
                TourismService.ServiceCategory.AVENTURA, 95.00, "Valle Sagrado", "CUSCO", 5,
                TourismService.DifficultyLevel.MODERADO, 8, Arrays.asList("Español", "Inglés"),
                Arrays.asList("Equipo de rafting", "Guía especializado", "Seguro de aventura", "Fotos del tour"),
                Arrays.asList("Ropa de cambio", "Toalla personal", "Transporte al hotel"),
                Arrays.asList("Saber nadar", "Mayor de 12 años", "Buena salud física"),
                1, Arrays.asList("https://example.com/rafting1.jpg", "https://example.com/rafting2.jpg"),
                "guide_006", "Diego Quispe", "diego.quispe@aventura.pe"),

            // TOURS RELIGIOSOS
            createTourismService("Ceremonia Ayahuasca Tradicional", "Experiencia espiritual auténtica con maestro curandero en entorno natural protegido", 
                TourismService.ServiceCategory.RELIGIOSO, 200.00, "Iquitos", "LORETO", 12,
                TourismService.DifficultyLevel.MODERADO, 6, Arrays.asList("Español", "Shipibo"),
                Arrays.asList("Maestro curandero", "Lugar ceremonial", "Cuidado post-ceremonia", "Orientación previa"),
                Arrays.asList("Alimentos antes de ceremonia", "Transporte a la ciudad", "Hospedaje"),
                Arrays.asList("Ayuno previo de 24h", "No medicamentos", "Mente abierta", "Mayor de 18 años"),
                7, Arrays.asList("https://example.com/ayahuasca1.jpg", "https://example.com/ayahuasca2.jpg"),
                "guide_007", "Don Roberto Acho", "don.roberto@medicinas.pe"),

            // TOURS ARQUEOLÓGICOS
            createTourismService("Circuito Arqueológico Chachapoya", "Exploración de la fortaleza de Kuelap y sarcófagos de Karajía con explicación arqueológica", 
                TourismService.ServiceCategory.ARQUEOLOGICO, 280.00, "Kuelap", "AMAZONAS", 10,
                TourismService.DifficultyLevel.MODERADO, 12, Arrays.asList("Español", "Inglés"),
                Arrays.asList("Teleférico Kuelap", "Guía arqueólogo", "Entradas a sitios", "Almuerzo típico"),
                Arrays.asList("Hospedaje", "Transporte desde Chachapoyas", "Cenas"),
                Arrays.asList("Calzado antideslizante", "Ropa de lluvia", "Cámara fotográfica"),
                2, Arrays.asList("https://example.com/kuelap1.jpg", "https://example.com/kuelap2.jpg"),
                "guide_008", "Elena Tuesta", "elena.tuesta@arqueologia.pe")
        );

        // Guardar servicios en Firestore
        for (TourismService service : services) {
            try {
                firestore.collection("tourismServices").add(service).get();
                log.info("✅ Servicio creado: {}", service.getName());
            } catch (Exception e) {
                log.error("❌ Error al crear servicio {}: {}", service.getName(), e.getMessage());
            }
        }
    }

    /**
     * Crear producto con datos completos
     */
    private Product createProduct(String title, String description, Product.ProductCategory category, 
                                Product.ProductType type, Double price, String location, String department,
                                List<String> images, String artisanId, String artisanName, String artisanEmail, String artisanPhone) {
        Product product = new Product();
        
        product.setName(title);
        product.setDescription(description);
        product.setCategory(category);
        product.setType(type);
        product.setPrice(price);
        product.setCurrency("PEN");
        product.setOrigin(location);
        product.setDepartment(department);
        product.setMainImageUrl(images.get(0));
        product.setAdditionalImages(images);
        product.setArtisanId(artisanId);
        product.setArtisanName(artisanName);
        product.setArtisanContact(artisanEmail + " | " + artisanPhone);
        
        // Datos aleatorios pero realistas
        product.setStockQuantity(random.nextInt(20) + 5); // Entre 5 y 24
        product.setRating(4.0 + random.nextDouble() * 1.0); // Entre 4.0 y 5.0
        product.setTotalRatings(random.nextInt(50) + 10); // Entre 10 y 59
        product.setViewCount(random.nextInt(200) + 50); // Entre 50 y 249
        product.setIsAvailable(true);
        product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)).toString());
        product.setUpdatedAt(LocalDateTime.now().toString());
        
        return product;
    }

    /**
     * Crear servicio turístico con datos completos
     */
    private TourismService createTourismService(String title, String description, TourismService.ServiceCategory category,
                                              Double price, String location, String department, Integer durationHours,
                                              TourismService.DifficultyLevel difficulty, Integer maxGroupSize, List<String> languages,
                                              List<String> included, List<String> notIncluded, List<String> requirements,
                                              Integer advanceBookingDays, List<String> images, String guideId, String guideName, String guideContact) {
        TourismService service = new TourismService();
        
        service.setName(title);
        service.setDescription(description);
        service.setCategory(category);
        service.setPricePerPerson(price);
        service.setCurrency("PEN");
        service.setLocation(location);
        service.setDepartment(department);
        service.setDuration(durationHours + " horas");
        service.setDifficulty(difficulty);
        service.setMaxGroupSize(maxGroupSize);
        service.setSpokenLanguages(languages);
        service.setIncluded(included);
        service.setNotIncluded(notIncluded);
        service.setPhysicalRequirement(requirements.get(0)); // Take first as physical requirement
        service.setRequiresAdvanceBooking(advanceBookingDays > 0);
        service.setMainImageUrl(images.get(0));
        service.setAdditionalImages(images);
        service.setGuideId(guideId);
        service.setGuideName(guideName);
        service.setGuideContact(guideContact);
        
        // Datos aleatorios pero realistas
        service.setRating(4.2 + random.nextDouble() * 0.8); // Entre 4.2 y 5.0
        service.setTotalRatings(random.nextInt(30) + 5); // Entre 5 y 34
        service.setViewCount(random.nextInt(150) + 25); // Entre 25 y 174
        service.setIsAvailable(true);
        service.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(180)).toString());
        service.setUpdatedAt(LocalDateTime.now().toString());
        
        return service;
    }
}
