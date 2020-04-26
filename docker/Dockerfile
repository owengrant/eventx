FROM openjdk:12-apline
COPY . /usr/app
WORKDIR /usr/app
EXPOSE 30000
CMD ["java", "-jar", "eventx-0.10.jar", "-conf", "resources/config/config.json", "--cluster"]
