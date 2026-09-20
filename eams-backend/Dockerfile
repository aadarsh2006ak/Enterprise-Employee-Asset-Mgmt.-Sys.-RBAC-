# ==============================================================================
# Multi-stage Production Dockerfile for Enterprise Asset Management Backend
# ==============================================================================

# Stage 1: Build JAR with Maven & OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application sources and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Hardened Production JRE Runtime
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# Install curl for container health checking
RUN apk add --no-cache curl tzdata

# Set timezone to UTC
ENV TZ=UTC

# Create unprivileged system user and group for security
RUN addgroup -S eamsgroup && adduser -S eamsuser -G eamsgroup

# Copy executable jar from builder stage
COPY --from=builder /build/target/eams-backend-*.jar app.jar

# Set permissions
RUN chown -R eamsuser:eamsgroup /app

USER eamsuser

# Expose Spring Boot Application Port
EXPOSE 8080

# Production-tuned JVM flags for containerized environments
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom -Dspring.backgroundpreinitializer.ignore=true"

# Container Healthcheck against Spring Boot Actuator
HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=30s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
