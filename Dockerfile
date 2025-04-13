# Dockerfile
FROM openjdk:17-jdk

# 빌드 산출물의 실제 파일명 예: demo-0.0.1-SNAPSHOT.jar
ARG JAR_FILE=demo/build/libs/demo-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
