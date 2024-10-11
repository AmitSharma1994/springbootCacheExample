# Use an official OpenJDK as a base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged JAR file into the container
COPY target/springbootCacheExample-0.0.1-SNAPSHOT.jar app.jar

# Command to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose port 8080 to the outside world
EXPOSE 8080
