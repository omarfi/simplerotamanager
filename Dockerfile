# Use an official Maven image with JDK 17 for the build stage
FROM maven:3.8.1-openjdk-17-slim as builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven configuration files and source code into the container
COPY pom.xml ./
COPY src ./src

# Use Maven to build the application
RUN mvn clean package -DskipTests

# Use an official OpenJDK image for JDK 17 for the runtime stage
FROM openjdk:17-oracle

# Set the working directory in the container
WORKDIR /app

# Copy only the artifact from the build stage into this new stage
COPY --from=builder /app/target/*.jar app.jar

# Set the default command to execute the jar file
CMD ["java", "-jar", "app.jar"]
