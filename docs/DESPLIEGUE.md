# Despliegue actual en AWS EC2 con PostgreSQL

El docente autorizo cualquier base de datos relacional. El objetivo obligatorio sigue siendo mostrar el despliegue mediante GitHub Actions y las pruebas con Postman contra EC2. El despliegue preparado utiliza PostgreSQL 17 en la misma instancia que los microservicios.

## Estado de preparacion del 30 de octubre de 2026

- Instancia: i-065c7f0d024a05017, nombre ssj.
- Region: us-east-1.
- Tipo: t3.small, Ubuntu 24.04, disco de 20 GiB.
- IP publica observada: 54.81.82.5. Puede cambiar al detener y volver a iniciar.
- Docker y Compose instalados.
- Eventos, compras y PostgreSQL funcionando.
- Version de preparacion: preparacion-20261030.
- Esta preparacion se hizo por SSH; todavia no es evidencia de despliegue mediante GitHub Actions.
- La coleccion paso por un tunel SSH: falta demostrar acceso directo mediante la IP publica.

## Persistencia y credenciales

PostgreSQL no publica el puerto 5432 al exterior. Guarda sus datos en el volumen entradas-cloud-postgres-data. Cada microservicio usa su propio usuario y esquema: eventos_app/eventos y compras_app/compras.

Las tres contrasenas se generaron en EC2 y estan solo en ~/entradas-cloud/deploy/.env, con permisos 600. No deben copiarse a GitHub ni incluirse en la documentacion.

Los scripts de deploy/postgres se ejecutan automaticamente solo al inicializar un volumen vacio. No borrar el volumen para cambiar una contrasena o volver a desplegar. Los cambios de esquema posteriores requieren migraciones.

## Acceso para Postman

En el grupo de seguridad sg-0a8960ac01ae56636 agrega TCP 8081 y TCP 8082 desde Mi IP. Mantiene SSH TCP 22 desde Mi IP. El acceso de GitHub Actions a SSH se autoriza temporalmente desde la IP del runner y requiere credenciales AWS del laboratorio.

Despues comprueba:
- http://54.81.82.5:8081/actuator/health
- http://54.81.82.5:8082/actuator/health

Importa postman/EC2.postman_environment.json y selecciona ese entorno. Si cambia la IP de la instancia, actualiza ambas variables y EC2_HOST en GitHub.

## Repositorio GitHub y Docker Hub

Sube esta carpeta como raiz del repositorio. Deben quedar pom.xml, .github, los dos modulos, deploy y postman directamente en la raiz. No subir target, data, logs, .env, archivos PEM ni el archivo privado de credenciales.

Crea dos repositorios publicos en Docker Hub: entradas-eventos y entradas-compras. Crea un token de escritura para GitHub Actions, sin compartirlo en el chat. El workflow construye y publica imagenes etiquetadas con el SHA del commit.

En GitHub configura estos secretos:

| Secreto | Valor o ubicacion |
| --- | --- |
| DOCKERHUB_USERNAME | Usuario Docker Hub |
| DOCKERHUB_TOKEN | Token de escritura |
| EC2_HOST | 54.81.82.5 |
| EC2_USER | ubuntu |
| EC2_SSH_KEY | Contenido de la clave privada vockey/labsuser, solo en GitHub Secrets |
| EC2_KNOWN_HOSTS | Clave publica SSH del servidor, verificando su huella |
| EC2_SECURITY_GROUP_ID | sg-0a8960ac01ae56636 |
| AWS_ACCESS_KEY_ID | Credencial temporal de AWS Academy |
| AWS_SECRET_ACCESS_KEY | Credencial temporal de AWS Academy |
| AWS_SESSION_TOKEN | Token de la sesion AWS Academy |

Variables del repositorio:
- AWS_REGION: us-east-1.
- DATABASE_ENGINE: postgres.
- DEPLOY_ENABLED: true, solo despues de cargar todos los secretos. Hasta entonces los push ejecutan las pruebas y omiten publicacion y despliegue.

El environment cloud no debe tener aprobacion manual si se exige despliegue automatico al push. Las credenciales temporales se deben renovar cuando venza la sesion del laboratorio.

La identidad AWS necesita autorizar y revocar reglas SSH en el grupo de seguridad indicado. El workflow intenta limpiar la regla temporal al terminar; revisar reglas si el runner se cancela de forma abrupta.

## Ejecucion del pipeline

1. Push a main.
2. test valida Bash/Compose y ejecuta mvn verify.
3. images construye y publica las imagenes Docker de ambos servicios.
4. deploy abre SSH solo para el runner, copia configuracion y ejecuta deploy.sh.
5. deploy.sh descarga imagenes, conserva el volumen PostgreSQL y comprueba salud y APP_VERSION.
6. Si la salud falla, intenta restaurar las imagenes anteriores, sin revertir esquemas.

Una pull request solo ejecuta pruebas. Las ejecuciones reales de GitHub Actions, Docker Hub y la reversion automatica siguen pendientes de configurar y comprobar.

## Revision del servidor

Conectado por SSH:

~~~bash
cd ~/entradas-cloud/deploy
set -a
source .current-images
set +a
sudo --preserve-env=EVENTOS_IMAGE,COMPRAS_IMAGE,APP_VERSION docker compose -p entradas-cloud -f compose.postgres.yml ps
sudo docker logs --tail 50 entradas-cloud-eventos-1
sudo docker logs --tail 50 entradas-cloud-compras-1
sudo docker exec entradas-cloud-postgres-1 psql -U postgres -d entradas -c 'SELECT * FROM eventos.eventos;'
sudo docker exec entradas-cloud-postgres-1 psql -U postgres -d entradas -c 'SELECT * FROM compras.compras;'
~~~

No muestres .env ni docker inspect de los contenedores, porque incluyen credenciales. Los datos de prueba no representan pagos reales.

La configuracion Oracle Cloud anterior se conserva como alternativa en DESPLIEGUE_ORACLE_OPCIONAL.md; no corresponde al despliegue PostgreSQL actual.
