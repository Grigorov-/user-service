# Use OpenJDK 22 as base image
FROM openjdk:22-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the target directory
COPY target/user-registration-0.1.0.jar app.jar

# Expose port (optional, only needed if running on Compute Engine)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]