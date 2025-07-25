# KIRI Engine Featureless Video Upload API

## Descripción
Esta API permite subir videos a KIRI Engine para generar modelos 3D usando el algoritmo **Featureless Object Scan**. Este algoritmo está optimizado para objetos sin características distintivas (como objetos lisos, esféricos, o con superficies uniformes).

## Endpoint
```
POST /api/kiri-engine/upload-featureless-video
```

## Requisitos del Video
- **Resolución máxima**: 1920x1080
- **Duración máxima**: 3 minutos
- **Formatos soportados**: MP4, AVI, MOV, WMV, FLV, WEBM
- **Tamaño máximo**: 500MB

## Parámetros

### Parámetros de Archivo
| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `videoFile` | File | Sí | Archivo de video |

### Parámetros de Configuración
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `fileFormat` | String | "OBJ" | Formato del archivo (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ) |

## Diferencias con Photogrammetry

| Aspecto | Photogrammetry | Featureless Object Scan |
|---------|----------------|------------------------|
| **Algoritmo** | Basado en características de imagen | Optimizado para objetos sin características |
| **Mejor para** | Objetos con texturas y detalles | Objetos lisos, esféricos, uniformes |
| **Parámetros** | Múltiples opciones de calidad | Solo formato de salida |
| **calculateType** | 1 (Photo Scan) | 2 (Featureless Object Scan) |

## Ejemplo de Uso

### cURL
```bash
curl --location --request POST 'http://localhost:8080/api/kiri-engine/upload-featureless-video' \
--header 'Content-Type: multipart/form-data' \
--form 'videoFile=@"/path/to/video.mp4"' \
--form 'fileFormat="OBJ"'
```

### JavaScript (Fetch)
```javascript
const formData = new FormData();
formData.append('videoFile', videoFile);
formData.append('fileFormat', 'OBJ');

fetch('/api/kiri-engine/upload-featureless-video', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### Python (requests)
```python
import requests

url = "http://localhost:8080/api/kiri-engine/upload-featureless-video"
files = {'videoFile': open('video.mp4', 'rb')}
data = {'fileFormat': 'OBJ'}

response = requests.post(url, files=files, data=data)
print(response.json())
```

## Respuesta Exitosa

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "serialize": "796a6f52457844b4918db3eadd64becc",
    "calculateType": 2
  },
  "ok": true
}
```

### Campos de Respuesta
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `code` | Integer | Código de respuesta (0 = éxito) |
| `msg` | String | Mensaje de respuesta |
| `data.serialize` | String | Identificador único del modelo |
| `data.calculateType` | Integer | Tipo de cálculo (2: Featureless Object Scan) |
| `ok` | Boolean | Indica si la operación fue exitosa |

## Respuestas de Error

### Error de Validación (400)
```json
{
  "error": "Error de validación",
  "message": "El archivo de video es requerido"
}
```

### Error de Procesamiento (500)
```json
{
  "error": "Error al procesar archivo de video",
  "message": "Formato de video no soportado"
}
```

## Flujo de Trabajo

1. **Subir Video**: Usar este endpoint para subir el video
2. **Obtener Serial**: La respuesta incluye un `serialize` único
3. **Consultar Estado**: Usar `/api/kiri-engine/model-status/{serial}` para verificar el progreso
4. **Descargar Modelo**: Usar `/api/kiri-engine/download-model/{serial}` cuando esté listo

## Validaciones

### Validaciones de Archivo
- El archivo no puede estar vacío
- El formato debe ser uno de los soportados
- El tamaño no puede exceder 500MB

### Validaciones de Parámetros
- `fileFormat`: Debe ser uno de los formatos soportados

## Casos de Uso Ideales

### Objetos Perfectos para Featureless Object Scan:
- **Esferas** (pelotas, globos, objetos esféricos)
- **Cilindros** (latas, botellas, tubos)
- **Cubos** (dados, cajas simples)
- **Objetos lisos** (sin texturas o patrones)
- **Superficies uniformes** (paredes lisas, objetos monocromáticos)
- **Objetos reflectantes** (espejos, metales pulidos)

### Objetos Mejor para Photogrammetry:
- **Objetos con texturas** (muebles, ropa, alimentos)
- **Objetos con detalles** (esculturas, joyas, herramientas)
- **Objetos con patrones** (telas, papeles, objetos decorativos)
- **Objetos con características distintivas** (cualquier objeto con variaciones visuales)

## Comparación de Endpoints

| Endpoint | Algoritmo | Parámetros | Mejor para |
|----------|-----------|------------|------------|
| `/upload-images` | Photogrammetry | Completo | Objetos con características |
| `/upload-video` | Photogrammetry | Completo | Videos de objetos con características |
| `/upload-featureless-video` | Featureless Object Scan | Mínimo | Objetos sin características |

## Notas Importantes

1. **Algoritmo Especializado**: Featureless Object Scan está diseñado específicamente para objetos que no tienen características distintivas suficientes para fotogrametría tradicional.

2. **Menos Parámetros**: Solo requiere el formato de salida, ya que el algoritmo está optimizado para este tipo de objetos.

3. **Tiempo de Procesamiento**: Puede ser más rápido que la fotogrametría tradicional para objetos apropiados.

4. **Calidad**: Proporciona mejores resultados para objetos lisos que la fotogrametría estándar.

5. **Límites de API**: Respeta los límites de la API de KIRI Engine para evitar errores de rate limiting.

## Ejemplos de Objetos por Tipo

### ✅ Perfectos para Featureless:
- Pelota de tenis
- Botella de agua
- Lata de refresco
- Esfera de cristal
- Cubo de Rubik (sin stickers)
- Cilindro de metal

### ❌ Mejor usar Photogrammetry:
- Mueble de madera
- Ropa con texturas
- Fruta con detalles
- Escultura con relieves
- Joya con gemas
- Herramienta con marcas 