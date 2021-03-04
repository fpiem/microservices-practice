FROM openjdk:11-jdk-slim
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY application.yml .
ENTRYPOINT ["java","-jar","/app.jar"]
