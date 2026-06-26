# ===========================================================================
# Backend InostraTech (Spring Boot, Java 21) — imagen para Render
# Build multi-etapa: compila con Maven y ejecuta con un JRE liviano.
# ===========================================================================

# --- Etapa 1: build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom para cachear dependencias.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Luego el codigo y empaquetado (sin tests para acelerar el deploy).
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Etapa 2: runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia el jar generado (nombre fijo para no depender de la version).
COPY --from=build /app/target/*.jar app.jar

# Render inyecta la variable PORT; la app la lee via server.port=${PORT:8080}.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
