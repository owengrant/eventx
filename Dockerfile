FROM openjdk:12-apline
COPY ./target/eventx-0.10.jar /usr/app
COPY ./application.json /usr/app/application.json
WORKDIR /usr/app
EXPOSE 30000
CMD ["java", "-jar", "eventx-0.10.jar", "-conf", "application.json", "--cluster"]
