FROM maven:3.6-jdk-11-slim
ADD . /app
WORKDIR /app
CMD ["java", "-jar", "eventx-0.7.3.jar", "-conf", "resources/config/config.json", "-cluster"]
