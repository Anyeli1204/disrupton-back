#!/usr/bin/env python3
"""
Script para probar el microservicio de Gemini Avatar Service
"""

import requests
import json
import time

# Configuración
BASE_URL = "http://localhost:5001"

def test_health():
    """Probar el endpoint de salud"""
    print("🏥 Probando health check...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Health check exitoso: {data}")
            return True
        else:
            print(f"❌ Health check falló: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error en health check: {e}")
        return False

def test_chat(avatar_type, message):
    """Probar el endpoint de chat"""
    print(f"\n💬 Probando chat con {avatar_type}...")
    print(f"📝 Mensaje: {message}")
    
    try:
        payload = {
            "avatarType": avatar_type,
            "message": message
        }
        
        response = requests.post(
            f"{BASE_URL}/chat",
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                print(f"✅ Chat exitoso:")
                print(f"   Avatar: {data['avatarType']}")
                print(f"   Mensaje: {data['userMessage']}")
                print(f"   Respuesta: {data['response']}")
                print(f"   Modelo: {data['model']}")
                return True
            else:
                print(f"❌ Chat falló: {data.get('error', 'Error desconocido')}")
                return False
        else:
            print(f"❌ Error HTTP: {response.status_code}")
            print(f"   Respuesta: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error en chat: {e}")
        return False

def test_models():
    """Probar el endpoint de modelos"""
    print("\n🤖 Probando listado de modelos...")
    try:
        response = requests.get(f"{BASE_URL}/models")
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                print(f"✅ Modelos disponibles:")
                for model in data.get("models", []):
                    print(f"   - {model['name']}: {model['displayName']}")
                return True
            else:
                print(f"❌ Error listando modelos: {data.get('error')}")
                return False
        else:
            print(f"❌ Error HTTP: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error listando modelos: {e}")
        return False

def test_token_count():
    """Probar el endpoint de conteo de tokens"""
    print("\n🔢 Probando conteo de tokens...")
    try:
        text = "Hola, ¿cómo estás? Cuéntame sobre la historia del Perú."
        payload = {"text": text}
        
        response = requests.post(
            f"{BASE_URL}/tokens/count",
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                print(f"✅ Conteo de tokens exitoso:")
                print(f"   Texto: {data['text']}")
                print(f"   Tokens: {data['tokenCount']}")
                return True
            else:
                print(f"❌ Error contando tokens: {data.get('error')}")
                return False
        else:
            print(f"❌ Error HTTP: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error contando tokens: {e}")
        return False

def main():
    """Función principal de pruebas"""
    print("🚀 Iniciando pruebas del microservicio Gemini Avatar Service")
    print("=" * 60)
    
    # Esperar un momento para que el servicio esté listo
    print("⏳ Esperando que el servicio esté listo...")
    time.sleep(2)
    
    # Ejecutar pruebas
    tests = [
        ("Health Check", test_health),
        ("Listado de Modelos", test_models),
        ("Conteo de Tokens", test_token_count),
        ("Chat con Vicuña", lambda: test_chat("VICUNA", "Hola, ¿cómo estás?")),
        ("Chat con Perro Peruano", lambda: test_chat("PERUVIAN_DOG", "Cuéntame sobre la historia del Perú")),
        ("Chat con Gallito de las Rocas", lambda: test_chat("COCK_OF_THE_ROCK", "¿Qué sabes sobre la biodiversidad peruana?")),
    ]
    
    results = []
    for test_name, test_func in tests:
        print(f"\n{'='*20} {test_name} {'='*20}")
        try:
            result = test_func()
            results.append((test_name, result))
        except Exception as e:
            print(f"❌ Error ejecutando {test_name}: {e}")
            results.append((test_name, False))
    
    # Resumen de resultados
    print(f"\n{'='*60}")
    print("📊 RESUMEN DE PRUEBAS")
    print("=" * 60)
    
    passed = 0
    total = len(results)
    
    for test_name, result in results:
        status = "✅ PASÓ" if result else "❌ FALLÓ"
        print(f"{test_name}: {status}")
        if result:
            passed += 1
    
    print(f"\n🎯 Resultado: {passed}/{total} pruebas pasaron")
    
    if passed == total:
        print("🎉 ¡Todas las pruebas pasaron! El microservicio está funcionando correctamente.")
    else:
        print("⚠️  Algunas pruebas fallaron. Revisa los logs del microservicio.")

if __name__ == "__main__":
    main()
