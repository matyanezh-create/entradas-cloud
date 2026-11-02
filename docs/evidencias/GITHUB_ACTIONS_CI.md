# Evidencia de integracion continua en GitHub Actions

Repositorio: https://github.com/matyanezh-create/entradas-cloud

Ejecucion verificada: https://github.com/matyanezh-create/entradas-cloud/actions/runs/41011503369

Commit: e9f99c488a7da14fb23eb94b66b96ad19dba1d01.
Evento: push a main.
Fecha de ejecucion: 30 de octubre de 2026.

## Resultado confirmado

- Trabajo test: success.
- Validacion Bash de deploy.sh y 01-schemas.sh: correcta.
- Validacion de Compose local: correcta.
- Compilacion Maven de ambos modulos: BUILD SUCCESS.
- EventoApiTest: 7 pruebas, 0 fallos.
- CompraApiTest: 8 pruebas, 0 fallos.
- EventoClientTest: 6 pruebas, 0 fallos.
- Total: 21 pruebas correctas.
- Reportes Surefire publicados como artefacto pruebas.

Artefacto: https://github.com/matyanezh-create/entradas-cloud/actions/runs/41011503369/artifacts/12957916793

## Alcance

Los trabajos images y deploy se omitieron porque DEPLOY_ENABLED aun no esta activado. Esta ejecucion demuestra integracion continua y pruebas automaticas; no demuestra despliegue continuo a EC2. El despliegue real de preparacion en EC2 se hizo por SSH y se documenta por separado.
