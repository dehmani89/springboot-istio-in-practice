# Build the jar first from the workspace root:  mvn -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/catalog-service.jar app.jar
USER 1001
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
