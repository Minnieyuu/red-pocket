FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/red-pocket.jar red-pocket.jar

ENTRYPOINT ["java", "-Xms256m", "-Xmx768m", "-jar", "red-pocket.jar"]