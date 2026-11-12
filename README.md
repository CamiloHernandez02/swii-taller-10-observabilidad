# Taller Lección 10: Observabilidad + Saga Pattern

Ingeniería de Software II (UPTC). Agregar capacidades de observabilidad y orquestar transacciones distribuidas usando Saga Pattern.

## Objetivo

Implementar observabilidad completa (logging, métricas, trazas distribuidas) y manejar transacciones distribuidas usando Saga pattern (orquestado o coreografía).

## Componentes

- Logging centralizado (ELK o similar)
- Métricas (Prometheus/Micrometer)
- Trazas distribuidas (Jaeger/Zipkin)
- Saga orchestrator para transacciones distribuidas

## Requisitos

- Instrumentar todos los servicios
- Correlacionar logs con trace IDs
- Implementar una Saga de compensación
- Dashboards de monitoreo

## Rama: `taller-observabilidad`
