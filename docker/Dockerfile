FROM openjdk:11.0.2-jre-slim
ADD . /app
WORKDIR /app
EXPOSE 30000
CMD ["java", "-jar", "eventx-0.7.3.jar", "-conf", "resources/config/config.json", "--cluster"]
