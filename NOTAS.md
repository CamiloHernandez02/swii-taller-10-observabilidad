# Notas Taller 10: Observability + Saga Pattern

## Resumen Ejecutivo
Implementación de observabilidad distribuida con correlation IDs, métricas Prometheus y distributed tracing, combinado con Saga pattern orchestration para transacciones distribuidas confiables.

## Componentes Principales

### Observabilidad

#### 1. CorrelationIdHolder
- ThreadLocal para almacenar correlation ID
- Generación UUID automática
- Limpieza segura después de requestso

#### 2. CorrelationIdFilter
- Servlet filter que inyecta correlation ID
- MDC (Mapped Diagnostic Context) para logs
- Headers HTTP bi-direccionales

#### 3. CorrelationIdInterceptor
- Alternativa a filter basada en interceptor
- Inyección en response headers
- Limpieza automática post-request

#### 4. OrderMetricsService
- Contadores: órdenes creadas, fallos
- Temporizadores: tiempo de procesamiento
- Medidores: monto total de órdenes
- Histogramas: distribución de latencias

#### 5. TracingConfig
- Configuración de Spring Web MVC
- Registro de interceptores
- Exclusión de rutas administrativas

### Saga Pattern

#### 1. OrderSagaState
- Estados: PENDING → PAYMENT → INVENTORY → SHIPMENT → COMPLETED
- Transiciones validadas
- Razón de fallos para compensación

#### 2. OrderSagaOrchestrator
- Orquestación central de saga
- Pasos secuenciales con compensación
- Manejo de fallos con rollback

## Flujo de Ejecución

### Request Normal
```
Request HTTP
   ↓
Generate/Extract Correlation ID
   ↓
Set MDC
   ↓
Handler (Order Service)
   ↓
OrderSagaOrchestrator.startOrderSaga()
   ├─ processPayment() → PAYMENT_PROCESSED
   ├─ reserveInventory() → INVENTORY_RESERVED
   ├─ scheduleShipment() → SHIPMENT_SCHEDULED
   └─ Complete → COMPLETED
   ↓
Log with CorrelationID
   ↓
Return Response + X-Correlation-ID header
```

### Request con Fallo
```
reserveInventory() falla
   ↓
compensatePayment()
   ↓
compensateInventory()
   ↓
compensateShipment()
   ↓
State = FAILED
```

## Pruebas Incluidas

### OrderSagaOrchestratorTest
- Creación de saga
- Transiciones de estado
- Manejo de fallos
- Almacenamiento de todas las sagas

### CorrelationIdHolderTest
- Generación de IDs únicos
- Persistencia en thread
- Limpieza segura

## Instalación y Ejecución

```bash
# Compilar
mvn clean package

# Tests
mvn test

# Ejecutar
mvn spring-boot:run
```

## Configuración para Tracing Real

```yaml
# application.yml
spring:
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:
      probability: 1.0  # 100% sampling (ajustar en prod)

management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
  metrics:
    export:
      prometheus:
        enabled: true
```

## Docker Stack (Opcional)

```bash
# Levantar Zipkin y Prometheus
docker run -d -p 9411:9411 openzipkin/zipkin
docker run -d -p 9090:9090 prom/prometheus
```

## Acceso a Métricas

- Prometheus: http://localhost:9090
- Grafana (si está): http://localhost:3000
- Zipkin: http://localhost:9411
- Actuator: http://localhost:8080/actuator

## Patrones Implementados

### 1. Saga Orchestration
- Coordinador central (OrderSagaOrchestrator)
- Control total del flujo
- Compensación automática

### 2. Correlation ID Pattern
- Rastrear request across services
- MDC para logging correlacionado
- Headers HTTP para propagación

### 3. Metrics Collection
- Counters, Timers, Gauges
- Prometheus compatible
- Grafana dashboards

### 4. Distributed Tracing Ready
- Structure logs para tracing
- Micrometer/Brave compatible
- Zipkin exportable

## Consideraciones de Producción

1. **Sampling Rate**: Reducir en alta carga
2. **Retention**: Políticas de limpieza de logs
3. **Alert Rules**: Umbrales para sagasque fallan
4. **Compensación Idempotente**: Poder ejecutar múltiples veces
5. **Timeout**: Timeouts apropiados por paso
6. **Monitoring**: Dashboard de sagasstatus
7. **Audit Trail**: Logs de cada transición

## Mejoras Futuras

- [ ] Persistencia de saga state en BD
- [ ] Retry automático con backoff
- [ ] Timeout configurable por paso
- [ ] Dashboard Grafana para métricas
- [ ] Alertas en fallos de saga
- [ ] Multi-step compensation
- [ ] Saga pause/resume
