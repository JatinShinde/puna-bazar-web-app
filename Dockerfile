# Multi-stage Dockerfile for Pune Bazar Web App

# Stage 1: Build stage with Maven & JDK 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy maven wrapper & pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build final jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage with minimal JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create directory for persistent database
RUN mkdir -p /app/data

# Copy built JAR from build stage
COPY --from=build /app/target/puna-bazar-app-1.0.0.jar app.jar

# Expose port (Render/Railway sets PORT environment variable dynamically)
EXPOSE 8080

# Environment defaults
ENV PORT=8080

# Entry point to execute the app
ENTRYPOINT ["java", "-jar", "app.jar"]
