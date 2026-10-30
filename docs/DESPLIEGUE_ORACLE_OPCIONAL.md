# Alternativa opcional con Oracle Cloud

La aclaracion del docente permite cualquier motor relacional. Esta guia conserva una ruta alternativa; el despliegue actual usa PostgreSQL en EC2 y se describe en DESPLIEGUE.md.

# Despliegue en Oracle Cloud y AWS EC2

Esta guía requiere cuentas propias o del laboratorio. El proyecto no contiene credenciales. Configura los valores directamente en Oracle, EC2 y GitHub; no los pegues en el chat.

## 1 Oracle Autonomous Database

1. Inicia o crea la base Autonomous del laboratorio con acceso de red que permita conectarse desde EC2. Si usas lista de acceso, agrega la IP de salida de EC2. El laboratorio debe permanecer activo durante la presentación.
2. Descarga el wallet de conexión mTLS y descomprímelo. Revisa tnsnames.ora para elegir el alias de conexión real, por ejemplo el terminado en _high. No modifiques certificados ni subas el wallet al repositorio.
3. Desde Database Actions con ADMIN, crea dos usuarios: EVENTOS_APP y COMPRAS_APP. Asigna contraseñas distintas y conserva sus valores de forma privada.
4. Concede a cada usuario CREATE SESSION, CREATE TABLE y CREATE SEQUENCE; asigna una cuota suficiente en DATA, por ejemplo 20 MB. No concedas privilegios de ADMIN a las aplicaciones.
5. Conéctate como EVENTOS_APP y ejecuta database/01_eventos.sql.
6. Conéctate como COMPRAS_APP y ejecuta database/02_compras.sql.

Ejemplo de permisos que ADMIN puede conceder después de crear los usuarios:

```sql
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO EVENTOS_APP;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO COMPRAS_APP;
ALTER USER EVENTOS_APP QUOTA 20M ON DATA;
ALTER USER COMPRAS_APP QUOTA 20M ON DATA;
```

Los scripts no son idempotentes: se ejecutan una sola vez en un esquema vacío. Antes de volver a ejecutarlos revisa los objetos existentes. El proyecto usa ddl-auto validate; si las tablas no coinciden, el servicio falla al arrancar. No cambies a create para arreglarlo, porque destruiría información.

## 2 Instancia EC2

Usa Ubuntu Server 24.04 LTS x86_64 y una instancia permitida por tu laboratorio. Se recomiendan al menos 2 GB de RAM para ambos contenedores y el sistema operativo. El usuario del ejemplo es ubuntu. Conserva el archivo PEM de la clave fuera del proyecto.

Crea un grupo de seguridad dedicado a esta instancia:

- Puerto 22: tu IP /32 para la preparación. El workflow agrega temporalmente la IP /32 del runner.
- Puerto 8081 y 8082: tu IP /32 y las IP de quienes harán la evaluación.
- Salida: permitir acceso al registro Docker, a Oracle por los destinos y puertos del wallet y a los servicios requeridos. En los ejemplos mTLS de Oracle se usa habitualmente 1522; revisa el descriptor real.

No uses esta API sin autenticación como servicio público permanente. Si una red institucional cambia de IP, actualiza la regla correspondiente antes del video.

En la instancia instala Docker Engine y el plugin Compose siguiendo la documentación oficial de Ubuntu/Docker. Como alternativa de laboratorio, los paquetes de Ubuntu pueden instalarse con:

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2 curl unzip
sudo systemctl enable --now docker
sudo usermod -aG docker ubuntu
```

Cierra la sesión SSH y vuelve a entrar para aplicar el grupo docker. Verifica:

```bash
docker version
docker compose version
curl --version
flock --version
mkdir -p ~/entradas-cloud/deploy/wallet
```

El workflow compila las imágenes en GitHub, no en EC2. Comprueba que el volumen de disco permita descargar dos imágenes y conservar una versión anterior.

## 3 Variables y wallet en EC2

Copia deploy/.env.example a ~/entradas-cloud/deploy/.env. Reemplaza DB_URL por el alias real:

```text
DB_URL=jdbc:oracle:thin:@TU_ALIAS_high?TNS_ADMIN=/app/wallet
```

Configura los nombres de usuario y contraseñas de ambos esquemas. No declares EVENTOS_IMAGE ni COMPRAS_IMAGE: el script las obtiene del commit. Para contraseñas con caracteres especiales usa comillas simples compatibles con Compose.

Copia los archivos descomprimidos del wallet a ~/entradas-cloud/deploy/wallet. No debe quedar una carpeta extra entre wallet y tnsnames.ora. El contenedor corre con UID/GID 10001; permite a ese grupo leer el wallet:

```bash
cd ~/entradas-cloud/deploy
chmod 600 .env
sudo chgrp -R 10001 wallet
find wallet -type d -exec chmod 750 {} \;
find wallet -type f -exec chmod 640 {} \;
```

El .env lo lee Compose desde la sesión de ubuntu; las variables necesarias se pasan al contenedor. La aplicación monta el wallet en modo lectura en /app/wallet.

## 4 Docker Hub

Crea dos repositorios PUBLICOS: entradas-eventos y entradas-compras. El script de EC2 descarga imágenes públicas sin almacenar un token Docker Hub en la instancia. Si deben ser privados, configura docker login de forma segura en EC2 antes de usar este diseño.

Crea un token Docker Hub con permiso de escritura para GitHub Actions. Usa tu nombre de usuario Docker Hub en minúsculas. El workflow etiqueta ambas imágenes con el SHA del commit.

## 5 Repositorio GitHub

Crea un repositorio vacío y sube el contenido de esta carpeta como raíz. Deben quedar pom.xml, Dockerfile y .github directamente en la raíz, no dentro de otra carpeta adicional. No subas target, data, logs, .env, wallet ni PEM.

Puedes usar GitHub Desktop para agregar la carpeta o Git desde esta raíz:

```bash
git init -b main
git add .
git commit -m "Implementar eventos compras y despliegue continuo"
git remote add origin URL_REAL_DEL_REPOSITORIO
git push -u origin main
```

Antes del primer push, configura los secretos para que el workflow pueda completar el despliegue; si ya hiciste push, repite la ejecución después de configurarlos.

En Settings > Secrets and variables > Actions agrega:

| Secret | Valor |
| --- | --- |
| DOCKERHUB_USERNAME | Nombre de usuario Docker Hub |
| DOCKERHUB_TOKEN | Token con permiso de escritura |
| EC2_HOST | IP pública o DNS de la instancia, sin http:// |
| EC2_USER | ubuntu |
| EC2_SSH_KEY | Contenido completo de la clave PEM de esta instancia |
| EC2_KNOWN_HOSTS | Línea de known_hosts con clave verificada del servidor |
| EC2_SECURITY_GROUP_ID | ID sg-... del grupo dedicado a la instancia |
| AWS_ACCESS_KEY_ID | Clave de la identidad autorizada para modificar ese grupo |
| AWS_SECRET_ACCESS_KEY | Secreto de esa identidad |
| AWS_SESSION_TOKEN | Token temporal de AWS Academy o sesión STS; vacío para claves permanentes |

En Variables agrega AWS_REGION, por ejemplo us-east-1. Debe corresponder a la instancia y al grupo de seguridad. Crea el environment cloud sin revisores obligatorios para que el despliegue sea automático al push; si el docente exige aprobación manual, explica que existe esa barrera.

La identidad AWS necesita ec2:AuthorizeSecurityGroupIngress y ec2:RevokeSecurityGroupIngress para ese grupo. En AWS Academy usa las credenciales y permisos que entregue el laboratorio; si no permite estas operaciones, solicita al docente la configuración autorizada para SSH. Actualiza las credenciales temporales cuando venzan.

Elimina reglas SSH temporales que queden tras una cancelación abrupta del runner. El trap procura limpiarlas, pero no puede garantizarlo si se detiene toda la máquina del runner.

## 6 Verificar la clave del servidor SSH

Desde tu equipo obtén una candidata con ssh-keyscan. No confíes en ella sin verificarla:

```bash
ssh-keyscan -t ed25519 IP_REAL_EC2 > ec2_known_hosts
ssh-keygen -lf ec2_known_hosts
```

Desde una consola de EC2 de confianza, por ejemplo EC2 Instance Connect, consulta la huella real:

```bash
sudo ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

Compara ambas huellas SHA256. Solo si coinciden, guarda el contenido de ec2_known_hosts en EC2_KNOWN_HOSTS. Si cambia la IP o se recrea la instancia, vuelve a verificar la identidad y actualiza host, reglas de Oracle y secreto.

## 7 Ejecutar y comprobar

1. Haz push a main.
2. Comprueba test, las dos ejecuciones images y deploy en GitHub Actions.
3. En EC2 revisa:

```bash
cd ~/entradas-cloud/deploy
set -a
source .current-images
set +a
docker compose -f compose.yml ps
curl -f http://localhost:8081/actuator/health
curl -f http://localhost:8082/actuator/health
curl -f http://localhost:8081/actuator/info
curl -f http://localhost:8082/actuator/info
```

4. Importa el entorno Postman EC2 y reemplaza REEMPLAZAR_IP_EC2 en ambas variables. Ejecuta la colección.
5. Revisa los registros desde Oracle con las consultas del guion del video.
6. Guarda un ID de compra, haz otro despliegue y vuelve a consultarlo. Así demuestras que la persistencia está en Oracle y sobrevive al reemplazo de contenedores.

Si necesitas ejecutar Compose manualmente, carga primero .current-images como en el ejemplo; de lo contrario faltarán EVENTOS_IMAGE y COMPRAS_IMAGE. Nunca ejecutes source .env porque su sintaxis es la de Compose; el script no lo necesita.

## Diagnóstico

| Síntoma | Comprobación |
| --- | --- |
| Fallo de conexión Oracle | Alias real, acceso de red, laboratorio activo, wallet y permisos del UID 10001 |
| ORA-01017 | Usuario y contraseña del esquema correspondiente |
| Error de validación Hibernate | Haber ejecutado el SQL correcto como cada usuario |
| permission denied en Docker | Reingresar a SSH después de agregar ubuntu al grupo docker |
| SSH host key verification failed | Host y known_hosts deben corresponder a la instancia actual |
| AccessDenied en regla de seguridad | Región, grupo y permisos IAM o del laboratorio |
| Pull access denied | Repositorios públicos, usuario correcto e imágenes con el SHA esperado |
| APP_VERSION no coincide | Revisar logs y qué etiquetas de imagen quedaron desplegadas |
| HTTP 503 al comprar | Servicio de eventos iniciado y Oracle accesible |
| Servicio termina por memoria | Revisar docker stats, logs y memoria disponible |

Para revisar logs con Compose, carga las imágenes de .current-images antes. Evita publicar logs con datos de conexión. La reversión solo restaura imágenes anteriores: no revierte cambios de esquema.

