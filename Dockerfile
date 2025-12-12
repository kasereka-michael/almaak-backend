# Backend Dockerfile (Spring Boot)
# Build stage
FROM maven:3.9.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy built jar (assumes a single jar in target)
COPY --from=build /app/target/*SNAPSHOT.jar app.jar
# Expose application port (adjust if different)
EXPOSE 8080
# Healthcheck can be configured in Render separately; optional here
# HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
