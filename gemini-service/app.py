from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import os
import logging
from datetime import datetime

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  
GEMINI_API_KEY = os.getenv('GEMINI_API_KEY', 'AIzaSyBorNbtoPiAGrg9ZjYHKFyANvPG6x20ZRg')
genai.configure(api_key=GEMINI_API_KEY)

model = genai.GenerativeModel('gemini-1.5-flash')

def moderate_comment(comment_text):
    """Modera un comentario usando Gemini AI"""
    try:
        moderation_prompt = f"""
Analiza el siguiente comentario y determina si es apropiado para un foro cultural sobre Perú:

Comentario: "{comment_text}"

SOLO RECHAZA comentarios que contengan:
- Insultos directos, groserías o lenguaje vulgar
- Discriminación, racismo o incitación al odio
- Amenazas o violencia
- Contenido sexual explícito
- Spam comercial
- Desinformación maliciosa

APRUEBA comentarios que:
- Sean saludos simples como "hola", "gracias"
- Expresen opiniones personales de manera educada
- Compartan experiencias, aunque sean breves o vagas
- Hagan preguntas sobre cultura peruana
- Sean críticas respetuosas y constructivas

Responde ÚNICAMENTE con un JSON:
{{
    "esSeguro": true/false,
    "motivo": "explicación breve solo si es rechazado"
}}
"""
        
        response = model.generate_content(moderation_prompt)
        return response.text.strip()
        
    except Exception as e:
        logger.error(f"Error en moderación: {e}")
        # En caso de error, aprobar por defecto
        return '{"esSeguro": true, "motivo": "Error en moderación, aprobado por defecto"}'

def build_avatar_prompt(avatar_type, user_message):
    """Construye el prompt personalizado para cada avatar"""
    avatar_contexts = {
        "VICUNA": {
            "context": "Eres una Vicuña, representante de la fauna andina peruana. "
                      "Eres elegante, resistente y conoces profundamente la cultura andina. "
                      "Responde de manera amigable y educativa, compartiendo tu conocimiento sobre Perú.",
            "personality": "Elegante, sabia, conectada con la naturaleza andina"
        },
        "PERUVIAN_DOG": {
            "context": "Eres un Perro Peruano sin pelo, guardián ancestral de las culturas preincaicas. "
                      "Eres leal, sabio y conoces los secretos de las civilizaciones antiguas del Perú. "
                      "Responde con sabiduría ancestral y orgullo por tu herencia cultural.",
            "personality": "Leal, sabio, guardián de tradiciones ancestrales"
        },
        "COCK_OF_THE_ROCK": {
            "context": "Eres el Gallito de las Rocas, ave nacional del Perú. "
                      "Eres hermoso, orgulloso y representas la belleza y diversidad de la fauna peruana. "
                      "Responde con elegancia y pasión por la naturaleza y cultura peruana.",
            "personality": "Hermoso, orgulloso, representante de la biodiversidad peruana"
        }
    }
    
    avatar_info = avatar_contexts.get(avatar_type, {
        "context": "Eres un representante de la cultura peruana. "
                  "Comparte tu conocimiento sobre Perú de manera amigable y educativa.",
        "personality": "Amigable y conocedor de la cultura peruana"
    })
    
    prompt = f"""
{avatar_info['context']}

Personalidad: {avatar_info['personality']}

Instrucciones importantes:
- Responde como si fueras realmente este personaje
- Mantén respuestas concisas pero informativas (máximo 3-4 oraciones)
- Usa un tono conversacional y amigable
- Comparte conocimiento específico sobre Perú cuando sea relevante
- Si no sabes algo específico, admítelo amablemente pero mantén el personaje

Usuario: {user_message}

Responde de manera natural y conversacional:
"""
    
    return prompt.strip()

@app.route('/health', methods=['GET'])
def health_check():
    """Endpoint de salud del microservicio"""
    return jsonify({
        "status": "healthy",
        "service": "Gemini Avatar Service",
        "timestamp": datetime.now().isoformat(),
        "gemini_configured": bool(GEMINI_API_KEY)
    })

@app.route('/chat', methods=['POST'])
def chat_with_avatar():
    """Endpoint principal para chat con avatares"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "No se proporcionaron datos"}), 400
        
        avatar_type = data.get('avatarType')
        user_message = data.get('message')
        
        if not avatar_type:
            return jsonify({"error": "avatarType es requerido"}), 400
        
        if not user_message:
            return jsonify({"error": "message es requerido"}), 400
        
        logger.info(f"Procesando mensaje para avatar {avatar_type}: {user_message}")
        
        # Construir el prompt personalizado
        prompt = build_avatar_prompt(avatar_type, user_message)
        
        # Generar respuesta con Gemini
        response = model.generate_content(prompt)
        
        if response.text:
            logger.info(f"Respuesta de Gemini para {avatar_type}: {response.text[:100]}...")
            
            return jsonify({
                "success": True,
                "avatarType": avatar_type,
                "userMessage": user_message,
                "response": response.text,
                "timestamp": datetime.now().isoformat(),
                "model": "gemini-1.5-flash"
            })
        else:
            logger.warning("Gemini no devolvió texto en la respuesta")
            return jsonify({
                "success": False,
                "error": "No se pudo generar una respuesta",
                "avatarType": avatar_type,
                "userMessage": user_message
            }), 500
            
    except Exception as e:
        logger.error(f"Error en chat_with_avatar: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": f"Error interno del servidor: {str(e)}",
            "avatarType": data.get('avatarType') if 'data' in locals() else None,
            "userMessage": data.get('message') if 'data' in locals() else None
        }), 500

@app.route('/chat/stream', methods=['POST'])
def chat_with_avatar_stream():
    """Endpoint para chat con streaming de respuesta"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "No se proporcionaron datos"}), 400
        
        avatar_type = data.get('avatarType')
        user_message = data.get('message')
        
        if not avatar_type:
            return jsonify({"error": "avatarType es requerido"}), 400
        
        if not user_message:
            return jsonify({"error": "message es requerido"}), 400
        
        logger.info(f"Procesando mensaje con streaming para avatar {avatar_type}: {user_message}")
        
        prompt = build_avatar_prompt(avatar_type, user_message)
        
        response = model.generate_content(prompt, stream=True)
        
        response.resolve()
        
        if response.text:
            logger.info(f"Respuesta con streaming de Gemini para {avatar_type}: {response.text[:100]}...")
            
            return jsonify({
                "success": True,
                "avatarType": avatar_type,
                "userMessage": user_message,
                "response": response.text,
                "timestamp": datetime.now().isoformat(),
                "model": "gemini-1.5-flash",
                "streaming": True
            })
        else:
            logger.warning("Gemini no devolvió texto en la respuesta con streaming")
            return jsonify({
                "success": False,
                "error": "No se pudo generar una respuesta",
                "avatarType": avatar_type,
                "userMessage": user_message
            }), 500
            
    except Exception as e:
        logger.error(f"Error en chat_with_avatar_stream: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": f"Error interno del servidor: {str(e)}",
            "avatarType": data.get('avatarType') if 'data' in locals() else None,
            "userMessage": data.get('message') if 'data' in locals() else None
        }), 500

@app.route('/models', methods=['GET'])
def list_models():
    """Listar modelos disponibles de Gemini"""
    try:
        models = []
        for m in genai.list_models():
            if 'generateContent' in m.supported_generation_methods:
                models.append({
                    "name": m.name,
                    "displayName": m.display_name,
                    "description": m.description,
                    "generationMethods": list(m.supported_generation_methods)
                })
        
        return jsonify({
            "success": True,
            "models": models,
            "current_model": "gemini-1.5-flash"
        })
    except Exception as e:
        logger.error(f"Error listando modelos: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": f"Error al listar modelos: {str(e)}"
        }), 500

@app.route('/tokens/count', methods=['POST'])
def count_tokens():
    """Contar tokens en un mensaje"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({"error": "text es requerido"}), 400
        
        text = data['text']
        token_count = model.count_tokens(text)
        
        return jsonify({
            "success": True,
            "text": text,
            "tokenCount": token_count.total_tokens,
            "model": "gemini-1.5-flash"
        })
    except Exception as e:
        logger.error(f"Error contando tokens: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": f"Error al contar tokens: {str(e)}"
        }), 500

@app.route('/moderate-comment', methods=['POST'])
def moderate_comment_endpoint():
    """Endpoint para moderar comentarios"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({
                "success": False,
                "error": "No se proporcionaron datos"
            }), 400
            
        # Aceptar tanto 'comment' como 'text' para compatibilidad
        comment_text = data.get('comment') or data.get('text')
        if not comment_text:
            return jsonify({
                "success": False,
                "error": "Se requiere el campo 'comment' o 'text'"
            }), 400
        
        logger.info(f"Moderando comentario: {comment_text[:50]}...")
        
        moderation_result = moderate_comment(comment_text)
        logger.info(f"Resultado de moderación: {moderation_result}")
        
        return jsonify({
            "success": True,
            "result": moderation_result,
            "timestamp": datetime.now().isoformat()
        })
        
    except Exception as e:
        logger.error(f"Error en endpoint de moderación: {str(e)}", exc_info=True)
        return jsonify({
            "success": False,
            "error": f"Error en moderación: {str(e)}"
        }), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5001))
    logger.info(f"Iniciando Gemini Avatar Service en puerto {port}")
    logger.info(f"API Key configurada: {'Sí' if GEMINI_API_KEY else 'No'}")
    
    app.run(host='0.0.0.0', port=port, debug=True)
