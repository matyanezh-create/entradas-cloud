-- Ejecutar como EVENTOS_APP en Oracle Autonomous Database.
CREATE SEQUENCE evento_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE eventos (
 id NUMBER(19) PRIMARY KEY,
 nombre VARCHAR2(150 CHAR) NOT NULL,
 fecha TIMESTAMP(6) WITH TIME ZONE NOT NULL,
 precio NUMBER(12,2) NOT NULL CHECK(precio > 0),
 activo NUMBER(1) DEFAULT 1 NOT NULL CHECK(activo IN (0,1))
);
CREATE INDEX idx_eventos_disponibles ON eventos(activo,fecha);

