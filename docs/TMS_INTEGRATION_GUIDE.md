# 🏭 Guía de Integración TMS (Terminal Manager System - Sistema de Balanza)

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Arquitectura General](#arquitectura-general)
3. [Componentes Implementados](#componentes-implementados)
4. [Flujo de Trabajo](#flujo-de-trabajo)
5. [API Endpoints](#api-endpoints)
6. [Modelos de Datos](#modelos-de-datos)
7. [Ejemplos de Uso](#ejemplos-de-uso)
8. [Seguridad y Roles](#seguridad-y-roles)
9. [Próximos Pasos](#próximos-pasos)

---

## 📖 Introducción

El **TMS (Terminal Manager System)** es un sistema externo de balanza que se integra con TruckTeck para registrar los pesajes de los camiones durante el proceso de carga de gas líquido.

### Responsabilidades del TMS

El sistema de balanza TMS tiene dos responsabilidades principales en el ciclo de vida de una orden:

1. **Registrar el Pesaje Inicial (Tara)** - Punto 2 del proceso
   - Peso del camión vacío antes de la carga
   - Generación del código de activación

2. **Registrar el Pesaje Final** - Punto 5 del proceso
   - Peso del camión cargado después de finalizar la carga
   - Cálculo de la conciliación balanza vs caudalímetro

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                     Sistema TruckTeck                        │
│                                                               │
│  ┌─────────────┐      ┌──────────────────┐                 │
│  │  SAP (ERP)  │──────▶│  OrderBusiness   │◀─────────┐     │
│  └─────────────┘      └──────────────────┘          │     │
│                              │                        │     │
│                              ▼                        │     │
│                       ┌─────────────┐                │     │
│                       │    Order    │                │     │
│                       │  (Entidad)  │                │     │
│                       └─────────────┘                │     │
│                              ▲                        │     │
│                              │                        │     │
│  ┌─────────────┐      ┌──────────────────┐          │     │
│  │ TMS Balanza │──────▶│ OrderTmsBusiness │──────────┘     │
│  └─────────────┘      └──────────────────┘                 │
│       (Externa)              │                               │
│                              │                               │
│                       ┌──────────────────┐                  │
│                       │ TmsRestController│                  │
│                       └──────────────────┘                  │
│                              ▲                               │
└──────────────────────────────┼───────────────────────────────┘
                               │
                        HTTP REST API
                               │
                    ┌──────────▼──────────┐
                    │ Sistema de Balanza  │
                    │        TMS          │
                    │     (Hardware)      │
                    └─────────────────────┘
```

### Flujo de Estados de la Orden

```
PENDING (1)
   │
   │ TMS registra pesaje inicial
   ▼
TARA_REGISTERED (2)
   │
   │ Sistema de carga procesa
   ▼
LOADING (3)
   │
   │ TMS registra pesaje final
   ▼
FINALIZED (4)
```

---

## 🧩 Componentes Implementados

### 1. **Capa de Persistencia**

#### `OrderRepository`
Ubicación: `src/main/java/ar/edu/iua/TruckTeck/model/persistence/OrderRepository.java`

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Busca orden por dominio de camión y estado (para pesaje inicial)
    Optional<Order> findByTruckAndState(Truck truck, OrderState state);
    
    // Busca orden por código de activación (para pesaje final)
    Optional<Order> findByActivationCode(String activationCode);
}
```

**¿Por qué estos métodos?**
- `findByTruckAndState`: Cuando llega el camión a la balanza, solo conocemos su dominio. Buscamos la orden PENDING para ese camión.
- `findByActivationCode`: Para el pesaje final, el operador ingresa el código de activación generado en el pesaje inicial.

---

### 2. **Capa de Negocio**

#### `IOrderTmsBusiness` (Interfaz)
Ubicación: `src/main/java/ar/edu/iua/TruckTeck/integration/tms/model/business/IOrderTmsBusiness.java`

Define el contrato para las operaciones TMS:

```java
public interface IOrderTmsBusiness {
    Order registerInitialWeighing(String domain, Double initialWeight) 
        throws BusinessException, NotFoundException, FoundException;
    
    Order registerFinalWeighing(String activationCode, Double finalWeight) 
        throws BusinessException, NotFoundException, FoundException;
}
```

#### `OrderTmsBusiness` (Implementación)
Ubicación: `src/main/java/ar/edu/iua/TruckTeck/integration/tms/model/business/OrderTmsBusiness.java`

Esta clase contiene toda la lógica de negocio para los pesajes:

**Método: `registerInitialWeighing`**

```java
@Override
public Order registerInitialWeighing(String domain, Double initialWeight) {
    // 1. Buscar camión por dominio
    Truck truck = truckRepository.findByDomain(domain)
        .orElseThrow(() -> new NotFoundException("Camión no encontrado: " + domain));
    
    // 2. Buscar orden PENDING para ese camión
    Order order = orderRepository.findByTruckAndState(truck, OrderState.PENDING)
        .orElseThrow(() -> new NotFoundException("No hay orden pendiente para: " + domain));
    
    // 3. Validar estado
    if (order.getState() != OrderState.PENDING) {
        throw new BusinessException("Orden no está en estado PENDING");
    }
    
    // 4. Generar código de activación (5 dígitos)
    String activationCode = generateActivationCode(); // ej: "12345"
    
    // 5. Registrar datos
    order.setInitialWeight(initialWeight);
    order.setActivationCode(activationCode);
    order.setInitialWeighing(LocalDateTime.now());
    order.setState(OrderState.TARA_REGISTERED);
    
    // 6. Guardar y registrar en log de auditoría
    Order savedOrder = orderRepository.save(order);
    logStateChange(savedOrder, PENDING, TARA_REGISTERED, "TMS", "Pesaje inicial");
    
    return savedOrder;
}
```

**Método: `registerFinalWeighing`**

```java
@Override
public Order registerFinalWeighing(String activationCode, Double finalWeight) {
    // 1. Buscar orden por código de activación
    Order order = orderRepository.findByActivationCode(activationCode)
        .orElseThrow(() -> new NotFoundException("Código inválido: " + activationCode));
    
    // 2. Validar que esté en estado LOADING (cerrada para carga)
    if (order.getState() != OrderState.LOADING) {
        throw new BusinessException("Orden debe estar en estado LOADING");
    }
    
    // 3. Registrar peso final
    order.setFinalWeight(finalWeight);
    order.setEndWeighing(LocalDateTime.now());
    order.setState(OrderState.FINALIZED);
    
    // 4. Guardar y registrar en log
    Order savedOrder = orderRepository.save(order);
    logStateChange(savedOrder, LOADING, FINALIZED, "TMS", "Pesaje final");
    
    return savedOrder;
}
```

**Características Clave:**
- ✅ Validación de estados antes de cada operación
- ✅ Generación automática de código de activación único
- ✅ Registro de auditoría en `OrderStatusLog`
- ✅ Manejo robusto de excepciones
- ✅ Logging detallado para trazabilidad

---

### 3. **Capa de Controladores (API REST)**

#### `TmsRestController`
Ubicación: `src/main/java/ar/edu/iua/TruckTeck/integration/tms/controllers/TmsRestController.java`

**URL Base:** `/api/v1/tms`

Este controlador expone dos endpoints REST para que el sistema de balanza externo pueda comunicarse con TruckTeck:

---

## 🌐 API Endpoints

### 1. Registrar Pesaje Inicial (Tara)

**Endpoint:** `POST /api/v1/tms/weighing/initial`

**Descripción:** 
Registra el peso del camión vacío cuando arriba a planta.

**Parámetros (Query Params):**

| Parámetro | Tipo | Obligatorio | Descripción |
|-----------|------|-------------|-------------|
| `domain` | String | Sí | Dominio (patente) del camión |
| `weight` | Double | Sí | Peso del camión vacío en kg (debe ser > 0) |

**Response (200 OK):**
```json
{
  "number": 12345,
  "activationCode": "47892",
  "truck": {
    "domain": "ABC123",
    "description": "Camión Mercedes Benz",
    "model": "Actros 2646"
  },
  "initialWeight": 8500.50,
  "state": "TARA_REGISTERED",
  "initialWeighing": "2025-10-25T10:30:00",
  "client": {
    "id": 1,
    "businessName": "Cliente SA"
  },
  "driver": {
    "id": 1,
    "name": "Juan",
    "lastName": "Pérez"
  }
}
```

**Errores Posibles:**

| Código | Descripción |
|--------|-------------|
| 404 | Camión no encontrado con ese dominio |
| 404 | No hay orden pendiente para ese camión |
| 400 | Orden no está en estado PENDING |
| 400 | Datos de entrada inválidos |
| 500 | Error interno del servidor |

**Ejemplo con cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/tms/weighing/initial?domain=ABC123&weight=8500.50"
```

---

### 2. Registrar Pesaje Final

**Endpoint:** `POST /api/v1/tms/weighing/final`

**Descripción:** 
Registra el peso del camión cargado y calcula la conciliación.

**Parámetros (Query Params):**

| Parámetro | Tipo | Obligatorio | Descripción |
|-----------|------|-------------|-------------|
| `activationCode` | String | Sí | Código de 5 dígitos generado en el pesaje inicial |
| `weight` | Double | Sí | Peso del camión cargado en kg (debe ser > 0) |

**Response (200 OK):**
```json
{
  "number": 12345,
  "activationCode": "47892",
  "state": "FINALIZED",
  "initialWeight": 8500.50,
  "finalWeight": 18750.25,
  "initialWeighing": "2025-10-25T10:30:00",
  "endWeighing": "2025-10-25T15:45:00",
  "accumulatedMass": 10200.00,
  "temperature": -42.5,
  "density": 0.58,
  "caudal": 15000.0,
  "truck": {
    "domain": "ABC123"
  },
  "client": {
    "id": 1,
    "businessName": "Cliente SA"
  },
  "driver": {
    "id": 1,
    "name": "Juan",
    "lastName": "Pérez"
  }
}
```

**Cálculos de Conciliación:**
- Neto por balanza: `finalWeight - initialWeight` = 10249.75 kg
- Producto cargado (caudalímetro): `accumulatedMass` = 10200.00 kg  
- Diferencia: 10249.75 - 10200.00 = **49.75 kg**

**Errores Posibles:**

| Código | Descripción |
|--------|-------------|
| 404 | Código de activación no encontrado |
| 400 | Orden no está en estado LOADING |
| 400 | Orden no tiene pesaje inicial |
| 400 | Datos de entrada inválidos |
| 500 | Error interno del servidor |

**Ejemplo con cURL:**
```bash
curl -X POST "http://localhost:8080/api/v1/tms/weighing/final?activationCode=47892&weight=18750.25"
```
  "message": "Operación exitosa",
  "data": {
    "orderNumber": 12345,
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

**Datos de Conciliación:**

| Campo | Descripción | Fórmula |
|-------|-------------|---------|
| `initialWeight` | Peso del camión vacío (tara) | Registrado en pesaje inicial |
| `finalWeight` | Peso del camión cargado | Registrado en pesaje final |
| `loadedProduct` | Producto cargado según caudalímetro | Última masa acumulada |
| `scaleNet` | Neto por balanza | finalWeight - initialWeight |
| `difference` | Diferencia balanza vs caudalímetro | scaleNet - loadedProduct |
| `averageTemperature` | Promedio de temperatura | Último valor (por ahora) |
| `averageDensity` | Promedio de densidad | Último valor (por ahora) |
| `averageFlow` | Promedio de caudal | Último valor (por ahora) |

**Errores Posibles:**

| Código | Descripción |
|--------|-------------|
| 404 | Código de activación no encontrado |
| 400 | Orden no está en estado LOADING |
| 400 | Orden no tiene pesaje inicial |
| 400 | Datos de entrada inválidos |
| 500 | Error interno del servidor |

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/tms/weighing/final \
  -H "Content-Type: application/json" \
  -d '{
    "activationCode": "47892",
    "weight": 18750.25
  }'
```

---

## 📊 Modelos de Datos

### Entidades Principales

#### `Order` (Orden)
```java
@Entity
public class Order {
    @Id
    private Long number;              // Número de orden
    
    private String activationCode;    // Código de 5 dígitos (TMS)
    private Double initialWeight;     // Peso inicial (tara)
    private Double finalWeight;       // Peso final
    
    @Enumerated(EnumType.STRING)
    private OrderState state;         // Estado actual
    
    private LocalDateTime initialWeighing;  // Cuándo se pesó vacío
    private LocalDateTime endWeighing;      // Cuándo se pesó cargado
    
    @ManyToOne
    private Truck truck;              // Camión asociado
    
    // Datos de carga (caudalímetro)
    private Double accumulatedMass;   // Última masa acumulada
    private Double temperature;       // Última temperatura
    private Double density;           // Última densidad
    private Double caudal;            // Último caudal
    
    // ... otros campos
}
```

#### `OrderStatusLog` (Log de Auditoría)
```java
@Entity
public class OrderStatusLog {
    @Id
    private Long id;
    
    private Long orderNumber;         // Orden que cambió
    
    @Enumerated(EnumType.STRING)
    private OrderState fromState;     // Estado anterior
    
    @Enumerated(EnumType.STRING)
    private OrderState toState;       // Nuevo estado
    
    private String actor;             // Quién hizo el cambio (ej: "TMS")
    private String note;              // Observaciones
    private LocalDateTime timestamp;  // Cuándo ocurrió
}
```

### Enumeración de Estados

```java
public enum OrderState {
    PENDING,            // 1 - Pendiente de pesaje inicial
    TARA_REGISTERED,    // 2 - Con pesaje inicial registrado
    LOADING,            // 3 - En carga (cerrada para carga)
    FINALIZED;          // 4 - Finalizada
    
    public boolean canTransitionTo(OrderState next) {
        return switch (this) {
            case PENDING -> next == TARA_REGISTERED;
            case TARA_REGISTERED -> next == LOADING;
            case LOADING -> next == FINALIZED;
            case FINALIZED -> false;  // Estado final
        };
    }
}
```

---

## 🔄 Flujo de Trabajo Completo

### Diagrama de Secuencia

```
SAP          TruckTeck        TMS Balanza      Sistema Carga
 │                │                 │                  │
 │──Orden─────▶  │                 │                  │
 │              [PENDING]           │                  │
 │                │                 │                  │
 │                │◀──Pesaje Inicial─│                │
 │              [TARA_REGISTERED]   │                  │
 │                │  (Código: 47892) │                  │
 │                │                 │                  │
 │                │◀──────────────────Inicia Carga────│
 │              [LOADING]           │                  │
 │                │◀──Datos en tiempo real─────────────│
 │                │  (masa, temp, densidad, caudal)    │
 │                │                 │                  │
 │                │──Cierra Carga──▶│                  │
 │              [LOADING]           │                  │
 │                │                 │                  │
 │                │◀───Pesaje Final──│                │
 │              [FINALIZED]         │                  │
 │                │ (Conciliación)  │                  │
```

### Descripción Paso a Paso

**1. Creación de la Orden (SAP)**
```
Estado: PENDING
Datos: Camión, Chofer, Cliente, Producto, Preset
Turno: 2025-10-25 14:00
```

**2. Arribo del Camión a Planta**
```
Operador registra en TMS:
- Dominio: ABC123
- Peso: 8500.50 kg

TMS llama a: POST /api/v1/tms/weighing/initial

Resultado:
- Estado: TARA_REGISTERED
- Código de activación: 47892
- Este código se le entrega al chofer
```

**3. Inicio de Carga**
```
Chofer ingresa código 47892 en el instrumento de carga
Instrumento habilita la bomba
Estado cambia a: LOADING
Preset: 10,000 kg
```

**4. Proceso de Carga (en tiempo real)**
```
Sistema de carga envía datos cada segundo:
- Masa acumulada: 100, 200, 300... 10,000 kg
- Temperatura: -42.5°C
- Densidad: 0.58 kg/m³
- Caudal: 15,000 kg/h

TruckTeck almacena estos datos según frecuencia configurada
```

**5. Cierre de Orden**
```
Cuando masa acumulada alcanza preset (10,000 kg):
- Bomba se detiene automáticamente
- Orden se cierra para carga
- No se aceptan más datos de detalle
```

**6. Pesaje Final**
```
Operador registra en TMS:
- Código de activación: 47892
- Peso final: 18,750.25 kg

TMS llama a: POST /api/v1/tms/weighing/final

Resultado:
- Estado: FINALIZED
- Conciliación:
  * Neto balanza: 10,249.75 kg
  * Caudalímetro: 10,200.00 kg
  * Diferencia: 49.75 kg (0.49%)
```

---

## 🔐 Seguridad y Roles

### Roles Propuestos

Para el examen final, deberás implementar **roles de usuario** que regulen el acceso a las APIs.

**Sugerencia de Roles:**

```java
public enum Role {
    // Sistema externo de balanza
    ROLE_TMS,
    
    // Sistema SAP (recepción de órdenes)
    ROLE_SAP,
    
    // Sistema de carga en tiempo real
    ROLE_LOADING_SYSTEM,
    
    // Operadores humanos
    ROLE_OPERATOR,
    
    // Administradores
    ROLE_ADMIN
}
```

### Matriz de Permisos

| Endpoint | TMS | SAP | Loading | Operator | Admin |
|----------|-----|-----|---------|----------|-------|
| POST /tms/weighing/initial | ✅ | ❌ | ❌ | ✅ | ✅ |
| POST /tms/weighing/final | ✅ | ❌ | ❌ | ✅ | ✅ |
| POST /orders (crear orden) | ❌ | ✅ | ❌ | ❌ | ✅ |
| POST /orders/{id}/detail (datos carga) | ❌ | ❌ | ✅ | ❌ | ✅ |
| POST /orders/{id}/close | ❌ | ❌ | ✅ | ✅ | ✅ |
| GET /orders/{id}/reconciliation | ❌ | ❌ | ❌ | ✅ | ✅ |

### Implementación con Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Endpoints TMS - solo accesibles por TMS y ADMIN
                .requestMatchers(POST, "/api/v1/tms/**")
                    .hasAnyRole("TMS", "OPERATOR", "ADMIN")
                
                // Endpoints SAP - solo accesibles por SAP y ADMIN
                .requestMatchers(POST, "/api/v1/orders")
                    .hasAnyRole("SAP", "ADMIN")
                
                // ... más configuraciones
            )
            .httpBasic() // o JWT
            .and()
            .csrf().disable(); // Para APIs REST
        
        return http.build();
    }
}
```

---

## 🧪 Ejemplos de Uso con Postman

### Colección Postman: Flujo Completo TMS

#### 1. **Crear Orden (SAP)** - Prerequisito
```http
POST /api/v1/orders
Content-Type: application/json
Authorization: Basic <SAP_credentials>

{
  "externalCode": "SAP-ORD-12345",
  "truck": {
    "domain": "ABC123"
  },
  "driver": {
    "id": 1
  },
  "client": {
    "id": 1
  },
  "product": {
    "id": 1
  },
  "scheduledDate": "2025-10-25T14:00:00",
  "preset": 10000.0
}
```

#### 2. **Registrar Pesaje Inicial (TMS)**
```http
POST /api/v1/tms/weighing/initial
Content-Type: application/json
Authorization: Basic <TMS_credentials>

{
  "domain": "ABC123",
  "weight": 8500.50
}

Tests (Postman):
pm.test("Status is 200", function() {
    pm.response.to.have.status(200);
});

pm.test("Activation code generated", function() {
    var json = pm.response.json();
    pm.expect(json.data.activationCode).to.have.lengthOf(5);
    // Guardar código para siguiente request
    pm.environment.set("activationCode", json.data.activationCode);
});

pm.test("State is TARA_REGISTERED", function() {
    var json = pm.response.json();
    pm.expect(json.data.state).to.equal("TARA_REGISTERED");
});
```

#### 3. **Simular Proceso de Carga** (Sistema de Carga)
```http
POST /api/v1/orders/{{orderNumber}}/loading-details
Content-Type: application/json

{
  "accumulatedMass": 5000.0,
  "temperature": -42.5,
  "density": 0.58,
  "caudal": 15000.0
}
```

#### 4. **Cerrar Orden** (Sistema de Carga)
```http
POST /api/v1/orders/{{orderNumber}}/close
Authorization: Basic <LOADING_credentials>
```

#### 5. **Registrar Pesaje Final (TMS)**
```http
POST /api/v1/tms/weighing/final
Content-Type: application/json
Authorization: Basic <TMS_credentials>

{
  "activationCode": "{{activationCode}}",
  "weight": 18750.25
}

Tests (Postman):
pm.test("Status is 200", function() {
    pm.response.to.have.status(200);
});

pm.test("State is FINALIZED", function() {
    var json = pm.response.json();
    pm.expect(json.data.state).to.equal("FINALIZED");
});

pm.test("Reconciliation calculated", function() {
    var json = pm.response.json();
    var recon = json.data.reconciliation;
    pm.expect(recon.scaleNet).to.be.above(0);
    pm.expect(recon.difference).to.exist;
});

pm.test("Difference is within acceptable range", function() {
    var json = pm.response.json();
    var diff = Math.abs(json.data.reconciliation.difference);
    var loaded = json.data.reconciliation.loadedProduct;
    var percentage = (diff / loaded) * 100;
    pm.expect(percentage).to.be.below(1); // < 1% de diferencia
});
```

### Scripts de Postman Útiles

**Pre-request Script para Simular Múltiples Datos de Carga:**
```javascript
// Simular envío de datos cada 10 segundos
const orderNumber = pm.environment.get("orderNumber");
const preset = 10000;
const steps = 10;
const increment = preset / steps;

for (let i = 1; i <= steps; i++) {
    setTimeout(() => {
        pm.sendRequest({
            url: `http://localhost:8080/api/v1/orders/${orderNumber}/loading-details`,
            method: 'POST',
            header: 'Content-Type: application/json',
            body: {
                mode: 'raw',
                raw: JSON.stringify({
                    accumulatedMass: increment * i,
                    temperature: -42.5,
                    density: 0.58,
                    caudal: 15000.0
                })
            }
        });
    }, i * 1000);
}
```

---

## ✅ Checklist de Implementación

### Para el Segundo Parcial (11/11/2025)

- [x] **Modelo de datos**
  - [x] Entidad `Order` con campos TMS
  - [x] Repositorio con queries personalizadas
  - [x] Enum `OrderState`
  - [x] Entidad `OrderStatusLog`

- [x] **Capa de negocio TMS**
  - [x] Interfaz `IOrderTmsBusiness`
  - [x] Implementación `OrderTmsBusiness`
  - [x] Generación de código de activación
  - [x] Registro de auditoría

- [x] **API REST TMS**
  - [x] Controlador `TmsRestController`
  - [x] Endpoint pesaje inicial
  - [x] Endpoint pesaje final
  - [x] DTOs de request/response
  - [x] Validaciones con `@Valid`

- [ ] **Documentación**
  - [x] Guía de integración (este documento)
  - [ ] OpenAPI/Swagger para endpoints TMS
  - [ ] Colección Postman con tests

- [ ] **Pruebas**
  - [ ] Tests unitarios para `OrderTmsBusiness`
  - [ ] Tests de integración para endpoints
  - [ ] Simulación de circuito completo

### Para el Examen Final

- [ ] **Seguridad**
  - [ ] Definir roles (TMS, SAP, OPERATOR, etc.)
  - [ ] Implementar autenticación
  - [ ] Configurar autorización por endpoint

- [ ] **Endpoints adicionales**
  - [ ] Crear orden (SAP)
  - [ ] Registrar datos de carga en tiempo real
  - [ ] Cerrar orden
  - [ ] Obtener conciliación

- [ ] **Cálculo de promedios**
  - [ ] Implementar cálculo real de temperatura media
  - [ ] Implementar cálculo real de densidad media
  - [ ] Implementar cálculo real de caudal medio

- [ ] **Frontend**
  - [ ] Dashboard de monitoreo de órdenes
  - [ ] Visualización en tiempo real
  - [ ] Gestión de alarmas de temperatura
  - [ ] Vista de conciliación

---

## 🚀 Próximos Pasos

### 1. Implementar Sistema SAP (Recepción de Órdenes)
Similar a TMS, crear:
- `OrderSapBusiness`
- `SapRestController` 
- Endpoint: `POST /api/v1/sap/orders`

### 2. Implementar Sistema de Carga en Tiempo Real
- `LoadingDataBusiness`
- `LoadingRestController`
- Endpoint: `POST /api/v1/loading/data`
- Endpoint: `POST /api/v1/loading/close`

### 3. Agregar Endpoint de Conciliación
```java
@GetMapping("/orders/{id}/reconciliation")
public ResponseEntity<?> getReconciliation(@PathVariable Long id) {
    // Solo para órdenes en estado FINALIZED
    // Retorna los mismos datos que pesaje final
}
```

### 4. Configuración de Frecuencia de Almacenamiento
```java
@ConfigurationProperties(prefix = "loading")
public class LoadingConfig {
    private int receptionFrequencySeconds = 1;
    private int storageFrequencySeconds = 10;
}
```

### 5. Tests Automatizados
```java
@SpringBootTest
@AutoConfigureMockMvc
class TmsIntegrationTest {
    
    @Test
    void testCompleteWeighingFlow() {
        // 1. Crear orden
        // 2. Registrar pesaje inicial
        // 3. Verificar código generado
        // 4. Simular carga
        // 5. Registrar pesaje final
        // 6. Verificar conciliación
    }
}
```

---

## 📝 Notas Importantes

### Consideraciones de Diseño

1. **¿Por qué `domain` en pesaje inicial y no `orderNumber`?**
   - Realismo: El operador de la balanza solo ve el camión, no conoce el número de orden.
   - La orden se busca automáticamente por camión + estado PENDING.

2. **¿Por qué código de activación de 5 dígitos?**
   - Fácil de memorizar para el chofer
   - Suficiente entropía (100,000 combinaciones)
   - Se valida unicidad antes de asignar

3. **¿Por qué validar estado antes de cada operación?**
   - Prevenir inconsistencias de datos
   - Garantizar que el flujo sea secuencial
   - Facilitar debugging

4. **¿Por qué log de auditoría separado?**
   - Cumplimiento regulatorio
   - Trazabilidad completa
   - No contamina la entidad principal

### Mejoras Futuras

1. **Manejo de Excepciones**
   - Código de activación duplicado (retry automático)
   - Timeout en registro de pesaje
   - Rollback en caso de error

2. **Validaciones Adicionales**
   - Peso final > peso inicial
   - Diferencia balanza-caudalímetro dentro de tolerancia
   - Tiempo entre pesajes dentro de ventana esperada

3. **Notificaciones**
   - Email/SMS al chofer con código de activación
   - Alerta si diferencia > umbral
   - Notificación cuando orden está lista

4. **Métricas**
   - Tiempo promedio de carga
   - Diferencias promedio por producto
   - Eficiencia de balanza vs caudalímetro

---

## 📞 Soporte

Si tienes dudas sobre la implementación TMS:

1. Revisa este documento
2. Consulta el código fuente comentado
3. Ejecuta los tests de ejemplo
4. Consulta con el equipo

---

**Documento creado:** 25 de octubre de 2025  
**Versión:** 1.0  
**Autor:** Sistema TruckTeck - Equipo de Desarrollo  
**Proyecto:** Ingeniería Web 3 - 2025
