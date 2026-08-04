FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/secureflow-*.jar secureflow.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "secureflow.jar"]
