# Use secure and up-to-date JRE image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the JAR file
COPY target/AIChatbot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run as non-root for security
USER 1001

ENTRYPOINT ["java", "-jar", "app.jar"]
