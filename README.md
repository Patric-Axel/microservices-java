# Order System - Reto Backend

Sistema backend de gestión de pedidos desarrollado con una arquitectura
de microservicios. La solución separa la gestión de pedidos y el
inventario, utiliza un API Gateway como punto de entrada, Keycloak para
autenticación OAuth 2.0 / OpenID Connect y bases de datos MySQL
independientes por servicio.

> Para levantar y probar rápidamente la solución, consultar
> `GUIA_RAPIDA.

## 1. Arquitectura

``` text
                    ┌─────────────────┐
                    │    Keycloak     │
                    │      :8083      │
                    └─────────────────┘

                    
                    ┌──────────────────┐
                    │ Client / Postman │
                    └────────┬─────────┘
                             │
                       JWT + X-Trace-Id
                             │
                             ▼
                    ┌─────────────────┐
                    │   API Gateway   │
                    │      :8082      │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  Order Service  │
                    └───────┬─────────┘
                            │ WebClient
                            ▼
                    ┌─────────────────┐
                    │Inventory Service│
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐

Order Service ───────────────► MySQL Order
Inventory Service ───────────► MySQL Inventory

```

El acceso externo a las APIs se realiza mediante el API Gateway.

## 2. Componentes

### API Gateway

Responsable de:

-   Punto de entrada de las solicitudes externas.
-   Validación de autenticación mediante JWT.
-   Integración con Keycloak como proveedor de identidad.
-   Gestión y propagación de `X-Trace-Id`.
-   Enrutamiento hacia los microservicios.

Puerto expuesto: `8082`.

### Order Service

Responsable de:

-   Registrar pedidos.
-   Listar pedidos.
-   Consultar un pedido por ID.
-   Cancelar pedidos.
-   Mantener el historial de estados.
-   Calcular el total del pedido.
-   Comunicarse con Inventory Service mediante WebClient.
-   Gestionar errores asociados al ciclo de vida del pedido.

### Inventory Service

Responsable de:

-   Registrar productos.
-   Listar productos.
-   Consultar productos por ID.
-   Actualizar productos.
-   Descontar stock.
-   Restaurar stock.
-   Validar disponibilidad de inventario.

### Keycloak

Responsable de autenticación y emisión de tokens JWT.

Configuración principal:

-   Realm: `order-system`
-   Client ID: `api-gateway`
-   Cliente público
-   OAuth 2.0 / OpenID Connect
-   Authorization Code + PKCE
-   PKCE: `S256`

La configuración se encuentra en:

`keycloak/order-system-realm.json`

y se importa automáticamente al levantar Keycloak mediante Docker
Compose.

## 3. Flujo de creación de un pedido

``` text
POST /api/orders
       │
       ▼
    PENDING
       │
       ▼
Order Service
       │
       │ WebClient
       ▼
Inventory Service
       │
       ▼
 ¿Stock suficiente?
      /      \
    SÍ        NO
    │          │
    ▼          ▼
Descontar   CANCELLED
  stock        │
    │          ▼
    ▼       Error 409
CONFIRMED
    │
    ▼
OrderHistory
```

Al registrar un pedido:

1.  Order Service obtiene la información del producto desde Inventory
    Service.
2.  Se construye el pedido y se genera un número con formato
    `ORD-XXXXXXXX`.
3.  Se calcula el total utilizando el precio del producto y la cantidad
    solicitada.
4.  El pedido se registra inicialmente como `PENDING`.
5.  El estado `PENDING` se registra en el historial.
6.  Order Service solicita a Inventory Service el descuento del stock.
7.  Si existe stock suficiente, el pedido pasa a `CONFIRMED`.
8.  Si el stock es insuficiente, el pedido pasa a `CANCELLED` y se
    genera el error correspondiente.
9.  Las transiciones de estado quedan registradas en `OrderHistory`.

## 4. Estados del pedido

Los estados utilizados son:

-   `PENDING`: pedido registrado y pendiente de validación de
    inventario.
-   `CONFIRMED`: inventario validado y stock descontado correctamente.
-   `CANCELLED`: pedido cancelado o sin stock disponible.

El historial permite mantener visibilidad sobre el ciclo de vida de cada
pedido.

## 5. Endpoints principales

### Inventory

``` text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PATCH  /api/products/{id}
//los siguientes son para el consumo del servicio order
PATCH  /api/products/{id}/stock?quantity={quantity}
PATCH  /api/products/{id}/stock/restore?quantity={quantity}
```

Los endpoints de modificación de stock son utilizados por Order Service
durante la gestión del pedido.

### Orders

``` text
GET    /api/orders
GET    /api/orders/{id}
GET    /api/orders/{id}/history
POST   /api/orders
PATCH  /api/orders/{id}/cancel
```

## 6. Comunicación entre microservicios

La comunicación síncrona desde Order Service hacia Inventory Service se
realiza mediante `WebClient`.

Flujo:

``` text
Order Service
      │
      │ HTTP / WebClient
      ▼
Inventory Service
```

Entre las operaciones utilizadas se encuentran:

-   Obtener producto por ID.
-   Validar/actualizar stock.
-   Restaurar stock durante el flujo de cancelación.

## 7. Persistencia

Cada microservicio mantiene su propia base de datos:

``` text
Order Service     → MySQL Order
Inventory Service → MySQL Inventory
```

Esto mantiene la independencia de persistencia entre los servicios.

La persistencia se implementa mediante Spring Data JPA / Hibernate.

## 8. Seguridad

La solución utiliza:

-   OAuth 2.0
-   OpenID Connect
-   JWT
-   Keycloak
-   Authorization Code + PKCE (`S256`)

El API Gateway valida el token antes de permitir el acceso a las rutas
protegidas.

No se entrega un Access Token fijo. El evaluador genera un token nuevo
desde Keycloak utilizando la configuración OAuth incluida en la
colección Postman.

El realm, cliente y usuario de prueba se distribuyen mediante:

`keycloak/order-system-realm.json`

Para una importación nueva, el usuario de prueba es `user` y su contraseña
de demostración es `demo-change-me`. Keycloak genera nuevas claves al importar
el realm. Estas credenciales son únicamente para desarrollo local.

## 9. Trazabilidad con X-Trace-Id

Se utiliza `X-Trace-Id` para correlacionar una misma operación entre los
componentes.

``` text
Postman / Cliente
       │
       ▼
API Gateway
       │ X-Trace-Id
       ▼
Order Service
       │ X-Trace-Id
       ▼
Inventory Service
```

El mismo identificador se propaga entre servicios y puede verificarse en
los logs.

Ejemplo de identificador para pruebas:

``` http
X-Trace-Id: RETO-TRACE-001
```

Logs:

``` bash
docker compose logs api-gateway
docker compose logs order-service
docker compose logs inventory-service
```

El `traceId` también forma parte de las respuestas de error
implementadas.

## 10. Manejo de errores

Los errores utilizan una estructura uniforme.

Ejemplo:

``` json
{
  "timestamp": "2026-10-06T04:00:00",
  "status": 409,
  "code": "STOCK_INSUFFICIENT",
  "message": "no hay stock disponible",
  "traceId": "RETO-TRACE-001"
}
```

Entre los escenarios manejados se encuentran:

-   Stock insuficiente.
-   Pedido inexistente.
-   Producto inexistente.
-   Estado de pedido inválido.
-   Datos de entrada inválidos.
-   Inventory Service no disponible.
-   Token requerido, inválido o no autorizado.
-   Error interno no controlado.

Se utiliza manejo global de excepciones para mantener un formato
consistente.

## 11. Validaciones

Los DTO de entrada utilizan Bean Validation.

Ejemplos de validaciones implementadas:

-   SKU obligatorio.
-   Nombre de producto obligatorio.
-   Precio obligatorio y mayor que cero.
-   Stock no negativo.
-   ID de producto obligatorio.
-   Cantidad obligatoria y mayor que cero.
-   Datos requeridos para registrar un pedido.

## 12. Docker y Docker Compose

La solución se encuentra dockerizada y puede levantarse desde la raíz
con:

``` bash
docker compose up --build
```

Servicios levantados:

-   `api-gateway`
-   `order-service`
-   `inventory-service`
-   `mysql-order`
-   `mysql-inventory`
-   `keycloak`

Para comprobar el estado:

``` bash
docker compose ps
```

Para detener la solución:

``` bash
docker compose down
```

Las bases MySQL utilizan health checks antes de iniciar los servicios
dependientes.

Keycloak importa automáticamente el realm `order-system` durante un
arranque limpio.

## 13. Postman

La colección se encuentra en:

`Postman/Order System.postman_collection.json`

La colección fue exportada y posteriormente importada nuevamente para
comprobar su portabilidad.

Incluye:

-   Solicitudes de Inventory.
-   Solicitudes de Orders.
-   Casos exitosos.
-   Casos de error.
-   Configuración OAuth 2.0 para obtener un token nuevo desde Keycloak.

Después de importar la colección, el evaluador debe generar un nuevo
Access Token y seleccionar `Use Token`.

## 14. Pruebas

El proyecto incluye pruebas automatizadas de servicios utilizando JUnit
5 y las herramientas de testing utilizadas en el proyecto.

Además de las pruebas automatizadas, se realizaron pruebas funcionales
mediante Postman sobre el sistema ejecutándose en Docker.

Entre los escenarios comprobados se encuentran:

-   Operaciones de inventario.
-   Registro de pedidos.
-   Validación de stock.
-   Stock insuficiente.
-   Consulta de pedidos.
-   Historial del pedido.
-   Cancelación.
-   Manejo de estados inválidos.
-   Producto inexistente.
-   Autenticación mediante JWT.
-   Respuestas sin token/token inválido.
-   Propagación de `X-Trace-Id`.

Antes de entregar se recomienda ejecutar nuevamente los tests Maven de
cada microservicio.

## 15. Tecnologías utilizadas

-   Java
-   Spring Boot
-   Spring Data JPA / Hibernate
-   Spring Security
-   Spring WebFlux / WebClient
-   Spring Cloud Gateway
-   OAuth 2.0 / OpenID Connect
-   JWT
-   Keycloak
-   MySQL
-   MapStruct
-   Lombok
-   Bean Validation
-   JUnit 5
-   Maven
-   Docker
-   Docker Compose
-   Postman

## 16. Requisitos implementados

  Requisito                                 Estado
  ----------------------------------------- --------------
  Order Service                             Implementado
  Inventory Service                         Implementado
  API Gateway                               Implementado
  Persistencia independiente por servicio   Implementado
  Comunicación HTTP mediante WebClient      Implementado
  OAuth 2.0 / OpenID Connect                Implementado
  JWT                                       Implementado
  Keycloak                                  Implementado
  Authorization Code + PKCE S256            Implementado
  Estados PENDING / CONFIRMED / CANCELLED   Implementado
  Historial de estados                      Implementado
  Validación y descuento de stock           Implementado
  Restauración de stock                     Implementado
  Manejo global de errores                  Implementado
  Formato uniforme de errores               Implementado
  X-Trace-Id                                Implementado
  Docker Compose                            Implementado
  JUnit 5                                   Implementado
  MapStruct                                 Implementado
  Lombok                                    Implementado
  Bean Validation                           Implementado
  Colección Postman                         Implementado

## 17. Pendientes y mejoras

### Spring AOP

Queda pendiente incorporar Spring AOP para centralizar aspectos
transversales, por ejemplo:

-   Logging de operaciones.
-   Medición de tiempos de ejecución.
-   Registro centralizado de errores.
-   Integración del contexto de `X-Trace-Id` con aspectos transversales.

La lógica de negocio actual permanece separada en las capas
correspondientes.

### OpenAPI / Swagger

Queda pendiente exponer la especificación OpenAPI y Swagger UI de manera
interactiva.

Actualmente los endpoints, flujo de consumo y escenarios de prueba se
encuentran documentados en este README y en la colección Postman.

## 18. Estructura de entrega

``` text
ws_reto/
├── api-gateway/
├── inventory-service/
├── keycloak/
│   └── order-system-realm.json
├── order-service/
├── Postman/
│   └── Order System.postman_collection.json
├── compose.yaml
├── README.md
```

## 19. Guía rápida

Para instrucciones paso a paso sobre:

-   levantar el proyecto;
-   importar Postman;
-   obtener un Access Token;
-   probar las APIs;
-   verificar trazabilidad; y
-   detener los contenedores;

