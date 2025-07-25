# 🏗️ Arquitectura Firebase para Disruptón 2025

## 📊 **Estructura de Base de Datos**

### **Firestore Collections**

#### **1. users**
```json
{
  "userId": "string",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "region": "string",
  "role": "STUDENT|MODERATOR|ADMIN",
  "createdAt": "timestamp",
  "lastLogin": "timestamp",
  "isActive": "boolean",
  "profileImage": "string (URL)",
  "studentId": "string (UTEC ID)"
}
```

#### **2. cultural_objects**
```json
{
  "objectId": "string",
  "name": "string",
  "description": "string",
  "origin": "string",
  "culturalType": "ARTESANIA|GASTRONOMIA|MUSICA|DANZA|TEXTIL|CERAMICA|ARQUITECTURA|FESTIVAL|RITUAL|LENGUAJE|TRADICION|OTRO",
  "localPhrases": "string",
  "story": "string",
  "region": "string",
  "captureNotes": "string",
  
  // Información técnica
  "kiriEngineSerial": "string",
  "modelUrl": "string (Firebase Storage URL)",
  "thumbnailUrl": "string (Firebase Storage URL)",
  "fileFormat": "OBJ|FBX|STL|PLY|GLB|GLTF|USDZ|XYZ",
  "numberOfImages": "number",
  
  // Metadatos
  "status": "DRAFT|PENDING_REVIEW|APPROVED|REJECTED",
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  
  // Relaciones
  "contributorId": "string (reference to users)",
  "moderatorId": "string (reference to users)",
  "moderationFeedback": "string",
  
  // Estadísticas
  "viewCount": "number",
  "likeCount": "number",
  "commentCount": "number"
}
```

#### **3. comments**
```json
{
  "commentId": "string",
  "content": "string",
  "createdAt": "timestamp",
  "authorId": "string (reference to users)",
  "culturalObjectId": "string (reference to cultural_objects)",
  "parentCommentId": "string (for nested replies)",
  "isModerated": "boolean",
  "moderationStatus": "PENDING|APPROVED|REJECTED"
}
```

#### **4. reactions**
```json
{
  "reactionId": "string",
  "type": "LIKE|LOVE|WOW|INTERESTING|EDUCATIONAL|CULTURAL_HERITAGE",
  "createdAt": "timestamp",
  "userId": "string (reference to users)",
  "culturalObjectId": "string (reference to cultural_objects)"
}
```

#### **5. ar_photos**
```json
{
  "photoId": "string",
  "imageUrl": "string (Firebase Storage URL)",
  "thumbnailUrl": "string (Firebase Storage URL)",
  "createdAt": "timestamp",
  "userId": "string (reference to users)",
  "culturalObjectId": "string (reference to cultural_objects)",
  "reflection": "string (user's story/reflection)",
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

#### **6. moderation_queue**
```json
{
  "queueId": "string",
  "culturalObjectId": "string (reference to cultural_objects)",
  "submittedAt": "timestamp",
  "assignedTo": "string (reference to users - moderator)",
  "priority": "LOW|MEDIUM|HIGH",
  "status": "PENDING|IN_REVIEW|APPROVED|REJECTED",
  "reviewNotes": "string"
}
```

## 🔐 **Firebase Storage Structure**

```
/disrupton-2025/
├── cultural-objects/
│   ├── {objectId}/
│   │   ├── model.{format}          # Modelo 3D generado
│   │   ├── thumbnail.jpg           # Imagen miniatura
│   │   └── original-images/        # Imágenes originales
│   │       ├── image1.jpg
│   │       ├── image2.jpg
│   │       └── ...
├── ar-photos/
│   ├── {photoId}/
│   │   ├── photo.jpg               # Foto con RA
│   │   └── thumbnail.jpg           # Miniatura
└── user-profiles/
    ├── {userId}/
    │   └── profile.jpg             # Foto de perfil
```

## 🔥 **Firebase Services Utilizados**

### **1. Firestore Database**
- **Colecciones principales**: users, cultural_objects, comments, reactions
- **Reglas de seguridad**: Basadas en roles y propiedad
- **Índices**: Para búsquedas por región, tipo cultural, estado

### **2. Firebase Storage**
- **Almacenamiento**: Modelos 3D, imágenes, fotos RA
- **Reglas de acceso**: Basadas en autenticación y roles
- **Optimización**: Compresión automática de imágenes

### **3. Firebase Authentication**
- **Métodos**: Email/password, Google, Facebook
- **Roles**: Estudiante, Moderador, Administrador
- **Verificación**: Email para estudiantes UTEC

### **4. Firebase Cloud Functions**
- **Procesamiento**: Integración con KIRI Engine API
- **Notificaciones**: Alertas de moderación
- **Análisis**: Generación de estadísticas

### **5. Firebase Cloud Messaging**
- **Notificaciones push**: Nuevos objetos, comentarios, moderación
- **Temas**: Por región, tipo cultural, rol

## 🛡️ **Reglas de Seguridad**

### **Firestore Rules**
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
  }
}
```

### **Storage Rules**
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

## 📱 **Integración con Aplicación Móvil**

### **Flutter/Dart**
```dart
// Ejemplo de estructura de datos
class CulturalObject {
  final String id;
  final String name;
  final String description;
  final String region;
  final String culturalType;
  final String status;
  final String modelUrl;
  final String thumbnailUrl;
  final DateTime createdAt;
  final String contributorId;
  
  // Constructor y métodos...
}
```

### **React Native/JavaScript**
```javascript
// Ejemplo de estructura de datos
const culturalObject = {
  id: 'string',
  name: 'string',
  description: 'string',
  region: 'string',
  culturalType: 'ARTESANIA',
  status: 'APPROVED',
  modelUrl: 'string',
  thumbnailUrl: 'string',
  createdAt: new Date(),
  contributorId: 'string'
};
```

## 💰 **Estimación de Costos**

### **Fase 1 (100 estudiantes)**
- **Firestore**: Gratis (1GB storage, 50K reads/day)
- **Storage**: Gratis (5GB)
- **Functions**: Gratis (125K invocations/month)
- **Total**: $0/mes

### **Fase 2 (1,000 estudiantes)**
- **Firestore**: ~$25/mes
- **Storage**: ~$10/mes
- **Functions**: ~$15/mes
- **Total**: ~$50/mes

### **Fase 3 (10,000+ estudiantes)**
- **Firestore**: ~$200/mes
- **Storage**: ~$100/mes
- **Functions**: ~$150/mes
- **Total**: ~$450/mes

## 🚀 **Próximos Pasos**

1. **Configurar proyecto Firebase**
2. **Implementar autenticación**
3. **Crear estructura de Firestore**
4. **Configurar reglas de seguridad**
5. **Integrar con KIRI Engine API**
6. **Desarrollar aplicación móvil**

¿Te parece bien esta arquitectura? ¿Quieres que empecemos configurando el proyecto Firebase? 