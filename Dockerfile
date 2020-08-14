FROM openjdk:8
ADD ./libs/application.jar application.jar
ENTRYPOINT ["java", "-jar", "./application.jar"]
