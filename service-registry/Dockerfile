FROM openjdk:8
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} service-registry.jar
ENTRYPOINT [ "java", "-jar", "/service-registry.jar" ]
EXPOSE 8761