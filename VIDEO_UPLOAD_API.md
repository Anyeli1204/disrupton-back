# KIRI Engine Video Upload API

## Descripción
Esta API permite subir videos a KIRI Engine para generar modelos 3D usando el algoritmo de Fotogrametría.

## Endpoint
```
POST /api/kiri-engine/upload-video
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
| `modelQuality` | Integer | 0 | Calidad del modelo (0: High, 1: Medium, 2: Low, 3: Ultra) |
| `textureQuality` | Integer | 0 | Calidad de textura (0: 4K, 1: 2K, 2: 1K, 3: 8K) |
| `fileFormat` | String | "OBJ" | Formato del archivo (OBJ, FBX, STL, PLY, GLB, GLTF, USDZ, XYZ) |
| `isMask` | Integer | 1 | Auto Object Masking (0: Off, 1: On) |
| `textureSmoothing` | Integer | 1 | Texture Smoothing (0: Off, 1: On) |

## Ejemplo de Uso

### cURL
```bash
curl --location --request POST 'http://localhost:8080/api/kiri-engine/upload-video' \
--header 'Content-Type: multipart/form-data' \
--form 'videoFile=@"/path/to/video.mp4"' \
--form 'modelQuality="1"' \
--form 'textureQuality="1"' \
--form 'fileFormat="OBJ"' \
--form 'isMask="1"' \
--form 'textureSmoothing="1"'
```

### JavaScript (Fetch)
```javascript
const formData = new FormData();
formData.append('videoFile', videoFile);
formData.append('modelQuality', '1');
formData.append('textureQuality', '1');
formData.append('fileFormat', 'OBJ');
formData.append('isMask', '1');
formData.append('textureSmoothing', '1');

fetch('/api/kiri-engine/upload-video', {
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

url = "http://localhost:8080/api/kiri-engine/upload-video"
files = {'videoFile': open('video.mp4', 'rb')}
data = {
    'modelQuality': '1',
    'textureQuality': '1',
    'fileFormat': 'OBJ',
    'isMask': '1',
    'textureSmoothing': '1'
}

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
    "calculateType": 1
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
| `data.calculateType` | Integer | Tipo de cálculo (1: Photo Scan) |
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
- `modelQuality`: Debe estar entre 0 y 3
- `textureQuality`: Debe estar entre 0 y 3
- `fileFormat`: Debe ser uno de los formatos soportados
- `isMask`: Debe ser 0 o 1
- `textureSmoothing`: Debe ser 0 o 1

## Notas Importantes

1. **Texture Smoothing**: Mejora la calidad de textura en áreas borrosas y produce mapas de textura más coherentes. Puede reducir la nitidez general de las texturas. Se recomienda desactivarlo para uso profesional que requiera escaneos fotográficos precisos.

2. **Auto Object Masking**: Automáticamente detecta y enmascara el objeto principal en el video.

3. **Tiempo de Procesamiento**: El tiempo de procesamiento depende de la duración del video y la calidad seleccionada.

4. **Límites de API**: Respeta los límites de la API de KIRI Engine para evitar errores de rate limiting. 