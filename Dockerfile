
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN mvn dependency:resolve -B

COPY src ./src
RUN mvn package -DskipTests -B


# Use an official OpenJDK as a base image
FROM eclipse-temurin:17-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged JAR file into the container
COPY --from=build /app/target/springbootCacheExample-0.0.1-SNAPSHOT.jar app.jar
# Command to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose port 8089 to the outside world
EXPOSE 8089
