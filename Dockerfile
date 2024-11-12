FROM ubuntu:latest AS build
WORKDIR /app
RUN apt-get update && apt-get install -y openjdk-17-jdk libx11-dev libxext-dev libfontconfig1

COPY . /app

RUN ./gradlew bootJar --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
EXPOSE 8080

# Install font libraries in the slim image as well
RUN apt-get update && apt-get install -y libx11-dev libxext-dev libfontconfig1

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
