# Verificación de la entrega

Comprobaciones realizadas localmente el 24 de octubre de 2026, con Java 17 y Maven 3.9.9.

## Resultado comprobado

- Compilación y empaquetado de ambos microservicios: BUILD SUCCESS.
- Suite automatizada: 21 pruebas, cero fallos, cero errores y cero omitidas.
- Eventos: siete pruebas de API, persistencia, actualización, baja lógica, entradas inválidas, fecha pasada y listado.
- Compras: ocho pruebas de API, cálculo exacto, precio histórico, validación, errores y ausencia de escrituras ante fallos.
- Cliente HTTP de eventos: seis pruebas con servidor HTTP real de prueba para respuestas válidas, 404, 500, evento vencido, evento inactivo y respuesta incompleta.
- Ejecución conjunta de ambos JAR por HTTP: creación de evento, compra de tres entradas, total 37501.50 y recuperación de compra.
- Conservación del nombre y precio original después de actualizar el catálogo.
- Persistencia de la compra después de detener y volver a iniciar el servicio de compras con H2 en archivo.
- Consulta de la compra después de dar de baja el evento.
- Validación de las configuraciones de Compose local y Oracle sin iniciar contenedores; la de Oracle usa valores ficticios solo para comprobar sintaxis.

La comprobación de reinicio usa archivos nuevos y aislados dentro de la carpeta de trabajo. No contiene datos de evaluación ni conexiones a Oracle. Ambos procesos se detuvieron al finalizar. Los reportes Surefire pueden reproducirse ejecutando mvn verify.

## Pendiente de comprobar con las cuentas del estudiante

- Conexión real a Oracle Cloud con wallet y los dos esquemas.
- Ejecución de los scripts SQL en Oracle y validación Hibernate contra esa base.
- Construcción y ejecución de imágenes Docker; el motor Docker de este equipo estaba apagado.
- Ejecución del workflow en GitHub Actions y sintaxis Bash en su runner Linux. Se incluye bash -n en CI; el shell MSYS local no pudo iniciarse bajo las restricciones de este entorno.
- Publicación en Docker Hub y despliegue en una instancia EC2.
- Permisos IAM, reglas de red y limpieza de acceso SSH temporal.
- Reversión a las imágenes anteriores ante un despliegue fallido.
- Ejecución interactiva de Postman en EC2; se entrega la colección preparada, no una captura de resultados ficticia.
- Grabación real en Teams, enlaces definitivos y acceso del docente.

El código y los archivos de configuración están preparados; las verificaciones locales no demuestran por sí solas un despliegue exitoso en la nube.



## Preparación de Oracle XE local el 26 de octubre de 2026

Se detectaron Oracle XE 21c, SQL Plus y los servicios OracleServiceXE y OracleOraDB21Home1TNSListener. El puerto TCP 1521 respondió. El intento de conexión administrativa por autenticación del sistema operativo devolvió ORA-12560; no se ejecutó DDL ni se crearon usuarios.

Se añadieron un instalador SQL interactivo para los esquemas y dos asistentes PowerShell. Ambos scripts PowerShell pasaron la revisión de sintaxis sin ejecutar las acciones de configuración. Las 21 pruebas anteriores corresponden a la versión Java verificada el 24 de octubre; esta actualización no cambia el código Java. La conexión y persistencia reales en Oracle XE siguen pendientes de las credenciales introducidas localmente.


## Prueba real de Oracle local y colección Postman

Se ejecutó la colección con Newman 6.2.1: 15 solicitudes y 20 comprobaciones sin fallos. Se consultaron directamente ambas tablas por JDBC: la compra 1 conserva cantidad 3, precio unitario 12500.50 y total 37501.50. El evento 1 quedó dado de baja por el caso de prueba. Ver evidencias/RESULTADO_POSTMAN_ORACLE.md y los reportes adjuntos. Esta evidencia actualiza las notas históricas de preparación pendientes de Oracle XE; no acredita Oracle Cloud ni ejecución gráfica en Postman. No se reiniciaron servicios en esta comprobación.

## Preparacion real en EC2 el 30 de octubre de 2026

Se recompilaron los dos modulos con el nuevo driver PostgreSQL y las 21 pruebas pasaron. Se instalaron Docker y Compose en EC2, se construyeron imagenes y se levantaron ambos servicios con PostgreSQL 17. La coleccion Newman paso 15 solicitudes y 20 comprobaciones por un tunel SSH. Se reinicio PostgreSQL y se recrearon los microservicios, conservandose la compra 1. Ver evidencias/RESULTADO_EC2_PREPARACION.md. Pendiente: acceso HTTP directo a la IP publica, GitHub Actions, publicacion Docker Hub y video. La aclaracion del docente permite cualquier base relacional; Oracle Cloud deja de ser requisito de esta implementacion.
