FROM openjdk:8
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} crust-service.jar
ENTRYPOINT [ "java", "-jar", "/crust-service.jar" ]
EXPOSE 9003