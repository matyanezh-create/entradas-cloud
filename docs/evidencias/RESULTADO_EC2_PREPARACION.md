# Prueba de aplicacion y persistencia en EC2

Fecha de preparacion: 30 de octubre de 2026.

- Instancia: i-065c7f0d024a05017; IP observada 54.81.82.5.
- Ubuntu 24.04, Docker y Docker Compose.
- PostgreSQL 17 con datos en volumen persistente.
- Dos microservicios con perfil postgres.
- Newman: 15 solicitudes y 20 comprobaciones correctas, cero fallos.

## Alcance de las pruebas

La coleccion se ejecuto desde el PC mediante un tunel SSH a la instancia. Las URLs del reporte son locales al tunel y no certifican acceso HTTP por la IP publica. La apertura de los puertos y la prueba directa requerida por el docente siguen pendientes.

La preparacion inicial se realizo por SSH. GitHub Actions y Docker Hub siguen pendientes de configuracion y ejecucion.

## Persistencia

Se reinicio PostgreSQL y se recrearon los dos contenedores de aplicacion. La compra 1 se recupero despues con cantidad 3, precio unitario 12500.50 y total 37501.50. El evento de prueba quedo dado de baja logica por la coleccion.

## Informes adjuntos

- ec2-postman-tunel.json: resultado completo de Newman.
- ec2-postman-tunel.xml: resultado JUnit.
- ec2-persistencia.json: compra recuperada despues del reinicio y recreacion.
