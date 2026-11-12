# Notas de Implementación - Taller Lección 10: Observabilidad + Saga

## Observabilidad (3 pilares)

1. **Logs**: Elasticsearch/Logstash/Kibana (ELK) o centralizado
2. **Métricas**: Prometheus + Grafana (Spring Boot Actuator)
3. **Trazas**: Jaeger/Zipkin (Spring Cloud Sleuth)

Correlation ID en todos los logs.

## Saga Pattern

Transacción distribuida con compensación:

```
Crear Investigador
  ↓ evento
Crear Publicación
  ↓ evento
Si falla: ejecutar compensaciones (rollback)
```

Puede ser:
- Orchestrated: servicio central coordina pasos
- Choreography: eventos disparan siguientes pasos

## Componentes

- Spring Cloud Sleuth para trazas
- Micrometer para métricas
- ELK o Loki para logs
- Saga coordinator o event listeners

## Entrega

- Branch: `taller-observabilidad`
