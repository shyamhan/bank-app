# Use a base image with OpenJDK 17
FROM openjdk:17-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/notification-service-1.0.0.jar /app/notification-service.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "notification-service.jar"]
