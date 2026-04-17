FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. 複製過去
COPY target/red-pocket.jar red-pocket.jar


ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "red-pocket.jar"]