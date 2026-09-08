# Architecture - IndicadoresAR

## Overview

Monolithic Spring Boot application (single deployable) serving both REST API and server-side rendered dashboard via Thymeleaf.

## Modules (feature-first)

Each feature groups its entity, repository, service and controller:

- `indicators/` - Indicator catalog (code, name, unit, source, frequency)
- `values/` - Historical values (IndicatorValue)
- `ingestion/bcra/` - HTTP client + Spring Batch Job for BCRA (dolar, tasas, reservas)
- `ingestion/indec/` - HTTP client + Spring Batch Job for INDEC (IPC)
- `monitoring/` - Job execution status, manual triggers, retries
- `dashboard/` - Thymeleaf controllers serving views
- `common/health/` - Liveness check

## Data Model

- `indicator` (BIGSERIAL PK, code UNIQUE, name, unit, source BCRA|INDEC, frequency DIARIA|MENSUAL, description)
- `indicator_value` (BIGSERIAL PK, indicator_id FK, date DATE, value NUMERIC(18,4), ingested_at TIMESTAMPTZ, UNIQUE(indicator_id, date)) -> enables idempotent upsert
- `BATCH_*` tables managed by Spring Batch (job instances, executions, step executions)

## Configuration

- `application.yml` - base config (datasource via env, JPA validate, Flyway, Batch)
- `application-dev.yml` - local overrides (thymeleaf cache off, show-sql)
- `application-test.yml` - Testcontainers datasource, Flyway enabled

## Decisions

- SQL seed for Indicator catalog in Flyway migration (stable business keys).
- Single project, no separate frontend - Thymeleaf + Chart.js via CDN.
- Flyway for versioned migrations, Hibernate `validate` only.
- Testcontainers for integration tests with real PostgreSQL.
