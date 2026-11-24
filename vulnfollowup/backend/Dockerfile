# Imagen del backend en dos etapas:
# 1) "build" compila con Maven (no hace falta tener Maven ni Java en el PC)
# 2) la imagen final solo lleva el JRE y el .jar -> más pequeña y con menos superficie de ataque
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
# Usuario sin privilegios: si alguien comprometiera la API, no sería root en el contenedor
RUN useradd --system --uid 1001 vfu
COPY --from=build /src/target/*.jar app.jar
USER vfu
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
