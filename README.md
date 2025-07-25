# KIRI Engine API Integration

Integración completa con la API de KIRI Engine para generar modelos 3D a partir de imágenes o videos usando fotogrametría.

## 🚀 Características

- ✅ **Subida de Imágenes**: Procesa múltiples imágenes (20-300) para generar modelos 3D
- ✅ **Subida de Video**: Procesa videos para generar modelos 3D usando fotogrametría
- ✅ **Consulta de Estado**: Verifica el progreso del procesamiento en tiempo real
- ✅ **Descarga de Modelos**: Obtiene enlaces de descarga cuando el modelo está listo
- ✅ **Validaciones Completas**: Verifica formatos, tamaños y parámetros
- ✅ **Manejo de Errores**: Respuestas de error detalladas y logging completo
- ✅ **CORS Habilitado**: Compatible con aplicaciones web frontend

## 📋 Requisitos

- Java 11 o superior
- Spring Boot 2.7+
- Maven
- API Key de KIRI Engine

## 🛠️ Instalación

1. **Clonar el repositorio**
```bash
git clone <repository-url>
cd disrupton_proyecto
```

2. **Configurar la API Key**
Editar `src/main/resources/application.properties`:
```properties
kiri.engine.api-key=tu_api_key_aqui
kiri.engine.base-url=https://api.kiriengine.app/api/v1
```

3. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

## 📚 API Endpoints

### 🖼️ Subida de Imágenes (Photogrammetry)
```
POST /api/kiri-engine/upload-images
```

**Parámetros:**
- `imagesFiles`: Lista de archivos de imágenes (20-300 imágenes)
- `modelQuality`: Calidad del modelo (0: High, 1: Medium, 2: Low, 3: Ultra)
- `textureQuality`: Calidad de textura (0: 4K, 1: 2K, 2: 1K, 3: 8K)
- `fileFormat`: Formato de salida (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)
- `isMask`: Auto Object Masking (0: Off, 1: On)
- `textureSmoothing`: Texture Smoothing (0: Off, 1: On)

### 🎯 Subida de Imágenes (Featureless Object Scan)
```
POST /api/kiri-engine/upload-featureless-images
```

**Parámetros:**
- `imagesFiles`: Lista de archivos de imágenes (20-300 imágenes)
- `fileFormat`: Formato de salida (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)

**Algoritmo Especializado:**
- Optimizado para objetos sin características distintivas
- Perfecto para esferas, cilindros, objetos lisos
- Menos parámetros, procesamiento más rápido

### 🎥 Subida de Video (Photogrammetry)
```
POST /api/kiri-engine/upload-video
```

**Parámetros:**
- `videoFile`: Archivo de video (MP4, AVI, MOV, WMV, FLV, WEBM)
- `modelQuality`: Calidad del modelo (0: High, 1: Medium, 2: Low, 3: Ultra)
- `textureQuality`: Calidad de textura (0: 4K, 1: 2K, 2: 1K, 3: 8K)
- `fileFormat`: Formato de salida (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)
- `isMask`: Auto Object Masking (0: Off, 1: On)
- `textureSmoothing`: Texture Smoothing (0: Off, 1: On)

**Requisitos del Video:**
- Resolución máxima: 1920x1080
- Duración máxima: 3 minutos
- Tamaño máximo: 500MB

### 🎯 Subida de Video (Featureless Object Scan)
```
POST /api/kiri-engine/upload-featureless-video
```

**Parámetros:**
- `videoFile`: Archivo de video (MP4, AVI, MOV, WMV, FLV, WEBM)
- `fileFormat`: Formato de salida (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ)

**Requisitos del Video:**
- Resolución máxima: 1920x1080
- Duración máxima: 3 minutos
- Tamaño máximo: 500MB

**Algoritmo Especializado:**
- Optimizado para objetos sin características distintivas
- Perfecto para esferas, cilindros, objetos lisos
- Menos parámetros, procesamiento más rápido

### 📊 Consulta de Estado
```
GET /api/kiri-engine/model-status/{serial}
```

### 📥 Descarga de Modelo
```
GET /api/kiri-engine/download-model/{serial}
```

### 🏥 Health Check
```
GET /api/kiri-engine/health
```

## 💻 Ejemplos de Uso

### Subida de Imágenes (Photogrammetry) con cURL
```bash
curl --location --request POST 'http://localhost:8080/api/kiri-engine/upload-images' \
--form 'imagesFiles=@"/path/to/image1.jpg"' \
--form 'imagesFiles=@"/path/to/image2.jpg"' \
--form 'modelQuality="1"' \
--form 'textureQuality="1"' \
--form 'fileFormat="OBJ"' \
--form 'isMask="1"' \
--form 'textureSmoothing="1"'
```

### Subida de Imágenes (Featureless Object Scan) con cURL
```bash
curl --location --request POST 'http://localhost:8080/api/kiri-engine/upload-featureless-images' \
--form 'imagesFiles=@"/path/to/image1.jpg"' \
--form 'imagesFiles=@"/path/to/image2.jpg"' \
--form 'fileFormat="OBJ"'
```

### Subida de Video (Photogrammetry) con cURL
```bash
curl --location --request POST 'http://localhost:8080/api/kiri-engine/upload-video' \
--form 'videoFile=@"/path/to/video.mp4"' \
--form 'modelQuality="1"' \
--form 'textureQuality="1"' \
--form 'fileFormat="OBJ"' \
--form 'isMask="1"' \
--form 'textureSmoothing="1"'
```

### Subida de Video (Featureless Object Scan) con cURL
```bash
curl --location --request POST 'http://localhost:8080/api/kiri-engine/upload-featureless-video' \
--form 'videoFile=@"/path/to/video.mp4"' \
--form 'fileFormat="OBJ"'
```

### JavaScript (Fetch)
```javascript
// Subida de imágenes (Photogrammetry)
const formData = new FormData();
images.forEach(image => formData.append('imagesFiles', image));
formData.append('modelQuality', '1');
formData.append('textureQuality', '1');
formData.append('fileFormat', 'OBJ');

const response = await fetch('/api/kiri-engine/upload-images', {
    method: 'POST',
    body: formData
});

// Subida de imágenes (Featureless Object Scan)
const featurelessFormData = new FormData();
images.forEach(image => featurelessFormData.append('imagesFiles', image));
featurelessFormData.append('fileFormat', 'OBJ');

const featurelessResponse = await fetch('/api/kiri-engine/upload-featureless-images', {
    method: 'POST',
    body: featurelessFormData
});

// Subida de video (Photogrammetry)
const videoFormData = new FormData();
videoFormData.append('videoFile', videoFile);
videoFormData.append('modelQuality', '1');
videoFormData.append('textureQuality', '1');
videoFormData.append('fileFormat', 'OBJ');

const videoResponse = await fetch('/api/kiri-engine/upload-video', {
    method: 'POST',
    body: videoFormData
});

// Subida de video (Featureless Object Scan)
const featurelessFormData = new FormData();
featurelessFormData.append('videoFile', videoFile);
featurelessFormData.append('fileFormat', 'OBJ');

const featurelessResponse = await fetch('/api/kiri-engine/upload-featureless-video', {
    method: 'POST',
    body: featurelessFormData
});
```

### Python (requests)
```python
import requests

# Subida de imágenes (Photogrammetry)
files = [('imagesFiles', open('image1.jpg', 'rb')),
         ('imagesFiles', open('image2.jpg', 'rb'))]
data = {
    'modelQuality': '1',
    'textureQuality': '1',
    'fileFormat': 'OBJ'
}

response = requests.post('http://localhost:8080/api/kiri-engine/upload-images', 
                        files=files, data=data)

# Subida de imágenes (Featureless Object Scan)
files = [('imagesFiles', open('image1.jpg', 'rb')),
         ('imagesFiles', open('image2.jpg', 'rb'))]
data = {'fileFormat': 'OBJ'}

response = requests.post('http://localhost:8080/api/kiri-engine/upload-featureless-images', 
                        files=files, data=data)

# Subida de video (Photogrammetry)
files = {'videoFile': open('video.mp4', 'rb')}
data = {
    'modelQuality': '1',
    'textureQuality': '1',
    'fileFormat': 'OBJ'
}

response = requests.post('http://localhost:8080/api/kiri-engine/upload-video', 
                        files=files, data=data)

# Subida de video (Featureless Object Scan)
files = {'videoFile': open('video.mp4', 'rb')}
data = {'fileFormat': 'OBJ'}

response = requests.post('http://localhost:8080/api/kiri-engine/upload-featureless-video', 
                        files=files, data=data)
```

## 🔄 Flujo de Trabajo

### Para Imágenes (Photogrammetry):
1. **Subir imágenes** → `POST /api/kiri-engine/upload-images`
2. **Obtener serial** → Respuesta incluye `serialize` único
3. **Consultar estado** → `GET /api/kiri-engine/model-status/{serial}` (periódico)
4. **Descargar modelo** → `GET /api/kiri-engine/download-model/{serial}` (cuando esté listo)

### Para Imágenes (Featureless Object Scan):
1. **Subir imágenes** → `POST /api/kiri-engine/upload-featureless-images`
2. **Obtener serial** → Respuesta incluye `serialize` único
3. **Consultar estado** → `GET /api/kiri-engine/model-status/{serial}` (periódico)
4. **Descargar modelo** → `GET /api/kiri-engine/download-model/{serial}` (cuando esté listo)

### Para Video (Photogrammetry):
1. **Subir video** → `POST /api/kiri-engine/upload-video`
2. **Obtener serial** → Respuesta incluye `serialize` único
3. **Consultar estado** → `GET /api/kiri-engine/model-status/{serial}` (periódico)
4. **Descargar modelo** → `GET /api/kiri-engine/download-model/{serial}` (cuando esté listo)

### Para Video (Featureless Object Scan):
1. **Subir video** → `POST /api/kiri-engine/upload-featureless-video`
2. **Obtener serial** → Respuesta incluye `serialize` único
3. **Consultar estado** → `GET /api/kiri-engine/model-status/{serial}` (periódico)
4. **Descargar modelo** → `GET /api/kiri-engine/download-model/{serial}` (cuando esté listo)

## 📁 Estructura del Proyecto

```
src/main/java/com/disrupton/
├── controller/
│   └── KiriEngineController.java      # Controladores REST
├── service/
│   └── KiriEngineService.java         # Lógica de negocio
├── model/
│   ├── ImageUploadRequest.java        # Modelo para imágenes (Photogrammetry)
│   ├── FeaturelessImageUploadRequest.java # Modelo para imágenes (Featureless)
│   ├── VideoUploadRequest.java        # Modelo para video (Photogrammetry)
│   ├── FeaturelessVideoUploadRequest.java # Modelo para video (Featureless)
│   ├── KiriEngineResponse.java        # Respuesta de subida
│   ├── ModelStatusResponse.java       # Respuesta de estado
│   └── ModelDownloadResponse.java     # Respuesta de descarga
└── config/
    └── KiriEngineConfig.java          # Configuración
```

## 🧪 Testing

### Páginas de Prueba HTML
- Abre `test-video-upload.html` en tu navegador para probar la funcionalidad de video Photogrammetry
- Abre `test-featureless-video-upload.html` en tu navegador para probar la funcionalidad de video Featureless Object Scan
- Abre `test-featureless-image-upload.html` en tu navegador para probar la funcionalidad de imágenes Featureless Object Scan

### Endpoints de Prueba
```bash
# Health check
curl http://localhost:8080/api/kiri-engine/health

# Subida de video Photogrammetry (usar archivo real)
curl -X POST http://localhost:8080/api/kiri-engine/upload-video \
  -F "videoFile=@test-video.mp4" \
  -F "modelQuality=1" \
  -F "textureQuality=1" \
  -F "fileFormat=OBJ"

# Subida de video Featureless (usar archivo real)
curl -X POST http://localhost:8080/api/kiri-engine/upload-featureless-video \
  -F "videoFile=@test-video.mp4" \
  -F "fileFormat=OBJ"

# Subida de imágenes Featureless (usar archivos reales)
curl -X POST http://localhost:8080/api/kiri-engine/upload-featureless-images \
  -F "imagesFiles=@image1.jpg" \
  -F "imagesFiles=@image2.jpg" \
  -F "fileFormat=OBJ"
```

## ⚙️ Configuración

### application.properties
```properties
# KIRI Engine Configuration
kiri.engine.api-key=${KIRI_API_KEY}
kiri.engine.base-url=https://api.kiriengine.app/api/v1

# Server Configuration
server.port=8080
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

### Variables de Entorno
```bash
export KIRI_API_KEY="tu_api_key_aqui"
```

## 🚨 Manejo de Errores

### Códigos de Error Comunes
- **400 Bad Request**: Parámetros inválidos o archivos no válidos
- **500 Internal Server Error**: Error interno del servidor o API de KIRI Engine

### Respuestas de Error
```json
{
  "error": "Error de validación",
  "message": "Se requieren al menos 20 imágenes"
}
```

## 📝 Logging

El servicio incluye logging detallado para debugging:
- Subida de archivos
- Comunicación con KIRI Engine API
- Estados de procesamiento
- Errores y excepciones

## 🔧 Desarrollo

### Agregar Nuevos Endpoints
1. Crear modelo en `model/`
2. Agregar método en `KiriEngineService`
3. Agregar endpoint en `KiriEngineController`
4. Actualizar documentación

### Validaciones Personalizadas
Las validaciones se pueden extender en los modelos `ImageUploadRequest` y `VideoUploadRequest`.

## 📄 Licencia

Este proyecto está bajo la licencia MIT.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request

## 📞 Soporte

Para soporte técnico o preguntas:
- Crear un issue en GitHub
- Contactar al equipo de desarrollo

---

**Nota**: Asegúrate de tener una API key válida de KIRI Engine antes de usar este servicio.

---

## 🔥 **Integración con Firebase**

### **Configuración de Firebase**
Para configurar Firebase en tu proyecto, sigue la guía completa en:
**[FIREBASE_SETUP.md](FIREBASE_SETUP.md)**

### **Resumen de la Configuración:**
- ✅ **Dependencias agregadas** al `pom.xml`
- ✅ **Configuración de Firebase Admin SDK** implementada
- ✅ **DTOs para Firebase** creados (UserDto, CulturalObjectDto, etc.)
- ✅ **Servicios de Firebase** implementados (FirebaseUserService, FirebaseCulturalObjectService, FirebaseStorageService)
- ✅ **Controlador actualizado** para usar Firebase
- ✅ **Documentación completa** de configuración

### **Estructura de Archivos Firebase:**
```
src/main/java/com/disrupton/
├── config/
│   └── FirebaseConfig.java              # Configuración de Firebase Admin SDK
├── dto/
│   ├── UserDto.java                     # DTO para usuarios
│   ├── CulturalObjectDto.java           # DTO para objetos culturales
│   ├── CommentDto.java                  # DTO para comentarios
│   └── ReactionDto.java                 # DTO para reacciones
├── service/
│   ├── FirebaseUserService.java         # Servicio de usuarios
│   ├── FirebaseCulturalObjectService.java # Servicio de objetos culturales
│   └── FirebaseStorageService.java      # Servicio de almacenamiento
└── controller/
    └── FirebaseCulturalController.java  # Controlador con Firebase
```

### **Endpoints de Firebase:**
```bash
# Usuarios
POST   /api/firebase/cultural/users                    # Crear usuario
GET    /api/firebase/cultural/users/{userId}           # Obtener usuario
GET    /api/firebase/cultural/users/region/{region}    # Usuarios por región

# Objetos Culturales
POST   /api/firebase/cultural/upload                   # Subir objeto cultural
GET    /api/firebase/cultural/objects                  # Objetos aprobados
GET    /api/firebase/cultural/objects/{objectId}       # Objeto por ID
GET    /api/firebase/cultural/objects/region/{region}  # Por región
GET    /api/firebase/cultural/objects/type/{type}      # Por tipo cultural
GET    /api/firebase/cultural/objects/pending          # Pendientes de revisión
PUT    /api/firebase/cultural/objects/{objectId}/status # Actualizar estado
POST   /api/firebase/cultural/objects/{objectId}/like  # Dar like
DELETE /api/firebase/cultural/objects/{objectId}/like  # Quitar like
```

### **Próximos Pasos:**
1. **Configurar proyecto Firebase** ✅ (Ver `FIREBASE_SETUP.md`)
2. **Implementar autenticación** ✅ (Firebase Auth integrado)
3. **Crear estructura de Firestore** ✅ (Servicios implementados)
4. **Configurar reglas de seguridad** ✅ (Documentadas)
5. **Integrar con KIRI Engine API** ✅ (Ya implementado)
6. **Desarrollar aplicación móvil** 🔄 (En progreso) 