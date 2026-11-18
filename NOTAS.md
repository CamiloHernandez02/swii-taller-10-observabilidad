# Notas del Taller de la Lección 6

## Plan de comunicación

| Necesidad | Patrón | Dónde |
| :--- | :--- | :--- |
| Confirmar que un investigador existe antes de registrar una publicación | RPI síncrono: gRPC | `VerificadorInvestigadorGrpc` |
| No caer en cascada si `investigadores-service` está lento o caído | Circuit Breaker (Resilience4j) | `VerificadorInvestigadorGrpc` (responde 503) |
| Ninguna dirección de red fija | Service Registry (Eureka) | `eureka-server` |
| Único punto de entrada | API Gateway | `gateway` |
| Notificar el registro sin acoplar | Domain Event + Transactional Outbox (Kafka) | `OutboxPublicadorEventos`, `OutboxPoller` |
| Eliminar un investigador y marcar sus publicaciones sin transacción distribuida | Saga (coreografía) | `iniciarEliminacion` / `InvestigadorEnEliminacionListener` / `PublicacionesMarcadasListener` |

## Limitaciones documentadas

- El canal gRPC sigue apuntando a `localhost:9090` (`static://`): Eureka resuelve las rutas `lb://` del Gateway, pero integrar el balanceo gRPC con Eureka queda fuera del alcance.
- `publicaciones-service` envía la confirmación de la Saga directamente con `KafkaTemplate`, sin outbox.
- No se implementa la transacción de compensación (temporizador que devuelva el estado a `ACTIVO`); es el reto opcional del taller.
- El `.proto` se copia en cada servicio a propósito, para que ninguno dependa del código del otro.

## Verificación de punta a punta (ejecutada)

Con Eureka, Gateway, ambos servicios, PostgreSQL, MongoDB y Kafka corriendo:

1. `POST /api/investigadores` por el Gateway: 201; el evento `investigadores.registrados` llega a `publicaciones-service`.
2. `POST /api/publicaciones` con ese correo: 201; con un correo inventado: 404 (verificación gRPC real).
3. `DELETE /api/investigadores/{id}`: 202; en los logs aparece la secuencia `Evento N publicado en investigadores.en-eliminacion`, `Saga: 3 publicaciones de ... marcadas como huérfanas`, `Saga: publicaciones confirmó para ... Eliminando investigador.`
4. El investigador ya no está en `SELECT id, correo_institucional, estado FROM investigadores;`.

No se probó manualmente el ciclo `CLOSED_TO_OPEN`/`HALF_OPEN` del Circuit Breaker ni el apagado de Kafka con el outbox.
