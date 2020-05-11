FROM openjdk:12-alpine
WORKDIR /usr/app
COPY ./target/eventx-0.10.0.jar /usr/app
COPY ./application.json /usr/app
EXPOSE 30000
CMD ["java", "-jar", "eventx-0.10.0.jar", "-conf", "application.json", "--cluster"]
