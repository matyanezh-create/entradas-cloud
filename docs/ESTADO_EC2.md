# Estado del despliegue en EC2

Preparacion del 30 de octubre de 2026.

La aplicacion y PostgreSQL ya se ejecutan en EC2. Se comprobaron ambos endpoints de salud dentro del servidor. La coleccion Postman se ejecuto con Newman a traves de un tunel SSH: 15 solicitudes y 20 comprobaciones, sin fallos.

Se comprobo por SQL y por la API que la compra 1 se conserva despues de reiniciar PostgreSQL y recrear los contenedores de ambos microservicios. La compra contiene tres entradas a 12500.50, total 37501.50.

## Pendientes para cumplir la entrega

- Habilitar 8081 y 8082 desde la IP de evaluacion y probar directamente la IP publica.
- Repositorio de entrega: https://github.com/matyanezh-create/entradas-cloud (carga del proyecto en curso).
- Configurar Docker Hub y los secretos del workflow.
- Obtener un despliegue real y exitoso mediante GitHub Actions.
- Grabar en Teams el proceso de despliegue, logs y pruebas Postman.
- Entregar enlaces definitivos y documentacion.

La version preparacion-20261030 fue instalada por SSH. No se presenta como un despliegue ejecutado por GitHub Actions.
