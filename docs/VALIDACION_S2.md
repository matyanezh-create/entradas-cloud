# Validación de semana 2

Validación realizada el 2 de noviembre de 2026 con Java 17 y Spring Boot 3.5.16.

## Compilación y pruebas automatizadas

`mvn -B -ntp verify` finalizó con BUILD SUCCESS. Se ejecutaron 40 pruebas sin fallos ni errores: 7 en eventos-service y 33 en compras-service. Se conservaron las pruebas de semana 1 y se añadieron 19 para archivos y S3.

Las pruebas nuevas verifican la generación desde una compra persistida, el archivo físico, la regeneración, validación de contenido y número, jerarquía de claves, creación condicional, reemplazo con ETag, consulta, descarga, eliminación, objetos ausentes y conflictos.

## Recorrido con AWS real

Se iniciaron ambos JAR localmente en los puertos 18081 y 18082 y se ejecutó la colección Postman mediante Newman contra el bucket real `cdy2204-resumenes-485691189324-s2`, en us-east-1.

Resultado: 16 solicitudes, 24 aserciones, 0 fallos. La base de datos de este recorrido fue H2 temporal en memoria; el almacenamiento de archivos fue Amazon S3 real. El TXT físico se verificó en disco. El perfil local normal conserva la base H2 en archivo; la persistencia EC2 de semana 1 tiene sus propias evidencias históricas.

| Paso | Estado comprobado |
|---|---|
| Crear evento y registrar compra | 201 y total 37501.50 |
| Generar resumen | 201, TXT físico y adjunto |
| Subir a carpeta del número | 201 y clave `1/resumen-1.txt` |
| Repetir carga | 409 |
| Consultar y descargar | 200 y contenido idéntico al generado |
| Reemplazar y volver a descargar | 200 y observación corregida visible |
| Eliminar | 204 |
| Consultar, descargar, borrar y reemplazar el eliminado | 404 |
| Cargar documento con número incorrecto | 400 |
| Generar desde compra inexistente | 404 |

Una consulta independiente mediante AWS SDK para Python confirmó versiones del objeto y un marcador de eliminación vigente. Al finalizar había cero objetos vigentes en el bucket; las versiones históricas quedaron conservadas. El bucket mantiene las cuatro opciones de bloqueo público, SSE-S3 AES256 y versionado.

Los resultados resumidos de Newman y la configuración S3 están en `docs/evidencias/`. Los ejemplos de TXT corresponden a datos de demostración. Los procesos Java de prueba se detuvieron al finalizar.

## Presentación pendiente

La integración y el recorrido AWS están comprobados. Falta grabar en Teams la presentación de los integrantes, demostrar las operaciones en Postman en tiempo real y compartir el enlace con permisos para el docente. El guion está en VIDEO_SEMANA2.md. No se generó una grabación ficticia ni se sustituyó por las pruebas automáticas.

Esta versión se publica en una rama de semana 2; no se presenta como un despliegue de esa extensión en EC2 ni como una modificación ya integrada en main.
