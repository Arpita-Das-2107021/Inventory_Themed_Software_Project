
# Your Dockerfile uses a multi-stage build.
# Meaning:

# Stage 1 → Build the project (compile code)
# Stage 2 → Run the project (only JAR + Java runtime)

# This makes the image smaller and cleaner.


# ---------- Stage 1: Build the application ----------
# Use Maven + Java 17 to compile and package the Spring Boot app.
FROM maven:3.9-eclipse-temurin-17 AS builder

# All following commands run inside /app.
WORKDIR /app

# Copy only the Maven file first for better layer caching.
# Copy this file from your computer to the container
COPY pom.xml ./

# Download dependencies before copying source code.
RUN mvn dependency:go-offline

# Copy the source code.
COPY src ./src

# Build a runnable JAR.
RUN mvn clean package -DskipTests

# This command builds the project.
# Equivalent to running on your PC:
# mvn clean package
# Result:
# target/myproject.jar
# Now the compiled JAR exists inside the container.


# ---------- Stage 2: Run the application ----------
# Smaller image that contains only Java runtime.
# Now we switch to another image.
# This image contains:
# Java Runtime only
FROM eclipse-temurin:17-jre

WORKDIR /app

# Creates a non-root user.
# Why?
# Running containers as root is unsafe.
# So we create:
# user: appuser
# group: appgroup

# Create a non-root user for safer container runtime.
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Copy the packaged JAR from builder stage.
COPY --from=builder /app/target/*.jar app.jar

# Give ownership to non-root user.
RUN chown appuser:appgroup app.jar

# Run as non-root user.
USER appuser

# Spring Boot runs on 8080 inside container.
EXPOSE 8080

# Start the Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]