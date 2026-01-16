# Build stage
FROM gradle:8-jdk21 AS builder
# Copy root Gradle configuration files
COPY build.gradle.kts settings.gradle.kts /app/
COPY gradle /app/gradle
# Copy the module we need to build
COPY 29-docker-deployment /app/29-docker-deployment
WORKDIR /app
RUN gradle :29-docker-deployment:bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/29-docker-deployment/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]