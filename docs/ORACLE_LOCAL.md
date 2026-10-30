# Usar Oracle Database XE en Windows

Se localizó Oracle Database XE 21c en este equipo y el puerto 1521 responde. La conexión administrativa automática devolvió ORA-12560. Todavía no se han creado usuarios ni probado compras contra esta base.

El perfil oracle ya admite JDBC directo sin wallet. La ruta detectada es C:\app\Moonlabpc\product\21c\dbhomeXE.

## 1 Comprobar Oracle

Detén las ejecuciones H2 o contenedores que ocupen 8081 y 8082, sin borrar sus datos.

Si Oracle no responde, revisa OracleServiceXE y OracleOraDB21Home1TNSListener en Servicios de Windows. Ambos están registrados en este equipo. Para iniciarlos usa PowerShell como administrador:

~~~powershell
Start-Service -Name OracleServiceXE
Start-Service -Name OracleOraDB21Home1TNSListener
~~~

Intenta conectar como SYSTEM al servicio XEPDB1 con la contraseña definida al instalar Oracle. No hace falta reinstalar ni cambiar contraseñas para comenzar.

## 2 Crear los esquemas

Abre PowerShell normal en la raíz del proyecto:

~~~powershell
powershell -ExecutionPolicy Bypass -File scripts/configurar-oracle-local.ps1
~~~

Si sqlplus no está en PATH:

~~~powershell
powershell -ExecutionPolicy Bypass -File scripts/configurar-oracle-local.ps1 -SqlPlusCommand "C:\app\Moonlabpc\product\21c\dbhomeXE\bin\sqlplus.exe"
~~~

El asistente muestra SYSTEM@//localhost:1521/XEPDB1. Introduce la contraseña nueva de SYSTEM cuando SQL Plus la solicite; ya no hay que escribir una conexión ni pulsar Enter para aceptarla. El asistente comprueba el contenedor y los usuarios existentes. Después pide dos contraseñas nuevas ocultas, crea EVENTOS_APP y COMPRAS_APP con privilegios mínimos y ejecuta las tablas y secuencias de cada esquema.

Elige contraseñas distintas de 16 a 30 caracteres, solo con letras, números y guion bajo y empezando por una letra. No actives SPOOL ni guardes contraseñas dentro de los archivos.

Se usa el tablespace USERS de Oracle XE. Si tu base se configuró con otro tablespace, revísalo antes. Si un usuario ya existe, el asistente se detiene sin borrarlo. Los DDL no se deshacen automáticamente: si falla a mitad de instalación, revisa qué objetos se crearon antes de repetir.

Si no recuerdas la contraseña de SYSTEM, avísame para revisar la recuperación autorizada; no la adivines ni reinstales la base.

## 3 Arrancar contra Oracle

Desde la raíz del proyecto:

~~~powershell
powershell -ExecutionPolicy Bypass -File scripts/run-oracle-local.ps1
~~~

El script compila y ejecuta las pruebas antes de iniciar. Introduce las dos contraseñas nuevas cuando aparezcan las solicitudes de credenciales. Mantén los nombres EVENTOS_APP y COMPRAS_APP. Las contraseñas se pasan a los procesos por variables de entorno, sin guardarlas en archivos ni incluirlas en la línea de comandos.

Si esta versión ya está compilada, puedes usar -SkipBuild. Para otra conexión usa -DatabaseService, -DatabasePort y -DatabaseHost. La URL predeterminada es jdbc:oracle:thin:@//localhost:1521/XEPDB1. La aplicación valida el esquema; no crea las tablas al iniciar.

Los logs terminan en -oracle.log y -oracle-error.log. El script muestra los PID para detener los servicios.

## 4 Verificar la persistencia real

1. Espera a que ambos endpoints de salud respondan UP.
2. Ejecuta la colección de postman con sus URLs localhost.
3. En SQL Developer conecta como EVENTOS_APP a localhost, puerto 1521, nombre de servicio XEPDB1. Consulta SELECT * FROM eventos.
4. Conecta como COMPRAS_APP y consulta SELECT * FROM compras.
5. Conserva el ID de compra, detén los microservicios y vuelve a iniciarlos con -SkipBuild.
6. Consulta la compra por ID en Postman y en Oracle. Guarda la evidencia real.

La comprobación con Oracle seguirá pendiente hasta verificar estas operaciones en esta base.

## Alcance para la entrega

Oracle local permite practicar y demostrar persistencia real en Oracle Database. El docente autorizo cualquier motor relacional. La version desplegada en EC2 usa PostgreSQL; esta guia conserva la opcion Oracle local.

Al desplegar en EC2, localhost apunta a EC2 y no a tu PC. Esta configuración no hace accesible la base Windows desde AWS. Para esa fase se necesita una base accesible desde EC2 y la arquitectura aprobada para la evaluación.

Documentación oficial:

- https://docs.oracle.com/en/database/oracle/oracle-database/21/xeinw/connecting-oracle-database-xe.html
- https://docs.oracle.com/en/database/oracle/oracle-database/21/xeinw/starting-and-stopping-oracle-database-xe.html


## Si Oracle rechaza las credenciales guardadas

Cuando los servicios se cierran con ORA-01017, se puede sincronizar exclusivamente la contrasena de los dos usuarios de aplicacion con el archivo privado generado. No crea ni borra tablas. Ejecuta desde la raiz, reemplazando la ruta por la de tu archivo privado:

~~~powershell
powershell -ExecutionPolicy Bypass -File scripts/sincronizar-credenciales-oracle.ps1 -CredentialFile "C:\ruta\privada\oracle-local-credenciales.txt" -IniciarServicios
~~~

Introduce solo la contrasena de SYSTEM. El asistente lee las otras del archivo y elimina su archivo SQL temporal al finalizar. No hagas capturas de la salida si ocurre un error en ALTER USER, pues SQL Plus puede incluir la sentencia fallida. No adjuntes el archivo privado en la entrega. El arranque ahora espera la salud UP y detecta procesos que terminan antes.
