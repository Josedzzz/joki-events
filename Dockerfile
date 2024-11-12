FROM ubuntu:latest AS build
WORKDIR /app
RUN apt-get update && apt-get install -y openjdk-17-jdk

COPY . /app

RUN ./gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
