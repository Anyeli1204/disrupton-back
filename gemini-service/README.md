# 🤖 Gemini Avatar Service

Microservicio en Python para manejar conversaciones con avatares usando la API de Google Gemini.

## 🚀 Características

- **Integración nativa con Gemini API** usando el SDK oficial de Python
- **Prompts personalizados** para cada tipo de avatar (Vicuña, Perro Peruano, Gallito de las Rocas)
- **Respuestas dinámicas** generadas por IA en lugar de respuestas fijas
- **API RESTful** con endpoints para chat, streaming, y utilidades
- **CORS habilitado** para integración con frontend
- **Logging detallado** para debugging
- **Health checks** para monitoreo

## 📋 Requisitos

- Python 3.8+
- API Key de Google Gemini

## 🛠️ Instalación

1. **Clonar o navegar al directorio del microservicio:**
```bash
cd gemini-service
```

2. **Instalar dependencias:**
```bash
pip install -r requirements.txt
```

3. **Configurar variables de entorno:**
```bash
# Copiar el archivo de ejemplo
cp env.example .env

# Editar .env con tu API key de Gemini
GEMINI_API_KEY=tu_api_key_aqui
```

4. **Obtener API Key de Gemini:**
   - Ve a [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Crea una nueva API key
   - Copia la key al archivo `.env`

## 🚀 Ejecución

### Desarrollo
```bash
python run.py
```

### Producción
```bash
gunicorn -w 4 -b 0.0.0.0:5001 app:app
```

## 📡 Endpoints

### Health Check
```http
GET /health
```

### Chat con Avatar
```http
POST /chat
Content-Type: application/json

{
  "avatarType": "VICUNA",
  "message": "Hola, ¿cómo estás?"
}
```

### Chat con Streaming
```http
POST /chat/stream
Content-Type: application/json

{
  "avatarType": "PERUVIAN_DOG",
  "message": "Cuéntame sobre la historia del Perú"
}
```

### Listar Modelos
```http
GET /models
```

### Contar Tokens
```http
POST /tokens/count
Content-Type: application/json

{
  "text": "Hola, ¿cómo estás?"
}
```

## 🎭 Tipos de Avatar

### VICUNA
- **Personalidad:** Elegante, sabia, conectada con la naturaleza andina
- **Conocimiento:** Cultura andina, fauna peruana, tradiciones

### PERUVIAN_DOG
- **Personalidad:** Leal, sabio, guardián de tradiciones ancestrales
- **Conocimiento:** Civilizaciones preincaicas, historia antigua del Perú

### COCK_OF_THE_ROCK
- **Personalidad:** Hermoso, orgulloso, representante de la biodiversidad peruana
- **Conocimiento:** Fauna peruana, naturaleza, biodiversidad

## 📝 Ejemplo de Uso

### Con curl
```bash
curl -X POST http://localhost:5001/chat \
  -H "Content-Type: application/json" \
  -d '{
    "avatarType": "VICUNA",
    "message": "Hola, ¿qué sabes sobre Machu Picchu?"
  }'
```

### Con Python
```python
import requests

response = requests.post('http://localhost:5001/chat', json={
    'avatarType': 'VICUNA',
    'message': 'Hola, ¿qué sabes sobre Machu Picchu?'
})

print(response.json())
```

## 🔧 Configuración

### Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `PORT` | Puerto del servidor | `5001` |
| `FLASK_ENV` | Entorno de Flask | `development` |
| `GEMINI_API_KEY` | API Key de Gemini | Requerido |
| `LOG_LEVEL` | Nivel de logging | `INFO` |

### Personalización de Prompts

Los prompts se pueden personalizar editando la función `build_avatar_prompt()` en `app.py`. Cada avatar tiene:

- **Contexto:** Descripción del personaje y su rol
- **Personalidad:** Características específicas del avatar
- **Instrucciones:** Comportamiento esperado en las respuestas

## 🐛 Troubleshooting

### Error: "API Key no configurada"
- Verifica que `GEMINI_API_KEY` esté en el archivo `.env`
- Asegúrate de que la API key sea válida

### Error: "No se pudo generar una respuesta"
- Verifica la conectividad a internet
- Revisa los logs para más detalles
- Verifica que la API key tenga permisos suficientes

### Error: "Puerto ya en uso"
- Cambia el puerto en la variable `PORT`
- O detén el proceso que esté usando el puerto

## 📊 Monitoreo

### Health Check
```bash
curl http://localhost:5001/health
```

### Logs
Los logs se muestran en la consola con el formato:
```
INFO:app:Procesando mensaje para avatar VICUNA: Hola
INFO:app:Respuesta de Gemini para VICUNA: ¡Hola! Como una vicuña...
```

## 🔄 Integración con Spring Boot

Para integrar este microservicio con tu aplicación Spring Boot, modifica el `GeminiAvatarService` para hacer llamadas HTTP a este microservicio en lugar de usar la integración directa.

## 📄 Licencia

MIT License - ver archivo LICENSE para más detalles.
