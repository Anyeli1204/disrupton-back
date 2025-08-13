#!/usr/bin/env python3
"""
Script para ejecutar el microservicio de Gemini Avatar Service
"""

import os
import sys
from dotenv import load_dotenv

# Cargar variables de entorno
load_dotenv()

# Agregar el directorio actual al path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app import app

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5001))
    debug = os.environ.get('FLASK_ENV') == 'development'
    
    print(f"🚀 Iniciando Gemini Avatar Service en puerto {port}")
    print(f"🔧 Modo debug: {debug}")
    print(f"🔑 API Key configurada: {'Sí' if os.getenv('GEMINI_API_KEY') else 'No'}")
    print(f"📡 Servicio disponible en: http://localhost:{port}")
    print(f"🏥 Health check: http://localhost:{port}/health")
    print(f"💬 Chat endpoint: http://localhost:{port}/chat")
    print("=" * 60)
    
    app.run(host='0.0.0.0', port=port, debug=debug)
