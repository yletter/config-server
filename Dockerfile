FROM eclipse-temurin:17-jdk-jammy

COPY target/config-server-0.0.1-SNAPSHOT.jar /tmp/config-server.jar

CMD ["java", "-jar", "/tmp/config-server.jar"]