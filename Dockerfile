# ===== Build Stage =====
FROM maven:3.9.6-amazoncorretto-17 AS build

WORKDIR /app

# Copy the entire project
COPY . .

# Build the Spring Boot application (skip integration tests that need Docker)
RUN ./gradlew clean build -x test || ./gradlew clean build --tests '*' --tests '!**/*IntegrationTest' 


# ===== Runtime Stage =====
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy only the built JAR file
COPY --from=build /app/build/libs/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java","-jar","app.jar"]
