# Matriz para revisar la evaluacion

La aclaracion del docente permite cualquier base relacional y exige demostrar el despliegue en AWS EC2 mediante GitHub Actions, con logs y pruebas Postman contra la instancia. Se conserva Oracle XE como opcion local; el despliegue EC2 usa PostgreSQL.

| Criterio | Implementacion | Evidencia necesaria |
| --- | --- | --- |
| Pipeline automatico, pruebas y despliegue | ci-cd.yml, pruebas Maven, imagenes y deploy.sh | Ejecucion real verde despues de push a main; sigue pendiente |
| Buenas practicas | Capas, validacion, BigDecimal, usuarios separados, secretos externos | Explicar codigo, pruebas y configuracion sin mostrar secretos |
| Persistencia | PostgreSQL 17 en EC2, esquemas separados y volumen persistente | Compra conservada tras reiniciar PostgreSQL y recrear microservicios |
| Componentes cloud | Instancia EC2, Docker y perfil postgres | Contenedores activos, logs y version publicada |
| Postman | Coleccion de 15 solicitudes y 20 comprobaciones | Prueba contra IP publica de EC2; la prueba por tunel SSH no la sustituye |
| Video explicativo | Guion de presentacion | Grabacion real de Teams, despliegue y funciones en tiempo real |

## Entrega

- [ ] Repositorio GitHub con el proyecto y permisos del evaluador.
- [ ] Pipeline verde con logs de pruebas, publicacion y despliegue.
- [ ] Coleccion Postman ejecutada contra la IP publica de EC2.
- [ ] Persistencia y logs mostrados en el video.
- [ ] Grabacion Teams accesible.
- [ ] ZIP de documentacion sin claves, tokens, contrasenas ni wallet.

Las pruebas realizadas y los pendientes se detallan en VERIFICACION.md. Esta matriz no garantiza la nota: la determina el docente.
