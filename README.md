# workspace
Prueba técnica backend - Banco Cuscatlán 2026. Microservicio en Spring Boot para reservar espacios de coworking.

Desarrollado con Java 17 y Spring Boot 3.5.16 se escogió dicha versión ya que según la documentación de spring es la version mas reciente y estable de spring boot 3x.

## Cómo levantarlo
Requiere Docker instalado en la maquina a correr

Levantar solución completa:

```
docker compose up --build
```

Si solo necesita la base para desarrollo local:
```
docker compose up -d postgres
```

La API corre en `http://localhost:8080`. 

Las migraciones corren solas, no hace falta hacer nada más. Ya vienen usuarios y espacios de prueba cargados (`V2__initial_data.sql`).


## Documentación
Se puede ver la docmuentación Swagger en `http://localhost:8080/swagger-ui.html`

## Arquitectura
Capas por responsabilidad técnica (`controller`, `service`, `repository`, `dto`, `entity`, `mapper`, `exception`, `security`, `config`, `client`), no por feature. Los DTOs de request y response van siempre separados; ninguna entidad JPA se expone directo en un controller.

## Seguridad: JWT
El Api usa JWT en vez de sesiones porque la API no guarda nada del lado del servidor. El token ya trae adentro el email y el rol del usuario, así que cada request se valida solo con lo que llega en el header, sin consultar ninguna sesión guardada. 

## Validación y manejo de errores
Cada DTO de entrada tiene sus propias reglas (`@NotBlank`, `@Pattern`, etc.), así los datos malos se rechazan antes de llegar a la lógica de negocio, sin tener que escribir esos chequeos a mano en cada service.

Y en vez de que cada controller resuelva sus errores por su cuenta, todo pasa por un solo lugar `GlobalExceptionHandler` que arma la respuesta siempre igual: mismo formato de JSON, mismo código HTTP según el tipo de error. Si agrego un endpoint nuevo, no tengo que volver a pensar en el manejo de errores, ya está resuelto una sola vez para toda la Api.

## Reglas de reservas
- Un `USER` solo reserva para sí mismo.
- Un `ADMIN` reserva a nombre de un `USER`, nunca para otro `ADMIN` ni para sí mismo.
- Un `ADMIN` ve y cancela cualquier reserva; un `USER` solo las propias.


## Patrón de diseño: State
Elegido para el ciclo de vida de la reserva (`PENDING` → `CONFIRMED` / `PENDING_PAYMENT` / `PAYMENT_DECLINED` → `CANCELLED` / `COMPLETED`).

El problema: no todos los cambios de estado son válidos (no se puede cancelar algo que ya está cancelado). Resolverlo a puro `if` obliga a repetir la misma validación en cada lugar del código que cambie un estado.

La solución: cada estado sabe a qué otros puede pasar. El código nunca pregunta en qué estado está, le dice al estado actual *cancelate* (`reservation.getStatus().cancel()`), y si no puede, el error sale solo.

Lo armé agregándole comportamiento a cada valor del enum `ReservationStatus`, sin una clase por estado.

## Pago externo + Circuit Breaker
El pago se simula contra WireMock Cloud con 5 tarjetas de prueba:

- `4111111111111111`: aprobada, la latencia varía, la reserva queda `CONFIRMED`.
- `4000000000000002`: rechazada por fondos insuficientes, queda `PAYMENT_DECLINED`.
- `4000000000000119`: el gateway tira error 500, ahí entra el fallback y queda `PENDING_PAYMENT`.
- `4000000000000044`: se aprueba pero tarda 8s, como el timeout del cliente es de 3s corta antes y también queda `PENDING_PAYMENT`.
- cualquier otra tarjeta: 400 inválida, queda `PAYMENT_DECLINED`.

Usé `resilience4j-spring-boot3` en vez del artifact que menciona el PDF (`spring-cloud-starter-circuitbreaker-resilience4j`), porque ese no expone `/actuator/circuitbreakers` ni da la anotación `@CircuitBreaker(fallbackMethod=...)` que pide el enunciado.

Config del circuito: ventana de 10 llamadas, se abre con 50% de fallas o lentitud (+2s). El `RestClient` corta a los 3s para no dejar requests colgados.

Con el circuito abierto, la reserva queda en `PENDING_PAYMENT` en vez de fallar. **Pendiente:** nada reintenta ese pago automáticamente todavía.

## Notificación asíncrona
Al confirmar una reserva y cuando se crea un usuario se dispara un correo simulado con `@Async`, sin bloquear la respuesta.

## Reporte de ocupación cacheado
`GET /api/reports/occupancy` (solo `ADMIN`) calcula el % de ocupación por espacio con una query nativa de Postgres, cacheada con `@Cacheable` e invalidada al crear o cancelar una reserva.

## Cómo evitamos N+1
El mapper de reservas solo expone los IDs de las relaciones (`user`, `space`), nunca campos anidados así nunca se dispara una consulta extra por fila.

## Transacciones en reservas
Crear una reserva hace varios pasos seguidos (validar horario, calcular precio, cobrar, guardar), y todo tiene que quedar bien o no pasar nada, por eso el método está marcado `@Transactional`.

Para el chequeo de solapamiento uso `saveAndFlush()` en vez de un `save()` normal: eso obliga a que la base de datos revise el constraint anti-solapamiento ahí mismo, dentro de la transacción, en vez de esperar a que Hibernate decida guardar los cambios más tarde. Si la base lo rechaza, atrapo ese error y lo convierto en una excepción propia (`OverlappingReservationException`) en vez de dejar que se rompa con un error feo de base de datos.

## Configuración por perfiles
Hay un `application-dev.yml` y un `application-prod.yml` para separar la config según el ambiente. Y en vez de leer cada propiedad suelta con `@Value("${...}")` desperdigado por el código, agrupé cada bloque de configuración en su propia clase (`JwtProperties`, `PaymentProperties`, `ReservationProperties`, `EmailProperties`). Así cada una sabe exactamente qué le pertenece, y si me equivoco escribiendo el nombre de una propiedad en el yml, el error sale al arrancar la app, no en producción cuando alguien la use.

## Actuator
Expuse `health`, `info`, `metrics` (más `circuitbreakers`/`circuitbreakerevents` para el circuit breaker) para tener visibilidad de que la app está viva sin inventar mi propio endpoint de salud. Es justo el tipo de información que un orquestador (Docker, Kubernetes) usa para decidir si reiniciar el contenedor o no.

## Tests
Mockito para la lógica de negocio, más un test simple para las transiciones de estado. Dos de integración con Testcontainers (Postgres real, no H2): uno prueba el constraint anti-solapamiento de la base, otro el flujo completo con login y creación de reserva reales.

## Fuera de alcance
- Reintento automático de pagos pendientes.
- Reservas en medias horas.
- Reserva diferenciada por tipo de espacio.
- Endpoint de disponibilidad antes de reservar.
- Paginación en el listado de espacios.

## Endpoints principales

- `POST /api/users/sign-up` — registro público.
- `POST /api/users/auth` — login.
- `GET/POST/PUT/DELETE /api/spaces` — CRUD de espacios.
- `GET/POST /api/reservations`, `POST /api/reservations/{id}/cancel`.
- `GET /api/reports/occupancy`.
- `GET /actuator/health`, `/actuator/circuitbreakers`.
