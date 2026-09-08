# IndicadoresAR

Dashboard de indicadores economicos argentinos (dolar oficial, tasas y reservas del BCRA + IPC del INDEC). ETL idempotente con Spring Batch sobre APIs publicas oficiales, PostgreSQL/Flyway y visualizacion Thymeleaf + Chart.js. Java 25 + Spring Boot 4.

## Stack

Java 25, Spring Boot 4.0.8, Spring Batch, Spring Data JPA, Thymeleaf, Chart.js, PostgreSQL 16, Flyway, Testcontainers, Docker Compose.

## Requisitos

- Java 25 (Temurin)
- Docker + Docker Compose
- Maven 3.9+ (o `./mvnw` si se agrega wrapper)

## Desarrollo local

```bash
docker compose up --build
# App: http://localhost:8080
# Health: http://localhost:8080/api/health
# DB: localhost:5432 (indicadoresar / indicadoresar)
```

Sin Docker:

```bash
# Levantar solo Postgres
docker compose up db -d
# Correr app
mvn spring-boot:run
```

## Tests

```bash
mvn test
```

Usa Testcontainers (requiere Docker corriendo) para tests de integracion con PostgreSQL real.

## Estructura

```
src/main/java/com/indicadoresar/
  indicators/   # Catalogo de indicadores
  values/       # Valores historicos
  ingestion/bcra|indec  # Jobs Spring Batch
  monitoring/   # Estado de jobs
  dashboard/    # Controllers Thymeleaf
  common/health # Health check
```

## Migraciones

Flyway en `src/main/resources/db/migration/`. Spring Batch crea sus tablas `BATCH_*` automaticamente (`spring.batch.jdbc.initialize-schema=always`).
