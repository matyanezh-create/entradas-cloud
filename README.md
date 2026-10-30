# Sistema de eventos y venta de entradas

Proyecto para CDY2204, experiencia 1, semana 1, forma B. Implementa dos microservicios Spring Boot con Java 17: un catálogo de eventos y un servicio de compras que calcula y conserva el resumen de cada venta. Incluye perfiles para desarrollo local y Oracle Cloud, Docker, CI/CD, pruebas y una colección Postman.

## Despliegue EC2 actual

El docente autorizo cualquier motor relacional. La preparacion en AWS utiliza PostgreSQL 17 y los dos microservicios en EC2. Consulta [el estado y los pendientes](docs/ESTADO_EC2.md) y [la guia de despliegue actual](docs/DESPLIEGUE.md). Oracle XE se conserva para desarrollo local.

## Empieza aquí

1. Instala Java 17 y comprueba `java -version`. Se incluye Maven Wrapper: no necesitas instalar Maven por separado. La primera ejecución necesita Internet para descargar Maven y dependencias.
2. Desde PowerShell en esta carpeta ejecuta `.\mvnw.cmd -B -ntp verify`. En Linux/macOS: `chmod +x mvnw` y `./mvnw -B -ntp verify`.
3. Abre dos terminales en esta carpeta y ejecuta:
   - `java -jar eventos-service/target/eventos-service-1.0.0.jar --spring.profiles.active=local`
   - `java -jar compras-service/target/compras-service-1.0.0.jar --spring.profiles.active=local`
4. Espera el arranque. Abre `http://localhost:8081/actuator/health` y `http://localhost:8082/actuator/health`; ambos deben indicar UP.
5. Importa `postman/Entradas.postman_collection.json` en Postman. Ejecuta sus 15 solicitudes en orden con Collection Runner.
6. Sigue [la guía de nube](docs/DESPLIEGUE.md) para conectar Oracle, Docker Hub, GitHub y EC2.
7. Sigue [el guion de presentación](docs/VIDEO_Y_ENTREGA.md) y revisa [la matriz de evaluación](docs/MATRIZ.md).

En Windows también puedes ejecutar `powershell -ExecutionPolicy Bypass -File scripts/run-local.ps1`; muestra los PID que debes detener al terminar. El script usa Maven Wrapper automáticamente. Usa `-MavenCommand C:\ruta\a\mvn.cmd` si prefieres una instalación propia. La compilación debe terminar antes de iniciar los dos servicios. Las rutas de Java y Maven no deben contener comillas adicionales.

## Oracle instalado en tu PC

Sigue [la guía de Oracle local](docs/ORACLE_LOCAL.md) para configurar Oracle XE sin wallet. Incluye asistentes que crean los esquemas y arrancan ambos servicios sin guardar contraseñas en archivos.

## Alternativa con Docker

Con Docker Desktop iniciado, ejecuta `docker compose -f compose.local.yml up --build -d`. Para detener usa `docker compose -f compose.local.yml down`. Los datos locales permanecen en volúmenes; `down -v` los elimina. El perfil local usa H2 en archivo para desarrollo y no sustituye la demostracion obligatoria del despliegue en EC2 con una base relacional persistente.

## API

| Método | URL | Resultado |
| --- | --- | --- |
| GET | :8081/api/eventos | Eventos activos y futuros, ordenados por fecha |
| GET | :8081/api/eventos/{id} | Detalle de evento activo |
| POST | :8081/api/eventos | Crear evento |
| PUT | :8081/api/eventos/{id} | Actualizar nombre, fecha y precio |
| DELETE | :8081/api/eventos/{id} | Baja lógica del evento |
| POST | :8082/api/compras | Comprar entradas y guardar resumen |
| GET | :8082/api/compras/{id} | Consultar la compra guardada |

Crear evento:

```json
{"nombre":"Concierto Cloud","fecha":"2099-06-15T20:00:00-03:00","precio":12500.50}
```

Comprar entradas, reemplazando el ID por el recibido al crear el evento:

```json
{"eventoId":1,"cantidad":3}
```

Resultado esperado: evento Concierto Cloud, cantidad 3, precio unitario 12500.50 y total 37501.50, además del ID de compra y la fecha de compra. Los precios se manejan con BigDecimal. Se asume una sola moneda por instalación; el caso no exige conversión de monedas.

## Organización

- `eventos-service`: controladores REST, validación, servicio transaccional y repositorio JPA.
- `compras-service`: consulta HTTP a eventos, cálculo del total, persistencia y recuperación de compras.
- `database`: scripts de creación de tablas y secuencias Oracle, uno por esquema.
- `deploy`: Compose Oracle, plantilla de variables y script de despliegue.
- `.github/workflows`: pruebas, publicación de dos imágenes y despliegue en EC2.
- `postman`: colección con comprobaciones y entorno EC2.
- `docs`: arquitectura, configuración, matriz, video y verificación.

## Decisiones y alcance

Las compras guardan una copia del nombre y precio del evento para preservar el historial. El precio lo obtiene el servidor desde eventos; no se acepta un precio enviado por el cliente. Las compras no se editan ni eliminan porque el caso solicita registro de ventas, no cancelaciones. El CRUD completo corresponde al catálogo de eventos. No se implementan pagos reales, aforo ni reserva de stock porque no se solicitan en la forma B.

Esta API es una demostración académica sin autenticación de usuarios. Usa los puertos de aplicación restringidos a las IP de evaluación; no se presenta como un sistema listo para recibir pagos o exponerse públicamente sin controles adicionales. Los cambios simultáneos en el catálogo pueden ocurrir entre la consulta del evento y el guardado de una compra; el precio queda fijado al consultar el catálogo. No hay transacción distribuida entre servicios.

La entrega todavía requiere configurar cuentas y realizar el despliegue real. Revisa [VERIFICACION.md](docs/VERIFICACION.md) para distinguir pruebas realizadas de comprobaciones pendientes.



