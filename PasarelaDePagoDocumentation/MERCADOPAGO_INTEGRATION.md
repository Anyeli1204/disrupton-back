# Integración MercadoPago - Disrupton Backend

## Resumen

Se ha implementado una pasarela de pago completa con MercadoPago para manejar todos los tipos de pago del sistema Disrupton:

### Tipos de Usuario y Pagos Soportados

#### Usuario Regular (USER)
- **Acceso por defecto**: Modelos unimodales, chat con IA Minki, 1 objeto 3D por día, ver 10 objetos 3D por día
- **Puede pagar por**:
  - Suscripción Premium (S/10): PREMIUM role
  - Suscripción Premium Max (S/30): PREMIUM_MAX role

#### Usuario Premium (PREMIUM - S/10)
- **Beneficios**: Modelos multimodales 50 requests/día, subir 5 objetos 3D/día, ver 20 objetos 3D/día
- **Duración**: 30 días

#### Usuario Premium Max (PREMIUM_MAX - S/30)
- **Beneficios**: Modelos multimodales ilimitados, subir 10 objetos 3D/día, ver 50 objetos 3D/día
- **Duración**: 30 días

#### Artesano (ARTISAN)
- **Límite gratuito**: 5 fotos de productos mensuales
- **Puede pagar por**:
  - Productos adicionales (S/1 cada uno)
  - Destacado en tienda (S/5 por 7 días)

#### Guía Turístico (GUIDE)
- **Límite gratuito**: 5 servicios mensuales
- **Puede pagar por**:
  - Servicios adicionales (S/1 cada uno)
  - Destacado en eventos (S/5 por 7 días)

## Configuración

### 1. Variables de Entorno

Crear un archivo `.env` o configurar las siguientes variables:

```bash
# MercadoPago Configuration
MERCADOPAGO_ACCESS_TOKEN=TEST-xxxxx-xxxxxx-xxxxxx-xxxxxxx  # Access Token de MercadoPago
MERCADOPAGO_PUBLIC_KEY=TEST-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxx  # Public Key
MERCADOPAGO_CLIENT_ID=xxxxxxxxxxxxxxxxx  # Client ID
MERCADOPAGO_CLIENT_SECRET=xxxxxxxxxxxxxxxxx  # Client Secret
MERCADOPAGO_WEBHOOK_SECRET=  # Opcional: secreto para webhooks
MERCADOPAGO_ENVIRONMENT=sandbox  # sandbox o production
```

### 2. Obtener Credenciales de MercadoPago

1. Registrarse en [MercadoPago Developers](https://www.mercadopago.com.pe/developers)
2. Crear una aplicación
3. Obtener las credenciales de TEST (sandbox) y PRODUCTION

### 3. Configurar Webhook

En el panel de MercadoPago, configurar el webhook URL:
```
https://tu-dominio.com/api/payments/webhook/mercadopago
```

**Eventos a suscribirse:**
- `payment`

## Endpoints Implementados

### Pagos de Suscripción

#### Suscripción Premium (S/10)
```http
POST /api/payments/subscription/premium
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "successUrl": "https://tu-app.com/success",
  "failureUrl": "https://tu-app.com/failure",
  "pendingUrl": "https://tu-app.com/pending"
}
```

#### Suscripción Premium Max (S/30)
```http
POST /api/payments/subscription/premium-max
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "successUrl": "https://tu-app.com/success",
  "failureUrl": "https://tu-app.com/failure",
  "pendingUrl": "https://tu-app.com/pending"
}
```

### Pagos para Artesanos

#### Producto Adicional (S/1)
```http
POST /api/payments/artisan/product-additional
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "successUrl": "https://tu-app.com/success",
  "failureUrl": "https://tu-app.com/failure",
  "pendingUrl": "https://tu-app.com/pending"
}
```

### Pagos para Guías

#### Servicio Adicional (S/1)
```http
POST /api/payments/guide/service-additional
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "successUrl": "https://tu-app.com/success",
  "failureUrl": "https://tu-app.com/failure",
  "pendingUrl": "https://tu-app.com/pending"
}
```

### Destacado en Tienda (S/5 por 7 días)

```http
POST /api/payments/featured-placement
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "itemId": "producto_o_servicio_id",
  "successUrl": "https://tu-app.com/success",
  "failureUrl": "https://tu-app.com/failure",
  "pendingUrl": "https://tu-app.com/pending"
}
```

### Consultar Pagos

#### Historial del Usuario
```http
GET /api/payments/history
Authorization: Bearer {jwt_token}
```

#### Detalles de un Pago
```http
GET /api/payments/{paymentId}
Authorization: Bearer {jwt_token}
```

## Flujo de Pago

1. **Usuario solicita pago** → Endpoint correspondiente
2. **Sistema valida** → Usuario, rol, monto, lógica de negocio
3. **Crea preferencia** → MercadoPago SDK
4. **Retorna URL** → Frontend redirige a MercadoPago
5. **Usuario paga** → En MercadoPago
6. **Webhook procesa** → Actualiza estado y beneficios
7. **Usuario redirigido** → success/failure/pending URL

## Respuestas de API

### Respuesta Exitosa
```json
{
  "paymentId": "uuid",
  "preferenceId": "mp_preference_id",
  "paymentType": "PREMIUM_SUBSCRIPTION",
  "status": "PENDING",
  "amount": 10.00,
  "currency": "PEN",
  "initPoint": "https://www.mercadopago.com/mpe/checkout/start?pref_id=xxx",
  "sandboxInitPoint": "https://sandbox.mercadopago.com/mpe/checkout/start?pref_id=xxx",
  "success": true,
  "message": "Pago creado exitosamente"
}
```

### Respuesta de Error
```json
{
  "success": false,
  "message": "El usuario ya tiene una suscripción activa"
}
```

## Validaciones Implementadas

### Suscripciones Premium
- ✅ Usuario no tiene suscripción activa
- ✅ Monto correcto (S/10 o S/30)
- ✅ Solo usuarios USER pueden suscribirse

### Productos/Servicios Adicionales
- ✅ Usuario tiene rol correcto (ARTISAN/GUIDE)
- ✅ Monto correcto (S/1)

### Destacado en Tienda
- ✅ Usuario es ARTISAN o GUIDE
- ✅ Monto correcto (S/5)
- ✅ Duración fija (7 días)

## Gestión Automática de Suscripciones

### Métodos en UserService

#### Verificar Premium Expirado
```java
userService.checkAndUpdateExpiredPremiumUsers();
```

#### Usuarios Próximos a Expirar
```java
List<UserDto> expiring = userService.getUsersWithExpiringPremium(3); // 3 días antes
```

## Seguridad

- ✅ Autenticación JWT requerida
- ✅ Validación de roles por endpoint
- ✅ Validación de ownership en consultas
- ✅ Validación de montos server-side
- ✅ External reference único por pago

## Estados de Pago

| Estado | Descripción |
|--------|-------------|
| `PENDING` | Pago creado, esperando confirmación |
| `APPROVED` | Pago aprobado y procesado |
| `REJECTED` | Pago rechazado |
| `CANCELLED` | Pago cancelado |
| `IN_PROCESS` | Pago en proceso |

## Troubleshooting

### Error: "Access token inválido"
- Verificar `MERCADOPAGO_ACCESS_TOKEN` en variables de entorno
- Confirmar que es el token correcto para el environment (sandbox/production)

### Error: "Usuario ya tiene suscripción activa"
- Verificar estado premium del usuario: `/api/payments/history`
- Revisar fecha de expiración en base de datos

### Webhook no se ejecuta
- Verificar URL del webhook en panel MercadoPago
- Confirmar que el endpoint `/api/payments/webhook/mercadopago` es accesible públicamente
- Revisar logs del servidor

### Pago queda en PENDING
- Típico en sandbox para probar diferentes escenarios
- En producción, verificar método de pago del usuario

## Testing

Para probar en sandbox:
1. Usar credenciales TEST
2. Utilizar [tarjetas de prueba de MercadoPago](https://www.mercadopago.com.pe/developers/es/docs/testing/test-cards)
3. Probar diferentes escenarios (aprobado, rechazado, pendiente)

## Integración con Frontend

El frontend debe:

1. **Llamar endpoint de pago** con URLs de callback
2. **Redirigir a initPoint** retornado por la API
3. **Manejar callbacks** en success/failure/pending URLs
4. **Actualizar UI** según estado del usuario

### Ejemplo con JavaScript

```javascript
async function createPremiumSubscription() {
  const response = await fetch('/api/payments/subscription/premium', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      successUrl: `${window.location.origin}/payment/success`,
      failureUrl: `${window.location.origin}/payment/failure`,
      pendingUrl: `${window.location.origin}/payment/pending`
    })
  });

  const paymentData = await response.json();

  if (paymentData.success) {
    window.location.href = paymentData.initPoint;
  }
}
```

## Utilidad de Collaborator

El sistema actual de `Collaborator` se mantiene para:

- ✅ **Acceso a redes de contacto** de GUIDE y ARTISAN (S/10)
- ✅ **Compatible con nuevo sistema de pagos**
- ✅ **Lógica existente preservada**

El nuevo sistema MercadoPago **complementa** al sistema Collaborator existente, no lo reemplaza.

## Soporte

Para dudas técnicas sobre la integración:
- Revisar logs en `/logs`
- Consultar [documentación oficial de MercadoPago](https://www.mercadopago.com.pe/developers)
- Verificar configuración en `application.yml`