FROM maven:3-openjdk-11 AS build-stage
WORKDIR /tmp
COPY . /tmp
RUN mvn package

FROM openjdk:11
COPY --from=build-stage /tmp/target/service.chat-1.0-SNAPSHOT.jar /chatservice.jar
CMD ["java", "-jar", "/chatservice.jar"]