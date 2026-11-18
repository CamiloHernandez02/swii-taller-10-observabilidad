# TALLER 10: Observabilidad + Saga Pattern

## Objetivos
- Implementar observabilidad con X-Correlation-ID para trazabilidad distribuida
- Recolectar métricas con Micrometer y Prometheus
- Implementar el patrón Saga para transacciones distribuidas
- Implementar compensaciones automáticas en caso de fallos

## Arquitectura Observabilidad + Saga

```
┌──────────────────────────────────────────────────────────────┐
│                 Observabilidad Distribuida                   │
├──────────────────────────────────────────────────────────────┤
│
│  Correlación de Requests:
│  Client → Service A → Service B → Service C
│          ↓          ↓             ↓
│        X-Correlation-ID propagado en todos los headers
│
│  Métricas (Prometheus):
│  - http.requests.total (counter)
│  - http.errors.total (counter)
│  - http.requests.duration (timer con percentiles)
│  - Accesible en /actuator/prometheus
│
│  Saga Pattern (Transacciones Distribuidas):
│  ┌──────────────────────────────────────────┐
│  │ SagaOrquestador                          │
│  │ - Ejecuta steps secuencialmente          │
│  │ - Registra compensaciones (LIFO)         │
│  │ - Rollback automático en caso de error   │
│  └──────────────────────────────────────────┘
```

## Concepto 1: X-Correlation-ID

### Problema
Con microservicios, un request puede atravesar múltiples servicios:
```
GET /investigadores/inv-001
  ↓
investigadores-service llama a publicaciones-service
  ↓
publicaciones-service llama a consultas-service
  ↓
¿Cómo sabemos que estos 3 requests están relacionados en los logs?
```

### Solución: CorrelacionIdInterceptor
```java
// Interceptor registrado en WebMvcConfigurer
@Component
public class CorrelacionIdInterceptor implements HandlerInterceptor {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String id = request.getHeader(CORRELATION_ID_HEADER);
        if (id == null) id = UUID.randomUUID().toString();
        
        correlationId.set(id);
        response.setHeader(CORRELATION_ID_HEADER, id);
        return true;
    }
}
```

### Uso
```bash
# Cliente envía request inicial
curl -H "X-Correlation-ID: abc-123" http://localhost:8081/investigadores

# Cada microservicio propaga el mismo ID
# - Logs: [abc-123] Procesando investigador X
# - Headers: X-Correlation-ID: abc-123 (en llamadas HTTP downstream)
# - Tracing: Correlacionar requests en ELK Stack
```

## Concepto 2: Métricas con Micrometer

### MetricasService
Recolecta métricas de negoció/técnicas:

```java
@Service
public class MetricasService {
    private Counter requestCount;      // Total de requests
    private Counter errorCount;        // Total de errores
    private Timer requestTimer;        // Duración con percentiles
    
    public void incrementarRequestCount() { requestCount.increment(); }
    public void incrementarErrorCount() { errorCount.increment(); }
    public Timer.Sample iniciarTimer() { return Timer.start(registry); }
    public void detenerTimer(Timer.Sample sample) { sample.stop(requestTimer); }
}
```

### Registrarse en Prometheus
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Monitorear
```bash
# Métricas disponibles en:
curl http://localhost:8081/actuator/prometheus

# Output (formato Prometheus):
# http_requests_total 125.0
# http_errors_total 3.0
# http_requests_duration_seconds{quantile="0.5"} 0.025
# http_requests_duration_seconds{quantile="0.95"} 0.102
# http_requests_duration_seconds{quantile="0.99"} 0.250
```

## Concepto 3: Saga Pattern

### Problema: Transacciones Distribuidas
Escenario: Crear un investigador y automáticamente crear una carpeta en un servicio externo
```
investigadores-service: CREATE investigador (OK)
   ↓
carpeta-service: CREATE carpeta    (FALLA - servidor down)
   ↓
Resultado: Investigador existe pero sin carpeta ❌
Necesitamos: Deshacer creación de investigador (rollback)
```

### Solución: Saga Orquestado
```java
SagaOrquestador saga = new SagaOrquestador();

// Step 1: Crear investigador
saga.agregarStep(new SagaStep(
    "crear-investigador",
    () -> investigadorService.crear(inv),                    // Acción
    (e) -> investigadorService.eliminar(inv.getId())         // Compensación
));

// Step 2: Crear carpeta
saga.agregarStep(new SagaStep(
    "crear-carpeta",
    () -> carpetaService.crear(inv.getId()),                 // Acción
    (e) -> carpetaService.eliminar(inv.getId())              // Compensación
));

try {
    saga.ejecutar();  // Ambos steps se ejecutan
} catch (SagaException e) {
    // Si Step 2 falla, automáticamente:
    // 1. No se ejecuta Step 2
    // 2. Se ejecutan compensaciones en LIFO (reverso):
    //    - Eliminar investigador (compensación de Step 1)
    // Resultado: Base de datos en estado consistente ✓
}
```

### Características del SagaOrquestador
1. **Ejecución Secuencial**: Steps se ejecutan en orden
2. **Tracking**: Registra qué steps se ejecutaron correctamente
3. **Compensaciones LIFO**: Si falla, deshace en reverso (Último en ejecutarse, primero en compensar)
4. **Excepción Consistente**: Lanza `SagaException` con contexto

## Servicios en T10

Todos los servicios de T9 (EDA) PLUS:

### investigadores-service (Mejorado)
- **Nuevo**: `CorrelacionIdInterceptor` propaga X-Correlation-ID
- **Nuevo**: `MetricasService` recolecta métricas
- **Nuevo**: `SagaOrquestador` para transacciones distribuidas
- **Config**: `/actuator/prometheus` expone métricas

### publicaciones-service (Mejorado)
- Mismo mejoras que investigadores-service

### consultas-service (Mejorado)
- Mismo mejoras para consistencia

## Configuración T10

### application.yml
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  pattern:
    console: "[%X{X-Correlation-ID}] %d{HH:mm:ss} %logger{36} - %msg%n"
```

### WebMvcConfigurer (Bean)
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CorrelacionIdInterceptor());
    }
}
```

## Tests en T10

**investigadores-service:**
- `SagaOrquestadorTest` (2 tests)
  - testSagaHappyPath: Ambos steps se ejecutan
  - testSagaConFallo: Falla en step 2, se compensa step 1
  
- `MetricasServiceTest` (4 tests)
  - testIncrementarRequestCount: Counter funciona
  - testIncrementarErrorCount: Counter funciona
  - testTimer: Timer registra duración
  - testTimerNull: Manejo de sample nulo

## Flujo End-to-End Mejorado

1. Cliente: `curl -H "X-Correlation-ID: xyz-789" POST /investigadores`
2. investigadores-service recibe, **propaga X-Correlation-ID**
3. Logs: `[xyz-789] Creando investigador Juan`
4. Métricas: `http.requests.total += 1`
5. Si falla: Saga compensa automáticamente
6. Response devuelve `X-Correlation-ID: xyz-789`
7. Cliente puede rastrear todo el flujo con `xyz-789` en logs/tracing

## Monitoreo Prometheus

### Scrape Configuration (en prometheus.yml)
```yaml
scrape_configs:
  - job_name: 'investigadores-service'
    static_configs:
      - targets: ['localhost:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

### Queries Útiles
```promql
# Total de requests en últimos 5 minutos
rate(http_requests_total[5m])

# Error rate
rate(http_errors_total[5m]) / rate(http_requests_total[5m])

# P95 de latencia
histogram_quantile(0.95, http_requests_duration_seconds)
```

## Próximas Fases

- **T11**: Polyglot Persistence con Redis, sincronización multi-BD
- **T12-T13**: ADRs documentando decisiones arquitectónicas

## Decisiones Arquitectónicas

### X-Correlation-ID en Headers
- Simple de implementar
- Funciona con logging tradicional (ELK Stack)
- Compatible con distributed tracing (Jaeger, Zipkin)
- Alternativa futura: OpenTelemetry para auto-instrumentation

### Saga Orquestado (vs. Coreografía)
- **Orquestado**: Un servicio central coordina transacciones
- **Coreografía**: Cada servicio escucha eventos y actúa
- T10 usa **Orquestado** (más simple, fácil de entender)
- Riesgo: Single point of failure en orquestador
- Mitigación: Réplicas con estado compartido (T11+)

### Métricas con Micrometer
- Agnóstico de backend (Prometheus, CloudWatch, Datadog)
- Integrado con Spring Boot Actuator
- Estándar de la industria

## Troubleshooting

| Problema | Solución |
|----------|----------|
| X-Correlation-ID no propaga | Verificar interceptor en WebConfig |
| Métricas no aparecen | Visitar `/actuator/metrics` primero para activarlas |
| Saga no compensa | Revisar logs, verificar excepciones en compensación |

## Referencias
- Saga Pattern: https://microservices.io/patterns/data/saga.html
- Micrometer: https://micrometer.io/
- Prometheus: https://prometheus.io/
- Distributed Tracing: https://opentelemetry.io/
