# Use an official Maven image for the build stage
FROM maven:3.8.4-jdk-11 as builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven configuration files and source code into the container
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn clean package

# Use an official Java runtime as a base image for the running stage
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy only the artifact from the build stage into this new stage
COPY --from=builder /app/target/*.jar app.jar

# Set the default command to execute the jar file
CMD ["java", "-jar", "app.jar"]
