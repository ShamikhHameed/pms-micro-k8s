FROM openjdk:8
ARG JAR_FILE=target/api-gateway-0.0.1.jar
COPY ${JAR_FILE} api-gateway.jar
ENTRYPOINT [ "java", "-jar", "/api-gateway.jar" ]
EXPOSE 9191
