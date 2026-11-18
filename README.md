# Taller Lección 6: comunicación real entre microservicios

Ingeniería de Software II (UPTC). Parte del estado del Taller de la Lección 5 (Java 25, Spring Boot 4.1.1, Spring Cloud 2025.1.3).

| Pieza | Proyecto | Puerto |
| :--- | :--- | :--- |
| Registro de servicios (Eureka) | `eureka-server` | 8761 |
| API Gateway (Spring Cloud Gateway) | `gateway` | 8000 |
| Investigadores (REST, gRPC servidor, Outbox, Saga) | `investigadores-service` | 8080 / gRPC 9090 |
| Publicaciones (gRPC cliente, Circuit Breaker, consumidores Kafka) | `publicaciones-service` | 8081 |

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
