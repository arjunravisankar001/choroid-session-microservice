# Multi-stage Dockerfile for choroid-session-service
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and config first for better layer caching
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./

# Ensure Gradle wrapper is executable (Linux)
RUN chmod +x gradlew

# Copy source
COPY src ./src

# Build the Spring Boot fat jar (skip tests for speed/stability in container)
RUN ./gradlew --no-daemon clean bootJar -x test

# Runtime stage (use smaller JRE image)
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Optionally set active profile / JVM opts via env vars
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS=""

# Copy built jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose application port (from application.properties)
EXPOSE 8300

# Optional health check (uncomment if curl added)
# RUN apk add --no-cache curl
# HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8300/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
