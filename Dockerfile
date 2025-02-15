# Use OpenJDK 22 as base image for build stage
FROM openjdk:22-jdk-slim AS build

# Set working directory
WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Install dependencies and compile proto files
RUN apt-get update && apt-get install -y maven \
    && mvn install -DskipTests \
    && mvn clean package -DskipTests

# Use a smaller JDK image for the final stage
FROM openjdk:22-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/user-registration-0.1.0.jar app.jar

# Expose port (for running in GCP Compute Engine or Kubernetes)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
