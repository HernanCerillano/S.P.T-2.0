# =============================================================================
# Dockerfile multi-stage para el backend SPT (Spring Boot 4 + Java 21).
# Listo para Railway: lee PORT del entorno, deja la JVM tomar memoria del cgroup
# del container y corre como usuario no-root.
# =============================================================================

# ── Stage 1: build con Maven wrapper ────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cachear dependencias: copiar primero pom + wrapper, descargar deps, después src.
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true

COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# ── Stage 2: runtime mínimo (solo JRE) ──────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no-root para seguridad.
RUN addgroup -g 1000 spring && adduser -u 1000 -G spring -s /bin/sh -D spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# Railway inyecta PORT; la app lo lee desde application.yml (server.port=${PORT:8080}).
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
