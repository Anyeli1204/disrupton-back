package com.disrupton.culturalAgent.util;

import com.disrupton.culturalAgent.model.AgenteCultural;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AgenteCulturalDataSeeder {

    private final Firestore firestore;

    /**
     * Verifica si ya existen datos de agentes culturales en Firestore
     */
    public boolean verificarDatosExistentes() {
        try {
            var agentesSnapshot = firestore.collection("agentes_culturales").limit(1).get().get();
            boolean tieneAgentes = !agentesSnapshot.isEmpty();
            
            log.info("🔍 Verificación de datos existentes: {} documentos encontrados", 
                    agentesSnapshot.size());
            
            return tieneAgentes;
            
        } catch (Exception e) {
            log.warn("⚠️ Error al verificar datos existentes, asumiendo que no existen: {}", 
                    e.getMessage());
            return false;
        }
    }

    /**
     * Inserta datos de ejemplo de agentes culturales
     */
    public void seedData() throws ExecutionException, InterruptedException {
        log.info("🌱 Iniciando inserción de datos de prueba para agentes culturales");

        List<AgenteCultural> agentesEjemplo = crearAgentesEjemplo();
        
        for (AgenteCultural agente : agentesEjemplo) {
            String docId = firestore.collection("agentes_culturales")
                    .add(agente)
                    .get()
                    .getId();
            
            log.info("✅ Agente creado: {} con ID: {}", agente.getName(), docId);
        }
        
        log.info("🎉 Datos de prueba insertados correctamente");
    }

    private List<AgenteCultural> crearAgentesEjemplo() {
        Timestamp now = Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond(), 0);
        
        return Arrays.asList(
            // ARTESANOS
            AgenteCultural.builder()
                    .name("María Elena Quispe")
                    .imageUrl("https://picsum.photos/300/300?random=1")
                    .region("Cusco")
                    .description("Artesana especializada en cerámica tradicional andina. Heredera de técnicas ancestrales transmitidas de generación en generación.")
                    .expertise("Cerámica Tradicional")
                    .specialties(Arrays.asList("Cerámica", "Alfarería", "Arte Andino"))
                    .type(AgenteCultural.AgentType.ARTISAN)
                    .phone("+51-984-123-456")
                    .whatsapp("+51984123456")
                    .email("maria.quispe@email.com")
                    .rating(4.8)
                    .totalRatings(45)
                    .location("Centro Histórico de Cusco")
                    .latitude(-13.5319)
                    .longitude(-71.9675)
                    .workPhotos(Arrays.asList(
                        "https://picsum.photos/400/300?random=10",
                        "https://picsum.photos/400/300?random=11",
                        "https://picsum.photos/400/300?random=12"
                    ))
                    .isActive(true)
                    .department("Cusco")
                    .province("Cusco")
                    .district("Wanchaq")
                    .craftTechnique("Cerámica quemada al aire libre")
                    .mainMaterials("Arcilla roja, pigmentos naturales")
                    .experienceYears(25)
                    .createdAt(now)
                    .updatedAt(now)
                    .build(),

            AgenteCultural.builder()
                    .name("Carlos Mamani Condori")
                    .imageUrl("https://picsum.photos/300/300?random=2")
                    .region("Puno")
                    .description("Maestro textil especializado en tejidos tradicionales aymaras. Sus trabajos han sido exhibidos en museos internacionales.")
                    .expertise("Textiles Andinos")
                    .specialties(Arrays.asList("Textiles", "Tejidos", "Alpaca", "Llama"))
                    .type(AgenteCultural.AgentType.ARTISAN)
                    .phone("+51-987-654-321")
                    .whatsapp("+51987654321")
                    .email("carlos.mamani@email.com")
                    .rating(4.9)
                    .totalRatings(62)
                    .location("Isla Taquile, Lago Titicaca")
                    .latitude(-15.7691)
                    .longitude(-69.6803)
                    .workPhotos(Arrays.asList(
                        "https://picsum.photos/400/300?random=13",
                        "https://picsum.photos/400/300?random=14"
                    ))
                    .isActive(true)
                    .department("Puno")
                    .province("Puno")
                    .district("Taquile")
                    .craftTechnique("Tejido en telar de cintura")
                    .mainMaterials("Fibra de alpaca, lana de oveja, tintes naturales")
                    .experienceYears(35)
                    .createdAt(now)
                    .updatedAt(now)
                    .build(),

            AgenteCultural.builder()
                    .name("Rosa Huamán Flores")
                    .imageUrl("https://picsum.photos/300/300?random=3")
                    .region("Ayacucho")
                    .description("Artesana especialista en retablos ayacuchanos y trabajos en piedra de Huamanga. Ganadora de múltiples concursos nacionales.")
                    .expertise("Retablos y Piedra de Huamanga")
                    .specialties(Arrays.asList("Retablos", "Piedra de Huamanga", "Escultura"))
                    .type(AgenteCultural.AgentType.ARTISAN)
                    .phone("+51-965-789-123")
                    .whatsapp("+51965789123")
                    .email("rosa.huaman@email.com")
                    .rating(4.7)
                    .totalRatings(38)
                    .location("Barrio Santa Ana")
                    .latitude(-13.1631)
                    .longitude(-74.2236)
                    .workPhotos(Arrays.asList(
                        "https://picsum.photos/400/300?random=15",
                        "https://picsum.photos/400/300?random=16",
                        "https://picsum.photos/400/300?random=17"
                    ))
                    .isActive(true)
                    .department("Ayacucho")
                    .province("Huamanga")
                    .district("Ayacucho")
                    .craftTechnique("Tallado en piedra de Huamanga")
                    .mainMaterials("Piedra de Huamanga, madera, pigmentos")
                    .experienceYears(20)
                    .createdAt(now)
                    .updatedAt(now)
                    .build(),

            // GUÍAS TURÍSTICOS
            AgenteCultural.builder()
                    .name("Luis Fernando Ccahuana")
                    .imageUrl("https://picsum.photos/300/300?random=4")
                    .region("Cusco")
                    .description("Guía oficial de turismo especializado en tours culturales e históricos por el Valle Sagrado y Machu Picchu.")
                    .expertise("Turismo Cultural e Histórico")
                    .specialties(Arrays.asList("Historia Inca", "Arqueología", "Trekking Cultural"))
                    .type(AgenteCultural.AgentType.TOURIST_GUIDE)
                    .phone("+51-988-111-222")
                    .whatsapp("+51988111222")
                    .email("luis.ccahuana@email.com")
                    .rating(4.9)
                    .totalRatings(156)
                    .location("Plaza de Armas de Cusco")
                    .latitude(-13.5164)
                    .longitude(-71.9785)
                    .workPhotos(Arrays.asList(
                        "https://picsum.photos/400/300?random=18",
                        "https://picsum.photos/400/300?random=19"
                    ))
                    .isActive(true)
                    .department("Cusco")
                    .province("Cusco")
                    .district("Cusco")
                    .spokenLanguages(Arrays.asList("Español", "Inglés", "Quechua", "Francés"))
                    .guideLicense("GTC-2019-001234")
                    .touristZones(Arrays.asList("Machu Picchu", "Valle Sagrado", "Cusco Centro", "Sacsayhuamán"))
                    .tourismType("Cultural e Histórico")
                    .experienceYears(12)
                    .createdAt(now)
                    .updatedAt(now)
                    .build(),

            AgenteCultural.builder()
                    .name("Ana Lucía Mendoza")
                    .imageUrl("https://picsum.photos/300/300?random=5")
                    .region("Arequipa")
                    .description("Guía especializada en turismo gastronómico y aventura. Experta en la región del Cañón del Colca y gastronomía arequipeña.")
                    .expertise("Turismo Gastronómico y Aventura")
                    .specialties(Arrays.asList("Gastronomía", "Senderismo", "Observación de Cóndores", "Termas"))
                    .type(AgenteCultural.AgentType.TOURIST_GUIDE)
                    .phone("+51-977-333-444")
                    .whatsapp("+51977333444")
                    .email("ana.mendoza@email.com")
                    .rating(4.8)
                    .totalRatings(89)
                    .location("Plaza de Armas de Arequipa")
                    .latitude(-16.4090)
                    .longitude(-71.5375)
                    .workPhotos(Arrays.asList(
                        "https://picsum.photos/400/300?random=20",
                        "https://picsum.photos/400/300?random=21",
                        "https://picsum.photos/400/300?random=22"
                    ))
                    .isActive(true)
                    .department("Arequipa")
                    .province("Arequipa")
                    .district("Cercado")
                    .spokenLanguages(Arrays.asList("Español", "Inglés", "Portugués"))
                    .guideLicense("GTC-2020-005678")
                    .touristZones(Arrays.asList("Cañón del Colca", "Monasterio de Santa Catalina", "Volcán Misti", "Yanahuara"))
                    .tourismType("Gastronómico y Aventura")
                    .experienceYears(8)
                    .createdAt(now)
                    .updatedAt(now)
                    .build(),

            AgenteCultural.builder()
                    .name("Pedro Martín Huillca")
                    .imageUrl("https://picsum.photos/300/300?random=6")
                    .region("Huacachina")
                    .description("Guía de aventura especializado en deportes extremos en dunas y oasis. Instructor certificado de sandboarding y buggies.")
                    .expertise("Turismo de Aventura Extrema")
                    .specialties(Arrays.asList("Sandboarding", "Buggies", "Deportes Extremos", "Fotografía de Paisajes"))
                    .type(AgenteCultural.AgentType.TOURIST_GUIDE)
                    .phone("+51-966-555-777")
                    .whatsapp("+51966555777")
                    .email("pedro.huillca@email.com")
                    .rating(4.6)
                    .totalRatings(73)
                    .location("Oasis de Huacachina")
                    .latitude(-14.0874)
                    .longitude(-75.7626)
                    .workPhotos(Arrays.asList(
                        "https://picsum.photos/400/300?random=23",
                        "https://picsum.photos/400/300?random=24"
                    ))
                    .isActive(true)
                    .department("Ica")
                    .province("Ica")
                    .district("Ica")
                    .spokenLanguages(Arrays.asList("Español", "Inglés"))
                    .guideLicense("GTC-2021-009876")
                    .touristZones(Arrays.asList("Huacachina", "Dunas de Ica", "Nazca", "Paracas"))
                    .tourismType("Aventura Extrema")
                    .experienceYears(6)
                    .createdAt(now)
                    .updatedAt(now)
                    .build()
        );
    }
}
