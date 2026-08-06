FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /project
COPY pom.xml .
COPY src src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S secureflow && adduser -S secureflow -G secureflow
COPY --from=build /project/target/secureflow-*.jar app.jar
USER secureflow
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=6 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
