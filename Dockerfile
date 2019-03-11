FROM maven:3.6-jdk-11-slim
ADD . /app
WORKDIR /app
CMD ["mvn", "clean", "compile", "vertx:run"]
