# Use Maven image to build the application
FROM maven:3.8.6-openjdk-8-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application (dependencies will be downloaded during build)
# Skip the separate dependency download step to avoid timeout issues
RUN mvn clean package -DskipTests -B

# Use Eclipse Temurin (OpenJDK replacement) for runtime
FROM eclipse-temurin:8-jre-jammy

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the application with optimized JVM settings for faster startup
ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-XX:+UseSerialGC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
