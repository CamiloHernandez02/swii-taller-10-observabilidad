# Taller 10: Observabilidad + Saga Pattern

Ingeniería de Software II (UPTC). Implementación de observabilidad distribuida con X-Correlation-ID y Saga Pattern para transacciones distribuidas.

**Base**: Taller 9 (EDA)
**Nuevo**: Taller 10 agrega observabilidad (Micrometer, Prometheus) y Saga Orquestado

| Pieza | Proyecto | Puerto | Descripción |
| :--- | :--- | :--- | :--- |
| Registro de servicios (Eureka) | `eureka-server` | 8761 | Service Discovery |
| API Gateway | `gateway` | 8000 | Enrutamiento centralizado |
| Investigadores (Observabilidad) | `investigadores-service` | 8081 | X-Correlation-ID, métricas, Saga |
| Publicaciones (Observabilidad) | `publicaciones-service` | 8082 | X-Correlation-ID, métricas, Saga |
| Consultas (Observabilidad) | `consultas-service` | 8084 | X-Correlation-ID, métricas |
| Prometheus | (interno) | 9090 | Recolección de métricas |

## Características T10: Observabilidad + Saga
- ✅ CorrelacionIdInterceptor (propaga X-Correlation-ID)
- ✅ MetricasService (counters, timers con percentiles)
- ✅ Prometheus /actuator/prometheus
- ✅ SagaOrquestador (ejecución secuencial, compensaciones LIFO)
- ✅ Rollback automático en caso de error
- ✅ Tests: 6 (2 Saga + 4 Métricas)

Patrones: gRPC (`VerificadorInvestigadorGrpc`), Circuit Breaker (Resilience4j), Service Registry (Eureka), API Gateway, Domain Event + Transactional Outbox (Kafka), Saga por coreografía (eliminar investigador).

## Ejecutar

```bash
cd investigadores-service && docker compose up -d      # PostgreSQL + Kafka
cd publicaciones-service && docker compose up -d       # MongoDB
# en terminales separadas, en este orden:
cd eureka-server && ./mvnw spring-boot:run
cd investigadores-service && ./mvnw spring-boot:run
cd publicaciones-service && ./mvnw spring-boot:run
cd gateway && ./mvnw spring-boot:run
```

Los servicios se consumen por el Gateway: `http://localhost:8000/api/investigadores` y `http://localhost:8000/api/publicaciones`.

Ver [NOTAS.md](NOTAS.md) para la verificación y las decisiones.
