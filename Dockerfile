# Dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR
COPY target/gerenciadorDeSenhas*.jar app.jar

# Segurança - usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]