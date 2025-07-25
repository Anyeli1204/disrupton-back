# 🔥 Configuración de Firebase para Disruptón 2025

## 📋 **Prerrequisitos**

1. **Cuenta de Google Cloud Platform**
2. **Proyecto Firebase creado**
3. **Java 11+ instalado**
4. **Maven configurado**

## 🚀 **Paso 1: Crear Proyecto Firebase**

### **1.1 Ir a Firebase Console**
- Ve a [console.firebase.google.com](https://console.firebase.google.com)
- Haz clic en "Crear un proyecto"
- Nombre del proyecto: `disrupton2025`
- ID del proyecto: `disrupton2025` (o el que prefieras)

### **1.2 Habilitar Servicios**
- **Firestore Database**: Crear base de datos en modo de producción
- **Storage**: Crear bucket de almacenamiento
- **Authentication**: Habilitar autenticación por email/password

## 🔑 **Paso 2: Generar Clave de Servicio**

### **2.1 Obtener Archivo de Credenciales**
1. En Firebase Console, ve a **Configuración del proyecto**
2. Pestaña **Cuentas de servicio**
3. Selecciona **Firebase Admin SDK**
4. Haz clic en **"Generar nueva clave privada"**
5. Descarga el archivo JSON

### **2.2 Colocar Archivo en el Proyecto**
```bash
# Copia el archivo descargado a:
src/main/resources/firebase-service-account.json
```

## ⚙️ **Paso 3: Configurar Variables**

### **3.1 Actualizar FirebaseConfig.java**
```java
// En FirebaseConfig.java, actualiza estos valores:
.setStorageBucket("TU_PROJECT_ID.appspot.com")
.setProjectId("TU_PROJECT_ID")
```

### **3.2 Actualizar FirebaseStorageService.java**
```java
// En FirebaseStorageService.java, actualiza:
private static final String BUCKET_NAME = "TU_PROJECT_ID.appspot.com";
```

## 🏗️ **Paso 4: Configurar Firestore**

### **4.1 Crear Colecciones**
En Firebase Console > Firestore Database, crea estas colecciones:

#### **users**
```json
{
  "name": "string",
  "email": "string",
  "role": "student|moderator|admin",
  "createdAt": "timestamp"
}
```

#### **cultural_objects**
```json
{
  "title": "string",
  "description": "string",
  "modelUrl": "string (URL en Firebase Storage del modelo 3D)",
  "createdBy": "string (reference a documento en users)",
  "createdAt": "timestamp",
  "status": "pending|approved|rejected"
}
```

#### **comments**
```json
{
  "objectId": "string (reference a cultural_objects)",
  "userId": "string (reference a users)",
  "text": "string",
  "createdAt": "timestamp"
}
```

#### **reactions**
```json
{
  "reactionId": "string",
  "type": "LIKE|LOVE|WOW|INTERESTING|EDUCATIONAL|CULTURAL_HERITAGE",
  "createdAt": "timestamp",
  "userId": "string",
  "culturalObjectId": "string"
}
```

#### **ar_photos**
```json
{
  "photoId": "string",
  "imageUrl": "string",
  "thumbnailUrl": "string",
  "createdAt": "timestamp",
  "userId": "string",
  "culturalObjectId": "string",
  "reflection": "string",
  "location": {
    "latitude": "number",
    "longitude": "number",
    "address": "string"
  },
  "tags": ["string"],
  "sharedToSocial": "boolean",
  "socialPlatforms": ["INSTAGRAM", "FACEBOOK", "TWITTER"]
}
```

#### **moderation_queue**
```json
{
  "queueId": "string",
  "culturalObjectId": "string",
  "submittedAt": "timestamp",
  "assignedTo": "string",
  "priority": "LOW|MEDIUM|HIGH",
  "status": "PENDING|IN_REVIEW|APPROVED|REJECTED",
  "reviewNotes": "string"
}
```

### **4.2 Configurar Reglas de Seguridad**

#### **Firestore Rules**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuarios pueden leer su propio perfil
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null; // Otros usuarios pueden ver perfiles
    }
    
    // Objetos culturales
    match /cultural_objects/{objectId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
        request.auth.token.role == 'STUDENT';
      allow update: if request.auth != null && 
        (request.auth.uid == resource.data.contributorId || 
         request.auth.token.role == 'MODERATOR');
    }
    
    // Comentarios
    match /comments/{commentId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.auth.uid == resource.data.authorId;
    }
    
    // Reacciones
    match /reactions/{reactionId} {
      allow read: if request.auth != null;
      allow create, delete: if request.auth != null;
    }
    
    // Fotos de RA
    match /ar_photos/{photoId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
    
    // Cola de moderación
    match /moderation_queue/{queueId} {
      allow read, write: if request.auth != null && 
        request.auth.token.role == 'MODERATOR';
    }
  }
}
```

#### **Storage Rules**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /disrupton-2025/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.resource.size < 50 * 1024 * 1024; // 50MB max
    }
  }
}
```

## 🧪 **Paso 5: Probar la Configuración**

### **5.1 Ejecutar la Aplicación**
```bash
mvn spring-boot:run
```

### **5.2 Verificar Logs**
Deberías ver estos mensajes en los logs:
```
🚀 Inicializando Firebase Admin SDK...
✅ Firebase Admin SDK inicializado correctamente
```

### **5.3 Probar Endpoints**
```bash
# Crear usuario de prueba
curl -X POST http://localhost:8080/api/firebase/cultural/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@utec.edu.pe",
    "fullName": "Usuario de Prueba",
    "region": "Lima",
    "role": "STUDENT",
    "isActive": true,
    "studentId": "20240001"
  }'

# Obtener usuarios
curl http://localhost:8080/api/firebase/cultural/users
```

## 🔧 **Paso 6: Configuración Avanzada**

### **6.1 Variables de Entorno (Opcional)**
```bash
# En application.properties
firebase.project.id=disrupton2025
firebase.storage.bucket=disrupton2025.appspot.com
firebase.service.account.path=classpath:firebase-service-account.json
```

### **6.2 Configuración de Índices**
En Firebase Console > Firestore > Índices, crear:

1. **cultural_objects**: `status` + `createdAt` (Descending)
2. **cultural_objects**: `region` + `status` + `createdAt` (Descending)
3. **cultural_objects**: `culturalType` + `status` + `createdAt` (Descending)
4. **cultural_objects**: `contributorId` + `createdAt` (Descending)

## 🚨 **Solución de Problemas**

### **Error: "Firebase Admin SDK not initialized"**
- Verifica que el archivo `firebase-service-account.json` existe
- Revisa que las credenciales sean correctas
- Asegúrate de que el proyecto ID coincida

### **Error: "Permission denied"**
- Verifica las reglas de Firestore
- Asegúrate de que el usuario tenga los permisos correctos
- Revisa que la autenticación esté configurada

### **Error: "Bucket not found"**
- Verifica que el bucket de Storage esté creado
- Asegúrate de que el nombre del bucket sea correcto
- Revisa que las reglas de Storage permitan acceso

## 📚 **Recursos Adicionales**

- [Firebase Admin SDK Documentation](https://firebase.google.com/docs/admin/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Storage Rules](https://firebase.google.com/docs/storage/security)
- [Google Cloud Storage Java Client](https://cloud.google.com/storage/docs/reference/libraries)

## ✅ **Verificación Final**

Una vez completada la configuración, deberías poder:

1. ✅ Inicializar Firebase Admin SDK sin errores
2. ✅ Crear y leer usuarios en Firestore
3. ✅ Subir y descargar archivos de Storage
4. ✅ Ejecutar todas las operaciones CRUD en las colecciones
5. ✅ Ver los datos en Firebase Console

¿Necesitas ayuda con algún paso específico? 