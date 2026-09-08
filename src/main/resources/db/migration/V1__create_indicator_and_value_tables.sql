-- V1: Baseline for IndicadoresAR
-- Creates indicator catalog and historical values with idempotent constraint

CREATE TABLE indicator (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    unit        VARCHAR(50)  NOT NULL,
    source      VARCHAR(20)  NOT NULL CHECK (source IN ('BCRA', 'INDEC')),
    frequency   VARCHAR(20)  NOT NULL CHECK (frequency IN ('DIARIA', 'MENSUAL')),
    description TEXT
);

CREATE TABLE indicator_value (
    id           BIGSERIAL PRIMARY KEY,
    indicator_id BIGINT NOT NULL REFERENCES indicator(id) ON DELETE CASCADE,
    date         DATE NOT NULL,
    value        NUMERIC(18,4) NOT NULL,
    ingested_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_indicator_value_indicator_date UNIQUE (indicator_id, date)
);

CREATE INDEX idx_indicator_value_indicator_date ON indicator_value (indicator_id, date);
CREATE INDEX idx_indicator_value_date ON indicator_value (date);

-- Seed catalog (stable business keys)
INSERT INTO indicator (code, name, unit, source, frequency, description) VALUES
    ('DOLAR_OFICIAL', 'Dolar Oficial', 'ARS', 'BCRA', 'DIARIA', 'Tipo de cambio oficial peso-dolar informado por el BCRA'),
    ('TASA_POLITICA_MONETARIA', 'Tasa de Politica Monetaria', '%', 'BCRA', 'DIARIA', 'Tasa de interes de referencia del BCRA'),
    ('RESERVAS_INTERNACIONALES', 'Reservas Internacionales', 'USD millones', 'BCRA', 'DIARIA', 'Reservas internacionales del BCRA'),
    ('IPC_NACIONAL', 'Indice de Precios al Consumidor Nacional', 'indice', 'INDEC', 'MENSUAL', 'IPC nivel general nacional del INDEC')
ON CONFLICT (code) DO NOTHING;
