#!/usr/bin/env bash
set -Eeuo pipefail
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  --set=eventos_password="$EVENTOS_DB_PASSWORD" --set=compras_password="$COMPRAS_DB_PASSWORD" <<'SQL'
CREATE ROLE eventos_app LOGIN PASSWORD :'eventos_password';
CREATE ROLE compras_app LOGIN PASSWORD :'compras_password';
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
CREATE SCHEMA eventos AUTHORIZATION eventos_app;
CREATE SCHEMA compras AUTHORIZATION compras_app;
SET ROLE eventos_app;
SET search_path TO eventos;
CREATE SEQUENCE evento_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE eventos (
 id BIGINT PRIMARY KEY,
 nombre VARCHAR(150) NOT NULL,
 fecha TIMESTAMP WITH TIME ZONE NOT NULL,
 precio NUMERIC(12,2) NOT NULL CHECK(precio > 0),
 activo INTEGER DEFAULT 1 NOT NULL CHECK(activo IN (0,1))
);
CREATE INDEX idx_eventos_disponibles ON eventos(activo,fecha);
RESET ROLE;
SET ROLE compras_app;
SET search_path TO compras;
CREATE SEQUENCE compra_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE compras (
 id BIGINT PRIMARY KEY,
 evento_id BIGINT NOT NULL,
 evento VARCHAR(150) NOT NULL,
 cantidad INTEGER NOT NULL CHECK(cantidad BETWEEN 1 AND 1000),
 precio_unitario NUMERIC(12,2) NOT NULL CHECK(precio_unitario > 0),
 precio_total NUMERIC(16,2) NOT NULL,
 fecha_compra TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT ck_compra_total CHECK(precio_total=precio_unitario*cantidad)
);
SQL
