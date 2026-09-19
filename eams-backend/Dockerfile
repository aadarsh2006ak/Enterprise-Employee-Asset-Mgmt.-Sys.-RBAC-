# Multi-stage Dockerfile for Enterprise Asset Management System Backend
# Stage 1: Build application with Maven & OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Copy POM and download dependencies for faster layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar (skip tests in docker build for speed)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Production JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create unprivileged system user for security
RUN addgroup -S eamsgroup && adduser -S eamsuser -G eamsgroup

# Copy executable jar from builder stage
COPY --from=builder /build/target/eams-backend-*.jar app.jar

# Adjust ownership
RUN chown -R eamsuser:eamsgroup /app

USER eamsuser

# Expose HTTP port
EXPOSE 8080

# Production-tuned JVM flags for containerized environment
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
