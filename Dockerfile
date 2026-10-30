FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY eventos-service/pom.xml eventos-service/pom.xml
COPY compras-service/pom.xml compras-service/pom.xml
COPY eventos-service/src eventos-service/src
COPY compras-service/src compras-service/src
ARG SERVICE
RUN mvn -B -ntp -pl ${SERVICE} -am package -DskipTests
RUN cp ${SERVICE}/target/${SERVICE}-1.0.0.jar /app.jar
FROM eclipse-temurin:17-jre-jammy
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app --create-home app && mkdir -p /app/data && chown -R app:app /app
WORKDIR /app
COPY --from=build --chown=app:app /app.jar app.jar
USER app
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-jar","app.jar"]

