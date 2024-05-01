# Use an official Maven image with JDK 17 for the build stage
FROM maven:3.8.6-openjdk-17-slim as builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven configuration files and source code into the container
COPY pom.xml ./
COPY src ./src

# Use Maven to build the application
RUN mvn clean package -DskipTests

# Use an official Node.js image to build the Angular web app
FROM node:14 as webapp-builder

# Set the working directory for the Angular app
WORKDIR /webapp

# Copy the Angular web app source files from the Spring Boot structure
COPY src/main/webapp ./

# If package.json and other configuration files exist, uncomment the following lines
# COPY src/main/webapp/package*.json ./
# RUN npm install

# Build the Angular application
# RUN npm run build

# Use an official OpenJDK image for JDK 17 for the runtime stage
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot application jar from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Copy the Angular web app from the webapp build stage to the static folder in the Spring Boot app
COPY --from=webapp-builder /webapp /app/static

# Set the default command to execute the jar file
CMD ["java", "-jar", "app.jar"]
