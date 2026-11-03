# Presentación en Teams de la semana 2

Grabar una reunión con el compañero y demostrar la solución real con Postman y la consola AWS. Duración sugerida: 8 a 10 minutos. Ocultar credenciales antes de compartir pantalla.

| Tiempo aproximado | Mostrar y explicar | Criterio de la pauta |
|---|---|---|
| 0 a 1 min | Dos microservicios de semana 1 y nuevas clases de archivos dentro de compras | Continuidad de la solución |
| 1 a 2 min | Crear evento y compra, generar TXT, abrir archivo físico y explicar total | Generación de archivos |
| 2 a 3 min | POST multipart, estado 201, clave `{id}/resumen-{id}.txt` y carpeta en S3 | Carga y jerarquía |
| 3 a 4 min | Consultar metadatos y guardar descarga original | Consulta y descarga |
| 4 a 6 min | Mostrar observación corregida, PUT, descarga modificada y misma clave | Actualización de existentes |
| 6 a 7 min | DELETE 204, segundo borrado 404 y ausencia de objeto vigente en consola | Eliminación y existencia |
| 7 a 9 min | Mostrar pruebas, carga duplicada 409, número incorrecto 400 y versionado | Validaciones y explicación completa |

Explicar: el número de resumen corresponde a la compra persistida; los archivos son objetos con prefijo, no carpetas físicas; el bucket es privado; el SDK utiliza credenciales temporales; reemplazar el TXT no cambia la venta; con versionado, borrar deja un marcador recuperable.

Antes de enviar al AVA, comprobar que el video muestra todas las operaciones en tiempo real, que ambos integrantes participan, que el docente puede abrir la grabación y que los enlaces del repositorio y del video son reales. Adjuntar el ZIP con fuente y documentación. La grabación no puede sustituirse por los resultados automáticos de Newman ni por este guion.
