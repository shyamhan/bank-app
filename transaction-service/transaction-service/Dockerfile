# Use a base image with OpenJDK 17
FROM openjdk:17-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/transaction-service-1.0.0.jar /app/transaction-service.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "transaction-service.jar"]
