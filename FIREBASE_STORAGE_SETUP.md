# 📁 Configuración de Firebase Storage para Modelos 3D

## 🎯 **Objetivo**
Configurar Firebase Storage para almacenar y servir modelos 3D generados por KIRI Engine, imágenes de objetos culturales y fotos de Realidad Aumentada.

## 🚀 **Paso 1: Habilitar Firebase Storage**

### **1.1 Crear Bucket de Storage**
1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto `disrupton2025`
3. En el menú lateral, ve a **Storage**
4. Haz clic en **"Comenzar"**
5. Selecciona **"Comenzar en modo de producción"**
6. Elige la ubicación del bucket (recomendado: `us-central1` para mejor rendimiento)

### **1.2 Configurar Reglas de Storage**
En Firebase Console > Storage > Rules, reemplaza las reglas con:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Permitir lectura a usuarios autenticados
    match /disrupton2025/{allPaths=**} {
      allow read: if request.auth != null;
      
      // Permitir escritura a usuarios autenticados con límites
      allow write: if request.auth != null 
        && request.resource.size < 100 * 1024 * 1024  // 100MB max
        && request.resource.contentType.matches('image/.*|model/.*|application/.*');
    }
  }
}
```

## 📂 **Paso 2: Estructura de Carpetas**

### **2.1 Organización Propuesta**
```
/disrupton2025/
├── cultural-objects/           # Modelos 3D de objetos culturales
│   ├── {objectId}/
│   │   ├── model.obj          # Modelo 3D principal
│   │   ├── model.glb          # Modelo para web/móvil
│   │   ├── thumbnail.jpg      # Imagen miniatura
│   │   └── original-images/   # Imágenes originales
│   │       ├── image1.jpg
│   │       ├── image2.jpg
│   │       └── ...
├── ar-photos/                  # Fotos de Realidad Aumentada
│   ├── {photoId}/
│   │   ├── photo.jpg          # Foto con RA
│   │   └── thumbnail.jpg      # Miniatura
└── user-profiles/             # Fotos de perfil de usuarios
    ├── {userId}/
    │   └── profile.jpg
```

## 🔧 **Paso 3: Configurar el Servicio**

### **3.1 Actualizar FirebaseStorageService.java**
```java
// Asegúrate de que el BUCKET_NAME coincida con tu proyecto
private static final String BUCKET_NAME = "disrupton2025.appspot.com";
```

### **3.2 Métodos Principales**

#### **Subir Modelo 3D**
```java
// Después de obtener el modelo de KIRI Engine
String modelUrl = storageService.uploadModel3D(modelData, objectId, "OBJ");
String glbUrl = storageService.uploadModel3D(glbData, objectId, "GLB");
```

#### **Subir Imagen Miniatura**
```java
// Generar miniatura del modelo 3D
String thumbnailUrl = storageService.uploadThumbnail(thumbnailData, objectId);
```

#### **Subir Fotos de RA**
```java
// Subir foto con Realidad Aumentada
String photoUrl = storageService.uploadARPhoto(photoFile, photoId);
```

## 📱 **Paso 4: Integración con KIRI Engine**

### **4.1 Flujo Completo**
```java
// 1. Subir imágenes a KIRI Engine
KiriEngineResponse response = kiriEngineService.uploadImages(imageRequest);

// 2. Esperar procesamiento y descargar modelo
byte[] modelData = kiriEngineService.downloadModel(response.getSerial());

// 3. Subir modelo a Firebase Storage
String modelUrl = storageService.uploadModel3D(modelData, objectId, "OBJ");

// 4. Actualizar objeto cultural con la URL
culturalObjectService.updateModelUrl(objectId, modelUrl);
```

### **4.2 Ejemplo de Implementación**
```java
@PostMapping("/objects")
public ResponseEntity<?> uploadCulturalObject(
        @RequestParam("title") String title,
        @RequestParam("description") String description,
        @RequestParam("createdBy") String createdBy,
        @RequestParam("images") List<MultipartFile> images) {
    
    try {
        // 1. Crear objeto cultural
        CulturalObjectDto culturalObject = new CulturalObjectDto();
        culturalObject.setTitle(title);
        culturalObject.setDescription(description);
        culturalObject.setCreatedBy(createdBy);
        culturalObject.setStatus("pending");
        
        CulturalObjectDto savedObject = culturalObjectService.saveCulturalObject(culturalObject);
        
        // 2. Subir imágenes a KIRI Engine
        ImageUploadRequest imageRequest = new ImageUploadRequest();
        imageRequest.setImagesFiles(images);
        imageRequest.setModelQuality(ModelQuality.MEDIUM);
        imageRequest.setTextureQuality(TextureQuality.TWO_K);
        imageRequest.setFileFormat(FileFormat.OBJ);
        
        KiriEngineResponse kiriResponse = kiriEngineService.uploadImages(imageRequest);
        
        // 3. Esperar procesamiento (en producción usar webhooks)
        Thread.sleep(30000); // 30 segundos de ejemplo
        
        // 4. Descargar modelo
        byte[] modelData = kiriEngineService.downloadModel(kiriResponse.getSerial());
        
        // 5. Subir a Firebase Storage
        String modelUrl = storageService.uploadModel3D(modelData, savedObject.getObjectId(), "OBJ");
        
        // 6. Actualizar objeto cultural
        culturalObjectService.updateModelUrl(savedObject.getObjectId(), modelUrl);
        
        return ResponseEntity.ok(savedObject);
        
    } catch (Exception e) {
        log.error("Error al procesar objeto cultural", e);
        return ResponseEntity.internalServerError().body("Error al procesar la solicitud");
    }
}
```

## 🔐 **Paso 5: Seguridad y Optimización**

### **5.1 Reglas de Seguridad Avanzadas**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /disrupton2025/{allPaths=**} {
      // Lectura pública para modelos aprobados
      allow read: if request.auth != null || 
        (resource.metadata.status == 'approved' && 
         request.path.matches('.*/cultural-objects/.*/model\\..*'));
      
      // Escritura solo para usuarios autenticados
      allow write: if request.auth != null 
        && request.resource.size < 100 * 1024 * 1024
        && request.resource.contentType.matches('image/.*|model/.*|application/.*')
        && request.resource.metadata.uploadedBy == request.auth.uid;
    }
  }
}
```

### **5.2 Optimización de Archivos**
- **Modelos 3D**: Comprimir antes de subir
- **Imágenes**: Redimensionar y comprimir
- **Miniaturas**: Generar automáticamente
- **CDN**: Usar Firebase Hosting para distribución

## 📊 **Paso 6: Monitoreo y Costos**

### **6.1 Métricas a Monitorear**
- **Uso de almacenamiento**: GB utilizados
- **Descargas**: Número de archivos descargados
- **Transferencia**: GB transferidos
- **Errores**: Fallos en subida/descarga

### **6.2 Estimación de Costos**
```
Fase 1 (100 estudiantes):
- Storage: ~5GB = $0.10/mes
- Transferencia: ~50GB = $4.50/mes
- Total: ~$4.60/mes

Fase 2 (1,000 estudiantes):
- Storage: ~50GB = $1.00/mes
- Transferencia: ~500GB = $45.00/mes
- Total: ~$46.00/mes
```

## 🧪 **Paso 7: Pruebas**

### **7.1 Probar Subida de Archivos**
```bash
# Subir imagen de prueba
curl -X POST http://localhost:8080/api/firebase/storage/upload \
  -F "file=@test-image.jpg" \
  -F "folder=cultural-objects/test"

# Subir modelo 3D
curl -X POST http://localhost:8080/api/firebase/storage/upload-model \
  -F "modelData=@test-model.obj" \
  -F "objectId=test123" \
  -F "format=OBJ"
```

### **7.2 Verificar en Firebase Console**
1. Ve a Firebase Console > Storage
2. Verifica que los archivos se subieron correctamente
3. Comprueba las URLs de descarga
4. Prueba el acceso desde la aplicación

## 🚨 **Solución de Problemas**

### **Error: "Bucket not found"**
- Verifica que el bucket esté creado en Firebase Console
- Asegúrate de que el nombre del bucket sea correcto
- Revisa las credenciales de servicio

### **Error: "Permission denied"**
- Verifica las reglas de Storage
- Asegúrate de que el usuario esté autenticado
- Revisa que el archivo no exceda el tamaño límite

### **Error: "Content type not allowed"**
- Verifica que el tipo de archivo esté permitido en las reglas
- Asegúrate de que el content-type sea correcto
- Revisa la extensión del archivo

## ✅ **Verificación Final**

Una vez completada la configuración, deberías poder:

1. ✅ Subir modelos 3D a Firebase Storage
2. ✅ Generar URLs públicas para descarga
3. ✅ Integrar con KIRI Engine API
4. ✅ Optimizar archivos automáticamente
5. ✅ Monitorear uso y costos

¿Necesitas ayuda con algún paso específico de la configuración de Storage? 