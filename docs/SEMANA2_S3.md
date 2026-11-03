# Resúmenes de compras en S3

Esta entrega extiende el sistema de eventos y compras de semana 1. Genera el resumen a partir de la venta persistida y guarda cada archivo en una carpeta cuyo nombre corresponde al número de resumen. La demostración usa Amazon S3 real con bucket privado, cifrado SSE-S3 y versionado.

## Iniciar la solución

Requisitos: Java 17 y credenciales temporales vigentes del laboratorio AWS Academy. Maven Wrapper permite compilar el código. El comprimido descargable incluye los dos JAR en `dist/`; en GitHub los JAR se obtienen compilando.

1. Extraer el ZIP en una carpeta propia.
2. Compilar con `.\mvnw.cmd -B -ntp verify` si se trabaja desde GitHub. Si se usa el ZIP, se puede ejecutar directamente desde `dist/`.
3. Abrir PowerShell en la raíz del proyecto y definir las variables del laboratorio. No incluirlas en Git ni mostrarlas en la grabación:

```powershell
$env:S3_BUCKET = 'cdy2204-resumenes-485691189324-s2'
$env:AWS_REGION = 'us-east-1'
$env:AWS_ACCESS_KEY_ID = 'VALOR_ACTUAL_DE_AWS_DETAILS'
$env:AWS_SECRET_ACCESS_KEY = 'VALOR_ACTUAL_DE_AWS_DETAILS'
$env:AWS_SESSION_TOKEN = 'VALOR_ACTUAL_DE_AWS_DETAILS'
java -jar dist/compras-service-1.0.0.jar --server.address=127.0.0.1
```

4. En otra terminal, desde la misma raíz:

```powershell
java -jar dist/eventos-service-1.0.0.jar --server.address=127.0.0.1
```

Para ejecutar lo compilado localmente, reemplazar `dist/` por `compras-service/target/` o `eventos-service/target/`, según corresponda. Los puertos predeterminados son 8082 para compras y 8081 para eventos. Las bases H2 locales se guardan en `data/`; los archivos físicos en `resumenes-locales/`. Si un puerto está ocupado, detener únicamente la instancia propia anterior o cambiar ambos puertos y las variables de Postman, además de EVENTOS_BASE_URL.

## Bucket y permisos

Bucket de la actividad: `cdy2204-resumenes-485691189324-s2`, región `us-east-1`. Se activaron las cuatro opciones de bloqueo público, Object Ownership BucketOwnerEnforced, cifrado AES256 gestionado por S3 y versionado. La política del bucket rechaza conexiones sin HTTPS. No se necesita hacer público el bucket para utilizar el SDK.

La identidad de ejecución necesita ListBucket en el bucket y GetObject, PutObject y DeleteObject en sus objetos. Se incluye una política de referencia en `infra/iam-policy.json`; sustituir el marcador por el nombre real si se utiliza otra cuenta o identidad. AWS Academy puede restringir la creación de identidades y políticas; usar las credenciales proporcionadas por el laboratorio.

Con versionado, DELETE elimina la versión vigente mediante un marcador. Se conservan las versiones anteriores y se pueden recuperar desde S3. El ejercicio no purga el historial. La región y las tres credenciales deben actualizarse cuando cambie o expire la sesión del laboratorio.

## API integrada

| Operación | Método y ruta | Respuesta correcta |
|---|---|---|
| Registrar venta | POST `/api/compras` con eventoId y cantidad | 201 y compra guardada |
| Generar TXT físico | POST `/api/compras/{numero}/resumen/generar` | 201, TXT adjunto y copia local |
| Subir TXT | POST `/api/compras/{numero}/resumen/archivo` | 201 y clave S3 |
| Consultar metadatos | GET `/api/compras/{numero}/resumen` | 200, tamaño, ETag y fecha |
| Descargar | GET `/api/compras/{numero}/resumen/archivo` | 200 y adjunto TXT |
| Reemplazar | PUT `/api/compras/{numero}/resumen/archivo` | 200, misma clave |
| Eliminar | DELETE `/api/compras/{numero}/resumen/archivo` | 204 sin cuerpo |

Las cargas y reemplazos usan multipart/form-data con campo `file` de tipo archivo. Se acepta texto UTF-8 de hasta 1 MiB, sin bytes nulos, que comience con `RESUMEN DE COMPRA`, salto de línea, `Numero: {numero}` y otro salto de línea. No enviar una ruta del computador como contenido ni escribir manualmente el encabezado multipart.

Se valida que la compra exista antes de acceder a su archivo. POST no sobrescribe: usa If-None-Match y devuelve 409 si ya hay un objeto. PUT consulta el ETag y escribe con If-Match; devuelve 409 si hubo un cambio concurrente. DELETE consulta existencia antes de borrar; un segundo borrado devuelve 404. La comprobación y el borrado son dos operaciones; no se ofrece bloqueo distribuido ante una escritura simultánea.

Otros errores: 400 para formato o número incorrecto, 413 para tamaño excesivo, 502 para rechazo/fallo del servicio AWS y 503 para conexión o configuración ausente. Si no se define S3_BUCKET, las rutas de semana 1 siguen funcionando y las de S3 indican que falta configuración.

## Generación y correcciones

ResumenService consulta CompraService y utiliza nombre, cantidad, precio, total y fecha de la venta guardada. El precio original procede del catálogo de eventos, y el cálculo con BigDecimal de semana 1 se conserva. El cliente no puede imponer un precio al generar el resumen.

Regenerar sobrescribe la copia local del mismo número con los datos históricos de la compra; no sube ni modifica automáticamente el objeto S3. La escritura se serializa dentro de cada instancia de la aplicación. Un fallo de disco devuelve error y no informa éxito.

El endpoint PUT reemplaza el documento y no altera la venta registrada en la base de datos. La demostración añade una observación de referencia de contacto corregida, conservando cantidad y total. Para modificar los datos comerciales de la venta se necesitaría una funcionalidad adicional de corrección de compras.

## Ejecutar en Postman Desktop

1. Importar `postman/Semana2_S3.postman_collection.json`. Revisar eventosUrl y comprasUrl.
2. Ejecutar 01 y 02. La colección conserva eventoId y compraId. El ejemplo compra tres entradas a 12500.50 CLP, total 37501.50.
3. Ejecutar 03. Guardar la respuesta como `resumen-generado.txt` con Save response to file. Mostrar además `resumenes-locales/resumen-{id}.txt` en el explorador.
4. Crear una copia `resumen-corregido.txt` y añadir al final `Observacion: referencia de contacto corregida`. Mantener encabezado y número.
5. Crear `resumen-numero-incorrecto.txt` con encabezado `RESUMEN DE COMPRA` y número 999999999.
6. En Body → form-data seleccionar el TXT generado en 04 y 05, el corregido en 08 y 14, y el de número incorrecto en 15. El campo se llama file y debe ser de tipo File.
7. Ejecutar 04 a 16 en orden. Verificar las aserciones y la carpeta en la consola S3. Guardar las descargas de 07 y 09 y comprobar la observación agregada. Al terminar, el objeto vigente ya no existe; activar Show versions para ver el historial.

Para automatizar se incluye `scripts/run-s2-postman.cjs`: instalar Newman con `npm install --no-save newman` y ejecutar `node scripts/run-s2-postman.cjs`. Este asistente guarda los archivos después de generar la respuesta, antes de que las solicitudes multipart los utilicen. Crea datos de demostración y borra únicamente el archivo S3 creado en ese recorrido; conserva la venta y el evento para consulta.

## Docker y EC2

Los Compose conservan la base de datos previa y ahora pasan las variables S3 al contenedor de compras. En Docker local, `compras-data` conserva también los resúmenes físicos. Antes de desplegar en EC2 se deben definir las variables S3 en el archivo privado `deploy/.env` del servidor o configurar una identidad de instancia con permisos equivalentes. Las credenciales del runner de GitHub Actions no se transmiten automáticamente al contenedor remoto.

Esta extensión se validó como aplicaciones Java locales conectadas a S3 real. El despliegue EC2 anterior no se convierte en la versión de semana 2 hasta que se despliegue esta rama y se configure S3 en el servidor.

## Referencias

- AWS SDK v2, S3Client: https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html
- AWS SDK v2, credenciales predeterminadas: https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html
- Guía de aprendizaje, instrucciones específicas forma B y pauta formativa CDY2204, experiencia 1, semana 2.
