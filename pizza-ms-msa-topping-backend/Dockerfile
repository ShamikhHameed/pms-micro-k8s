FROM openjdk:8
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} topping-service.jar
ENTRYPOINT [ "java", "-jar", "/topping-service.jar" ]
EXPOSE 9002