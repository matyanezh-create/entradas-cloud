# Resultado de la colección Postman con Oracle local

La colección se ejecutó con Newman 6.2.1, el ejecutor de colecciones de Postman, contra los servicios locales conectados a Oracle XE. No se ejecutó desde la interfaz gráfica: Postman mostró un aviso del Firewall de Windows que no se respondió automáticamente.

## Resultado

- Solicitudes ejecutadas: **15**.
- Comprobaciones ejecutadas: **20**.
- Fallos: **0**.
- Base comprobada directamente: **Oracle XE, servicio XEPDB1**.
- Evento de prueba: **1**; compra de prueba: **1**.
- Compra: **3 entradas × 12500.50 = 37501.50**.

## Detalle por solicitud

| Solicitud | HTTP real | Comprobaciones correctas |
| --- | --- | --- |
| 01 Salud eventos | 200 | 2/2 |
| 02 Salud compras | 200 | 1/1 |
| 03 Crear evento | 201 | 1/1 |
| 04 Listar eventos disponibles | 200 | 2/2 |
| 05 Consultar evento | 200 | 1/1 |
| 06 Comprar tres entradas | 201 | 2/2 |
| 07 Consultar compra persistida | 200 | 2/2 |
| 08 Actualizar evento | 200 | 1/1 |
| 09 Compra conserva precio original | 200 | 2/2 |
| 10 Rechazar cantidad cero | 400 | 1/1 |
| 11 Rechazar evento inexistente | 404 | 1/1 |
| 12 Rechazar evento inválido | 400 | 1/1 |
| 13 Eliminar evento | 204 | 1/1 |
| 14 Evento eliminado no comprable | 404 | 1/1 |
| 15 Compra se conserva tras eliminación | 200 | 1/1 |

## Persistencia comprobada en Oracle

Después de ejecutar la colección se consultaron las tablas con sus propios usuarios. El evento está dado de baja lógicamente (ACTIVO = 0) y su precio actualizado es 15000. La compra conserva el precio original 12500.50, la cantidad 3 y el total 37501.50.

La baja lógica del evento fue parte de la colección y afecta solo al registro creado para esta ejecución. La compra permanece disponible en GET http://localhost:8082/api/compras/1.

La consulta directa a Oracle confirma filas persistidas. En esta ejecución no se reiniciaron los microservicios ni Oracle; la supervivencia a reinicios de Oracle y el despliegue en nube no se presentan como comprobados.

## Evidencia reproducible

- postman-oracle-local.json: informe completo de Newman.
- postman-oracle-local.xml: resultados JUnit.
- oracle-consulta-directa.json: valores leídos directamente desde Oracle por JDBC.

Los informes no incluyen contraseñas ni el archivo privado de credenciales.

Fecha de consulta Oracle: 2026-10-26T21:24:50.002555600-03:00

Documentación de Newman: https://learning.postman.com/docs/collections/using-newman-cli/installing-running-newman
