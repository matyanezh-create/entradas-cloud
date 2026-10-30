-- Ejecutar como COMPRAS_APP. No requiere acceso al esquema EVENTOS_APP.
CREATE SEQUENCE compra_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE TABLE compras (
 id NUMBER(19) PRIMARY KEY,
 evento_id NUMBER(19) NOT NULL,
 evento VARCHAR2(150 CHAR) NOT NULL,
 cantidad NUMBER(10) NOT NULL CHECK(cantidad BETWEEN 1 AND 1000),
 precio_unitario NUMBER(12,2) NOT NULL CHECK(precio_unitario > 0),
 precio_total NUMBER(16,2) NOT NULL,
 fecha_compra TIMESTAMP(6) WITH TIME ZONE NOT NULL,
 CONSTRAINT ck_compra_total CHECK(precio_total=precio_unitario*cantidad)
);

