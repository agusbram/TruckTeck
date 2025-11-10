# 🚛 TruckTeck - Sistema TMS (Balanza)

## 📍 Resumen Rápido

El **TMS (Terminal Manager System)** es el sistema de balanza que registra los pesajes de los camiones durante el proceso de carga.

## 🎯 Endpoints Implementados

### Base URL: `/api/v1/tms`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/weighing/initial` | Registra pesaje inicial (tara) |
| POST | `/weighing/final` | Registra pesaje final y conciliación |

## 🔄 Flujo de Estados

```
PENDING → TARA_REGISTERED → LOADING → FINALIZED
   ↑           ↑                ↑          ↑
  SAP         TMS            Sistema    TMS
                             de Carga
```

## 📁 Estructura de Archivos

```
src/main/java/ar/edu/iua/TruckTeck/
│
├── integration/tms/
│   ├── controllers/
│   │   └── TmsRestController.java          ← API REST para balanza
│   └── model/business/
│       ├── IOrderTmsBusiness.java          ← Interfaz
│       └── OrderTmsBusiness.java           ← Lógica de negocio
│
├── model/
│   ├── Order.java                          ← Entidad principal
│   ├── OrderStatusLog.java                 ← Log de auditoría
│   ├── enums/
│   │   └── OrderState.java                 ← Estados
│   └── persistence/
│       └── OrderRepository.java            ← Queries personalizadas
│
└── controllers/
    └── Constants.java                      ← URL_TMS definida
```

## 🧪 Prueba Rápida con cURL

### 1. Pesaje Inicial
```bash
curl -X POST http://localhost:8080/api/v1/tms/weighing/initial \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "ABC123",
    "weight": 8500.50
  }'
```

**Respuesta:**
```json
{
  "code": 200,
  "message": "Operación exitosa",
  "data": {
    "orderNumber": 1,
    "activationCode": "47892",
    "truckDomain": "ABC123",
    "initialWeight": 8500.50,
    "state": "TARA_REGISTERED",
    "timestamp": "2025-10-25T14:30:00"
  }
}
```

### 2. Pesaje Final
```bash
curl -X POST http://localhost:8080/api/v1/tms/weighing/final \
  -H "Content-Type: application/json" \
  -d '{
    "activationCode": "47892",
    "weight": 18750.25
  }'
```

**Respuesta:**
```json
{
  "code": 200,
  "message": "Operación exitosa",
  "data": {
    "orderNumber": 1,
    "activationCode": "47892",
    "state": "FINALIZED",
    "timestamp": "2025-10-25T15:45:00",
    "reconciliation": {
      "initialWeight": 8500.50,
      "finalWeight": 18750.25,
      "loadedProduct": 10200.00,
      "scaleNet": 10249.75,
      "difference": 49.75,
      "averageTemperature": -42.5,
      "averageDensity": 0.58,
      "averageFlow": 15000.0
    }
  }
}
```

## 🔑 Características Clave

✅ **Generación automática de código de activación** (5 dígitos)  
✅ **Validación de estados** antes de cada operación  
✅ **Log de auditoría** automático en `OrderStatusLog`  
✅ **Cálculo de conciliación** balanza vs caudalímetro  
✅ **Validaciones con Bean Validation** (@Valid, @NotBlank, @Positive)  
✅ **Manejo robusto de excepciones** con respuestas estándar  
✅ **Logging detallado** con SLF4J

## 🔍 Métodos de Repositorio Agregados

```java
// OrderRepository.java

// Busca orden por dominio de camión y estado
Optional<Order> findByTruckAndState(Truck truck, OrderState state);

// Busca orden por código de activación
Optional<Order> findByActivationCode(String activationCode);
```

## ⚠️ Validaciones Implementadas

### Pesaje Inicial
- ✅ Camión debe existir en la BD
- ✅ Debe haber una orden PENDING para ese camión
- ✅ Peso debe ser > 0
- ✅ Código de activación debe ser único

### Pesaje Final
- ✅ Código de activación debe existir
- ✅ Orden debe estar en estado LOADING
- ✅ Orden debe tener pesaje inicial registrado
- ✅ Peso debe ser > 0
<!-- 
## 🎓 Para el Parcial (11/11/2025)

- [x] Implementación completa de TMS
- [x] Endpoints REST funcionales
- [x] Validaciones y manejo de errores
- [x] Log de auditoría
- [ ] Documentación OpenAPI/Swagger
- [ ] Colección Postman con tests

## 🚀 Para el Final

- [ ] Agregar seguridad con roles (ROLE_TMS)
- [ ] Implementar sistema SAP (crear órdenes)
- [ ] Implementar sistema de carga (datos en tiempo real)
- [ ] Agregar endpoint de conciliación independiente
- [ ] Frontend para monitoreo -->

## 📚 Documentación Completa

Ver: [`docs/TMS_INTEGRATION_GUIDE.md`](./docs/TMS_INTEGRATION_GUIDE.md)

## 🐛 Debugging

### Ver logs de TMS:
```bash
tail -f logs/truckteck.log | grep "TMS:"
```

### Ver cambios de estado:
```sql
SELECT * FROM order_status_log 
WHERE actor = 'TMS' 
ORDER BY timestamp DESC;
```

### Verificar código de activación:
```sql
SELECT number, activation_code, state, initial_weight, final_weight 
FROM orders 
WHERE activation_code = '47892';
```
---

**Proyecto:** TruckTeck - Ingeniería Web 3 (2025)  
**Módulo:** Integración TMS (Balanza)  
**Estado:** ✅ Implementado y Funcional
