# Video y entrega final

El docente permite cualquier base relacional y exige el despliegue en AWS EC2 por GitHub Actions, los logs y las pruebas Postman contra los endpoints expuestos de EC2. La version desplegada usa PostgreSQL 17; Oracle XE queda como alternativa de desarrollo local.

## Antes de grabar

Completa los pendientes de ESTADO_EC2.md. La preparacion manual por SSH no es suficiente como evidencia de GitHub Actions. El informe de Newman por tunel SSH tampoco sustituye la prueba directa contra la IP de EC2.

Inicia una reunion Teams con tu companero o companera. Prepara GitHub Actions, Docker Hub, la instancia EC2, Postman y la conexion SSH. No muestres .env, claves PEM ni tokens.

## Secuencia de demostracion

1. Presenta integrantes y el sistema de eventos y venta de entradas.
2. Explica los dos microservicios y los dos esquemas de PostgreSQL. El precio de la compra se conserva como dato historico.
3. Muestra validacion, calculo BigDecimal y una prueba automatizada.
4. En Postman selecciona el entorno EC2. Muestra que las URLs contienen la IP publica del servidor y los puertos 8081/8082.
5. Crea un evento con precio 12500.50 y compra tres entradas. Destaca cantidad, precio unitario y total 37501.50.
6. Consulta el evento y la compra directamente en PostgreSQL. Guarda el ID de esa compra para comprobarla despues del despliegue.
7. Muestra errores esperados: cantidad cero, evento inexistente y datos invalidos.
8. Haz un cambio pequeno en el proyecto, commit y push a main. Muestra pruebas, publicacion de imagenes y despliegue exitoso en GitHub Actions.
9. Muestra logs de los microservicios en EC2. Comprueba que actuator/info corresponde al SHA del commit desplegado.
10. Vuelve a consultar la compra anterior en Postman y en la base para demostrar persistencia.
11. Muestra actualizacion y baja logica del evento, conservandose la compra historica.
12. Relaciona la demostracion con los criterios de la pauta.

La coleccion elimina logicamente el evento que crea; si necesitas mostrar mas compras, crea un evento nuevo. Conserva por separado el ID destinado a la prueba entre despliegues.

## Consultas de evidencia en EC2

~~~bash
sudo docker exec entradas-cloud-postgres-1 psql -U postgres -d entradas -c 'SELECT * FROM eventos.eventos ORDER BY id;'
sudo docker exec entradas-cloud-postgres-1 psql -U postgres -d entradas -c 'SELECT * FROM compras.compras ORDER BY id;'
sudo docker logs --tail 50 entradas-cloud-eventos-1
sudo docker logs --tail 50 entradas-cloud-compras-1
~~~

## Entrega en AVA

- Enlace al video de Teams accesible para el docente.
- Enlace al repositorio GitHub con permisos del evaluador.
- ZIP o RAR de la documentacion final.

Completar:
- Integrantes: ____________________
- Seccion y docente: ____________________
- Repositorio: ____________________
- Ejecucion exitosa de GitHub Actions: ____________________
- Grabacion Teams: ____________________
- Fecha de verificacion externa en EC2: ____________________

No entregar secretos, wallets ni claves privadas. La nota la determina el docente segun la evidencia presentada.
