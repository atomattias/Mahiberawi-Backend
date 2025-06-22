# Use Eclipse Temurin JDK 17 (more reliable than OpenJDK)
FROM eclipse-temurin:17-jdk-alpine AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p uploads

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 