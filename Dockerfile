# ---------- BUILD STAGE ----------
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the JAR (skip tests for faster Docker builds)
RUN mvn -q -DskipTests clean package

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:17-jre
WORKDIR /opt/app

# Copy final artifact from build stage
COPY --from=build /app/target/fraud-rule-engine-0.1.0.jar app.jar

# Expose application port
EXPOSE 8080

# JVM tuning for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/app/app.jar"]
